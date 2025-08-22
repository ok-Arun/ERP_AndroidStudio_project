package com.example.final3o.Teacher;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.final3o.LoginActivity;
import com.example.final3o.R;
import com.example.final3o.Teacher.Attendance.TeacherAttendanceActivity;
import com.example.final3o.Teacher.Grading.GradingActivity;
//import com.example.final3o.Teacher.Students.ViewStudentsActivity;
import com.example.final3o.Teacher.Students.ApprovalActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

public class TeacherDashboardActivity extends AppCompatActivity {

    TextView teacherWelcomeText, teacherIdText;
    Button attendanceManagementButton, gradingButton, viewStudentsButton,logoutButton;

    FirebaseAuth mAuth;
    FirebaseFirestore db;
    ListenerRegistration registration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_dashboard);

        // Initialize views
        teacherWelcomeText = findViewById(R.id.teacherWelcomeText);
        teacherIdText = findViewById(R.id.teacherIdText);
        attendanceManagementButton = findViewById(R.id.attendanceManagementButton);
        gradingButton = findViewById(R.id.gradingButton);
        viewStudentsButton = findViewById(R.id.viewStudentsButton);
        logoutButton = findViewById(R.id.logoutButton);

        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Get the current teacher's UID from FirebaseAuth (UID is unique to Firebase Authentication)
        String teacherUid = mAuth.getCurrentUser().getUid();

        // Query the teachers collection by teacherUid (assuming the teacher's UID is stored as 'uid' in Firestore)
        db.collection("teachers")
                .whereEqualTo("uid", teacherUid)  // Match the 'uid' field in Firestore
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            // There should be only one document, as uid is unique
                            DocumentSnapshot document = querySnapshot.getDocuments().get(0);

                            // Retrieve the teacher's name and teacherId from the document fields
                            String teacherName = document.getString("name");
                            String teacherId = document.getString("teacherId");

                            // Display teacher's name and teacher ID
                            teacherWelcomeText.setText("Welcome, " + teacherName);
                            teacherIdText.setText("Teacher ID: " + teacherId);
                        } else {
                            // Handle the case where no document was found
                            Log.w("TeacherDashboard", "No teacher found for the UID");
                            teacherWelcomeText.setText("Welcome, Teacher");
                            teacherIdText.setText("Teacher ID: N/A");
                        }
                    } else {
                        // Handle errors
                        Log.e("TeacherDashboard", "Error fetching teacher data", task.getException());
                    }
                });

        // Set onClick listeners for each button (e.g., Attendance Management, Grading, etc.)
        attendanceManagementButton.setOnClickListener(v -> {
            startActivity(new Intent(TeacherDashboardActivity.this, TeacherAttendanceActivity.class));
        });

        gradingButton.setOnClickListener(v -> {
            startActivity(new Intent(TeacherDashboardActivity.this, GradingActivity.class));
        });
        viewStudentsButton.setOnClickListener(v -> {
            startActivity(new Intent(TeacherDashboardActivity.this, ApprovalActivity.class));
        });
        logoutButton.setOnClickListener(v -> {
            startActivity(new Intent(TeacherDashboardActivity.this, LoginActivity.class));
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Remove Firestore listener to avoid memory leaks
        if (registration != null) {
            registration.remove();
        }
    }
}
