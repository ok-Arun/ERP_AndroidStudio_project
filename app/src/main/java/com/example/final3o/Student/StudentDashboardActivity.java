package com.example.final3o.Student;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.final3o.LoginActivity;
import com.example.final3o.R;
import com.example.final3o.Student.Attendance.AttendanceActivity;
import com.example.final3o.Student.Edit.EditProfileActivity;
import com.example.final3o.Student.Marks.MarksActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class StudentDashboardActivity extends AppCompatActivity {

    private TextView studentName, studentId, attendancePercentage, marksPercentage;
    private Button editButton, viewAttendanceButton, marksButton, logoutButton;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String currentStudentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_dashboard);

        // Initialize Views
        studentName = findViewById(R.id.studentName);
        studentId = findViewById(R.id.studentId);
        attendancePercentage = findViewById(R.id.attendancePercentage);
        marksPercentage = findViewById(R.id.marksPercentage);
        editButton = findViewById(R.id.editButton);
        viewAttendanceButton = findViewById(R.id.viewAttendanceButton);
        marksButton = findViewById(R.id.marksButton);
        logoutButton = findViewById(R.id.logoutButton);  // Initialize logout button

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Fetch current student details
        fetchStudentDetails();

        TextView marqueeText = findViewById(R.id.marqueeText);
        marqueeText.setSelected(true);  // Important to start the scrolling effect

        // Button Listeners
        editButton.setOnClickListener(v -> {
            Intent intent = new Intent(StudentDashboardActivity.this, EditProfileActivity.class);
            intent.putExtra("studentId", currentStudentId);
            startActivity(intent);
        });

        viewAttendanceButton.setOnClickListener(v -> {
            Intent intent = new Intent(StudentDashboardActivity.this, AttendanceActivity.class);
            intent.putExtra("studentId", currentStudentId);
            startActivity(intent);
        });

        marksButton.setOnClickListener(v -> {
            Intent intent = new Intent(StudentDashboardActivity.this, MarksActivity.class);
            intent.putExtra("studentId", currentStudentId);
            startActivity(intent);
        });



        // Logout Button Listener
        logoutButton.setOnClickListener(v -> {
            auth.signOut();
            Toast.makeText(StudentDashboardActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(StudentDashboardActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear back stack
            startActivity(intent);
            finish();
        });
    }

    private void fetchStudentDetails() {
        String userEmail = auth.getCurrentUser().getEmail();

        if (userEmail == null) {
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show();
            return;
        }

        CollectionReference studentsRef = db.collection("students");
        studentsRef.whereEqualTo("email", userEmail)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot studentDoc = queryDocumentSnapshots.getDocuments().get(0);

                        String fullName = studentDoc.getString("fullName");
                        currentStudentId = studentDoc.getString("studentId");
                        studentName.setText(fullName);
                        studentId.setText("Student ID: " + currentStudentId.toUpperCase());

                        fetchAttendance();
                        fetchMarks();
                    } else {
                        Toast.makeText(this, "Student data not found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load student data.", Toast.LENGTH_SHORT).show());
    }

    private void fetchAttendance() {
        CollectionReference attendanceRef = db.collection("allAttendance");

        attendanceRef.whereEqualTo("studentId", currentStudentId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int totalClasses = 0;
                    int attendedClasses = 0;

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        totalClasses++;
                        String status = document.getString("attendanceStatus");

                        if (status != null && status.equalsIgnoreCase("Present")) {
                            attendedClasses++;
                        }
                    }

                    if (totalClasses > 0) {
                        int percentage = (attendedClasses * 100) / totalClasses;
                        attendancePercentage.setText(percentage + "%");
                    } else {
                        attendancePercentage.setText("N/A");
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load attendance.", Toast.LENGTH_SHORT).show());
    }

    private void fetchMarks() {
        CollectionReference gradesRef = db.collection("all_grades");

        gradesRef.whereEqualTo("studentId", currentStudentId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int totalMarks = 0;
                    int paperCount = 0;

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Long marks = document.getLong("marks");
                        if (marks != null) {
                            totalMarks += marks;
                            paperCount++;
                        }
                    }

                    if (paperCount > 0) {
                        int averageMarks = totalMarks / paperCount;
                        marksPercentage.setText(averageMarks + "%");
                    } else {
                        marksPercentage.setText("N/A");
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load marks.", Toast.LENGTH_SHORT).show());
    }
}
