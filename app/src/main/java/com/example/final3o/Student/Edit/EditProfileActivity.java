package com.example.final3o.Student.Edit;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.final3o.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private EditText fullNameInput, phoneNumberInput, addressInput, dobInput, parentNameInput, semesterInput;
    private Spinner genderSpinner, courseNameSpinner;
    private Button saveButton;
    private String studentId;  // Using studentId instead of Firebase UID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        studentId = getIntent().getStringExtra("studentId");  // Get the studentId passed from previous screen

        // Initialize UI elements
        fullNameInput = findViewById(R.id.fullNameInput);
        phoneNumberInput = findViewById(R.id.phoneNumberInput);
        addressInput = findViewById(R.id.addressInput);
        dobInput = findViewById(R.id.dobInput);
        parentNameInput = findViewById(R.id.parentNameInput);
        semesterInput = findViewById(R.id.semesterInput);
        genderSpinner = findViewById(R.id.genderSpinner);
        courseNameSpinner = findViewById(R.id.courseNameSpinner);
        saveButton = findViewById(R.id.saveButton);

        // Fetch data from Firestore
        fetchUserData();

        // Save Button Click Listener
        saveButton.setOnClickListener(v -> {
            showConfirmationDialog();
        });
    }

    private void fetchUserData() {
        // Fetch student data from Firestore using studentId
        DocumentReference userRef = db.collection("students").document(studentId);
        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    // Auto-fill the fields with the fetched data
                    String fullName = document.getString("fullName");
                    String phoneNumber = document.getString("phoneNumber");
                    String address = document.getString("address");
                    String dob = document.getString("dob");
                    String parentName = document.getString("parentName");
                    String gender = document.getString("gender");
                    String course = document.getString("courseName");
                    String semester = document.getString("semester");
                    String status = document.getString("status"); // Fetch the status field

                    fullNameInput.setText(fullName);
                    phoneNumberInput.setText(phoneNumber);
                    addressInput.setText(address);
                    dobInput.setText(dob);
                    parentNameInput.setText(parentName);
                    semesterInput.setText(semester);

                    // Set Gender Spinner
                    ArrayAdapter<CharSequence> genderAdapter = ArrayAdapter.createFromResource(
                            this, R.array.gender_options, android.R.layout.simple_spinner_item);
                    genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    genderSpinner.setAdapter(genderAdapter);
                    if (gender != null) {
                        int genderPosition = genderAdapter.getPosition(gender);
                        genderSpinner.setSelection(genderPosition);
                    }

                    // Fetch courses from Firestore and populate the courseNameSpinner
                    fetchCourses(course);  // Pass the current course for setting default selection

                    // Check the status and show the marquee if it's pending
                    if ("pending".equals(status)) {
                        TextView marqueeTextView = findViewById(R.id.marqueeTextView);
                        marqueeTextView.setVisibility(View.VISIBLE);  // Show marquee if the status is "pending"
                    } else {
                        TextView marqueeTextView = findViewById(R.id.marqueeTextView);
                        marqueeTextView.setVisibility(View.GONE);  // Hide marquee if the status is not "pending"
                    }
                }
            }
        });
    }


    private void fetchCourses(String currentCourse) {
        // Reference to the "courses" collection in Firestore
        db.collection("courses").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // List to hold course names
                ArrayList<String> courseList = new ArrayList<>();

                for (DocumentSnapshot document : task.getResult()) {
                    String courseName = document.getString("courseName");  // Assuming the field is "courseName"
                    if (courseName != null) {
                        courseList.add(courseName);
                    }
                }

                // Populate the courseNameSpinner with the fetched courses
                ArrayAdapter<String> courseAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, courseList);
                courseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                courseNameSpinner.setAdapter(courseAdapter);

                // Set the selected course (if available) in the spinner
                if (currentCourse != null) {
                    int coursePosition = courseAdapter.getPosition(currentCourse);
                    courseNameSpinner.setSelection(coursePosition);
                }
            } else {
                Toast.makeText(this, "Failed to fetch courses", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to save changes?")
                .setPositiveButton("Yes", (dialog, which) -> saveChanges())
                .setNegativeButton("No", null)
                .show();
    }

    private void saveChanges() {
        // Collect new data
        String fullName = fullNameInput.getText().toString();
        String phoneNumber = phoneNumberInput.getText().toString();
        String address = addressInput.getText().toString();
        String dob = dobInput.getText().toString();
        String parentName = parentNameInput.getText().toString();
        String gender = genderSpinner.getSelectedItem().toString();
        String courseName = courseNameSpinner.getSelectedItem().toString();
        String semester = semesterInput.getText().toString();

        // Create a HashMap for the updated user data
        Map<String, Object> updatedData = new HashMap<>();
        updatedData.put("fullName", fullName);
        updatedData.put("phoneNumber", phoneNumber);
        updatedData.put("address", address);
        updatedData.put("dob", dob);
        updatedData.put("parentName", parentName);
        updatedData.put("gender", gender);
        updatedData.put("courseName", courseName);
        updatedData.put("semester", semester);
        updatedData.put("studentId", studentId);
        updatedData.put("status", "pending");  // Status: Pending approval

        // Send the updated data to Firestore (for approval process)
        db.collection("approval_requests").document(studentId)
                .set(updatedData)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Display the marquee message indicating the request is under processing
                        Toast.makeText(this, "Request Sent for Approval", Toast.LENGTH_SHORT).show();

                        // After sending the request, we can check for status again by fetching the user data
                        fetchUserData(); // Re-fetch data to ensure the marquee is displayed correctly
                    } else {
                        Toast.makeText(this, "Failed to send request", Toast.LENGTH_SHORT).show();
                    }
                });
    }


}
