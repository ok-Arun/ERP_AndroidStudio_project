package com.example.final3o.Registration;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.example.final3o.LoginActivity;
import com.example.final3o.R;
import com.google.firebase.firestore.*;
import java.util.*;

public class RegisterActivity extends AppCompatActivity {

    private EditText fullNameEditText, emailEditText, dobEditText, parentsNameEditText, passwordEditText, confirmPasswordEditText;
    private Spinner genderSpinner, courseSpinner;
    private Button registerButton, loginButton;

    private final String[] genderOptions = {"Select Gender", "Male", "Female", "Other"};
    private ArrayList<String> courseNames = new ArrayList<>();
    private ArrayList<String> courseIDs = new ArrayList<>();
    private ArrayList<String> courseDepartments = new ArrayList<>();
    private ArrayAdapter<String> courseAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.example.final3o.R.layout.activity_register);

        // Initialize views
        fullNameEditText = findViewById(com.example.final3o.R.id.fullNameEditText);
        emailEditText = findViewById(com.example.final3o.R.id.emailEditText);
        dobEditText = findViewById(com.example.final3o.R.id.dobEditText);
        parentsNameEditText = findViewById(com.example.final3o.R.id.parentsNameEditText);
        passwordEditText = findViewById(com.example.final3o.R.id.passwordEditText);
        confirmPasswordEditText = findViewById(com.example.final3o.R.id.confirmPasswordEditText);
        genderSpinner = findViewById(com.example.final3o.R.id.genderSpinner);
        courseSpinner = findViewById(com.example.final3o.R.id.courseSpinner);
        registerButton = findViewById(com.example.final3o.R.id.registerButton);
        loginButton = findViewById(R.id.loginButton);

        // Setup gender and course spinners
        setupGenderSpinner();
        setupCourseSpinner();
        setupDOBPicker();
        fetchCoursesFromFirestore();

        registerButton.setOnClickListener(v -> registerUser());
        loginButton.setOnClickListener(v -> startActivity(new Intent(RegisterActivity.this, LoginActivity.class)));
    }

    private void setupGenderSpinner() {
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, genderOptions);
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genderSpinner.setAdapter(genderAdapter);
    }

    private void setupCourseSpinner() {
        courseAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, courseNames);
        courseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        courseSpinner.setAdapter(courseAdapter);
    }

    private void setupDOBPicker() {
        dobEditText.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    (view, selectedYear, selectedMonth, selectedDay) ->
                            dobEditText.setText(String.format(Locale.getDefault(), "%02d/%02d/%d", selectedDay, selectedMonth + 1, selectedYear)),
                    year, month, day);
            datePickerDialog.show();
        });
    }

    private void fetchCoursesFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("courses")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        courseNames.clear();
                        courseIDs.clear();
                        courseDepartments.clear();

                        for (DocumentSnapshot document : task.getResult()) {
                            String courseName = document.getString("courseName");
                            String courseID = document.getString("courseId");
                            String departmentID = document.getString("depid");

                            if (courseName != null && courseID != null && departmentID != null) {
                                courseNames.add(courseName);
                                courseIDs.add(courseID);
                                courseDepartments.add(departmentID);
                            }
                        }

                        courseNames.add(0, "Select Course");
                        courseAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(this, "Failed to fetch courses", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void registerUser() {
        String fullName = fullNameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String dob = dobEditText.getText().toString().trim();
        String parentName = parentsNameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString();
        String confirmPassword = confirmPasswordEditText.getText().toString();
        String gender = genderSpinner.getSelectedItem().toString();
        String course = courseSpinner.getSelectedItem().toString();

        if (fullName.isEmpty() || email.isEmpty() || dob.isEmpty() || parentName.isEmpty()
                || password.isEmpty() || confirmPassword.isEmpty()
                || gender.equals("Select Gender") || course.equals("Select Course")) {
            Toast.makeText(this, "Please fill all fields correctly", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        int regYear = Calendar.getInstance().get(Calendar.YEAR) % 100; // e.g., 25 for 2025

        String selectedCourseID = courseIDs.get(courseSpinner.getSelectedItemPosition() - 1);
        String selectedDepartmentID = courseDepartments.get(courseSpinner.getSelectedItemPosition() - 1);

        // Move to DummyPaymentActivity, passing all details except studentId
        Intent intent = new Intent(RegisterActivity.this, DummyPaymentActivity.class);
        intent.putExtra("fullName", fullName);
        intent.putExtra("email", email);
        intent.putExtra("dob", dob);
        intent.putExtra("gender", gender);
        intent.putExtra("parentName", parentName);
        intent.putExtra("cid", selectedCourseID);
        intent.putExtra("courseName", course);
        intent.putExtra("depid", selectedDepartmentID);
        intent.putExtra("password", password);
        intent.putExtra("regYear", regYear);
        startActivity(intent);
    }
}
