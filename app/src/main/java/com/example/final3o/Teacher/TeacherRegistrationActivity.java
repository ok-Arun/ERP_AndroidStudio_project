package com.example.final3o.Teacher;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.final3o.Admin.AdminDashboardActivity;
import com.example.final3o.Admin.User.ManageUsersActivity;
import com.example.final3o.R;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TeacherRegistrationActivity extends AppCompatActivity {

    private EditText nameEditText, emailEditText, passwordEditText, qualificationsEditText, phoneEditText;
    private AutoCompleteTextView subjectAutoCompleteTextView;
    private Button registerButton;
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    private List<String> courseList;
    private ArrayAdapter<String> courseAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_registration);

        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Initialize views
        nameEditText = findViewById(R.id.nameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        qualificationsEditText = findViewById(R.id.qualificationsEditText);
        phoneEditText = findViewById(R.id.phoneEditText);
        subjectAutoCompleteTextView = findViewById(R.id.subjectAutoCompleteTextView);
        registerButton = findViewById(R.id.registerButton);

        courseList = new ArrayList<>();
        courseAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, courseList);
        subjectAutoCompleteTextView.setAdapter(courseAdapter);

        // Fetch course names to populate specialization dropdown
        fetchCourses();

        registerButton.setOnClickListener(v -> registerTeacher());
    }

    private void fetchCourses() {
        firestore.collection("courses")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String cid = doc.getString("cid");
                        if (cid != null) {
                            courseList.add(cid);
                        }
                    }
                    courseAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error fetching courses: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void registerTeacher() {
        String fullName = nameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String specialization = subjectAutoCompleteTextView.getText().toString().trim();
        String qualifications = qualificationsEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();

        // Basic validation
        if (TextUtils.isEmpty(fullName) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password) ||
                TextUtils.isEmpty(specialization) || TextUtils.isEmpty(qualifications) || TextUtils.isEmpty(phone)) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create Firebase Auth user
        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser firebaseUser = authResult.getUser();
                    if (firebaseUser == null) return;

                    // Generate teacher ID: format techYY### where YY = last two digits of year
                    int year = Calendar.getInstance().get(Calendar.YEAR);
                    String yearPrefix = String.format("tech%02d", year % 100);

                    firestore.collection("teachers")
                            .whereGreaterThanOrEqualTo("teacherId", yearPrefix + "000")
                            .whereLessThan("teacherId", yearPrefix + "999")
                            .get()
                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                int maxId = 0;
                                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                                    String id = doc.getString("teacherId");
                                    if (id != null && id.startsWith(yearPrefix)) {
                                        try {
                                            int num = Integer.parseInt(id.substring(yearPrefix.length()));
                                            if (num > maxId) maxId = num;
                                        } catch (NumberFormatException ignored) {}
                                    }
                                }

                                String newTeacherId = yearPrefix + String.format("%03d", maxId + 1);

                                // Prepare teacher data for 'teachers' collection
                                Map<String, Object> teacherData = new HashMap<>();
                                teacherData.put("uid", firebaseUser.getUid());
                                teacherData.put("name", fullName);
                                teacherData.put("email", email);
                                teacherData.put("cid", specialization);
                                teacherData.put("qualifications", qualifications);
                                teacherData.put("phone", phone);
                                teacherData.put("teacherId", newTeacherId);
                                teacherData.put("role", "teacher");
                                teacherData.put("userStatus", "active");
                                teacherData.put("createdAt", Timestamp.now());

                                // Save teacher data
                                firestore.collection("teachers").document(newTeacherId)
                                        .set(teacherData)
                                        .addOnSuccessListener(aVoid -> {
                                            // Also save in 'users' collection
                                            Map<String, Object> userData = new HashMap<>();
                                            userData.put("uid", firebaseUser.getUid());
                                            userData.put("name", fullName);
                                            userData.put("email", email);
                                            userData.put("cid", specialization);
                                            userData.put("teacherId", newTeacherId);
                                            userData.put("role", "teacher");
                                            teacherData.put("userStatus", "active");
                                            userData.put("createdAt", Timestamp.now());

                                            firestore.collection("users").document(newTeacherId)
                                                    .set(userData)
                                                    .addOnSuccessListener(aVoid1 -> {
                                                        Toast.makeText(this, "Registration Successful. ID: " + newTeacherId, Toast.LENGTH_LONG).show();

                                                        Intent intent = new Intent(TeacherRegistrationActivity.this, ManageUsersActivity.class);
                                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                        startActivity(intent);
                                                        finish();
                                                    })

                                                    .addOnFailureListener(e -> Toast.makeText(this, "Users Collection Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                                        })
                                        .addOnFailureListener(e -> Toast.makeText(this, "Firestore Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());

                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "ID Fetch Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());

                })
                .addOnFailureListener(e -> Toast.makeText(this, "Auth Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void clearFields() {
        nameEditText.setText("");
        emailEditText.setText("");
        passwordEditText.setText("");
        subjectAutoCompleteTextView.setText("");
        qualificationsEditText.setText("");
        phoneEditText.setText("");
    }
}
