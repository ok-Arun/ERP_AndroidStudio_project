package com.example.final3o.Student.Attendance;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.final3o.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AttendanceActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private Spinner paperSpinner;
    private Button fetchAttendanceButton;
    private RecyclerView attendanceRecyclerView;
    private StudAdapter studAdapter;
    private List<StudModel> attendanceList;

    private TextView totalClassesTextView, presentCountTextView, absentCountTextView,percentageTextView;
    private LinearLayout countLayout,countLayout1;
    private String studentId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance);

        db = FirebaseFirestore.getInstance();
        paperSpinner = findViewById(R.id.paperSpinner);
        fetchAttendanceButton = findViewById(R.id.fetchAttendanceButton);
        attendanceRecyclerView = findViewById(R.id.attendanceRecyclerView);

        attendanceList = new ArrayList<>();
        studAdapter = new StudAdapter(attendanceList);
        attendanceRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        attendanceRecyclerView.setAdapter(studAdapter);

        totalClassesTextView = findViewById(R.id.totalClassesTextView);
        presentCountTextView = findViewById(R.id.presentCountTextView);
        absentCountTextView = findViewById(R.id.absentCountTextView);
        percentageTextView = findViewById(R.id.percentageTextView);
        countLayout = findViewById(R.id.countLayout);
        countLayout1 = findViewById(R.id.countLayout1);

        studentId = getIntent().getStringExtra("studentId");


        fetchAttendanceButton.setOnClickListener(v -> fetchAttendance());

        String studentId = getIntent().getStringExtra("studentId");

        if (studentId != null) {
            fetchCourseAndPapers(studentId);
        } else {
            Toast.makeText(this, "Student ID not found!", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchCourseAndPapers(String studentId) {
        db.collection("students")
                .document(studentId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            String courseName = document.getString("courseName");
                            String semester = document.getString("semester");

                            if (courseName != null && semester != null) {
                                fetchPapers(courseName, semester);
                            }
                        }
                    } else {
                        Toast.makeText(AttendanceActivity.this, "Error fetching student data", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchPapers(String courseName, String semester) {
        db.collection("papers")
                .whereEqualTo("semester", semester)
                .whereEqualTo("courseName", courseName)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ArrayList<String> paperList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String paper = document.getString("paperName");
                            if (paper != null) {
                                paperList.add(paper);
                            }
                        }
                        populateSpinner(paperList);
                    } else {
                        Toast.makeText(AttendanceActivity.this, "Error fetching papers", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void populateSpinner(ArrayList<String> paperList) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, paperList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        paperSpinner.setAdapter(adapter);
    }

    private void fetchAttendance() {
        if (paperSpinner.getSelectedItem() == null) {
            Toast.makeText(this, "Please select a paper", Toast.LENGTH_SHORT).show();
            return;
        }

        String paperName = paperSpinner.getSelectedItem().toString();
        db.collection("allAttendance")
                .whereEqualTo("paperName", paperName)
                .whereEqualTo("studentId", studentId) // Filter by studentId too
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        attendanceList.clear();
                        int presentCount = 0;
                        int absentCount = 0;

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String date = document.getString("date");
                            String status = document.getString("attendanceStatus");

                            StudModel studModel = new StudModel(paperName, date, status);
                            attendanceList.add(studModel);

                            if ("Present".equalsIgnoreCase(status)) {
                                presentCount++;
                            } else if ("Absent".equalsIgnoreCase(status)) {
                                absentCount++;
                            }
                        }

                        // Sort by date
                        Collections.sort(attendanceList, Comparator.comparing(StudModel::getDate));
                        studAdapter.notifyDataSetChanged();

                        if (attendanceList.isEmpty()) {
                            Toast.makeText(AttendanceActivity.this, "No attendance records found.", Toast.LENGTH_SHORT).show();
                            countLayout.setVisibility(View.GONE);
                        } else {
                            // Set counts
                            int totalClasses = presentCount + absentCount;
                            totalClassesTextView.setText("Total: " + totalClasses);
                            presentCountTextView.setText("Present: " + presentCount);
                            absentCountTextView.setText("Absent: " + absentCount);

                            // Calculate and set percentage
                            if (totalClasses > 0) {
                                double percentage = (presentCount * 100.0) / totalClasses;
                                percentageTextView.setText(String.format("Percentage: %.2f%%", percentage));
                            } else {
                                percentageTextView.setText("Percentage: 0%");
                            }

                            countLayout.setVisibility(View.VISIBLE);
                        }


                    } else {
                        Toast.makeText(AttendanceActivity.this, "Error fetching attendance", Toast.LENGTH_SHORT).show();
                    }
                });
    }

}
