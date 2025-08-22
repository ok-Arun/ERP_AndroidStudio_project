package com.example.final3o.Student.Marks;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.final3o.R;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class MarksActivity extends AppCompatActivity {

    TextView tvStudentId, tvDepartment, tvCourse, tvNoData;
    Spinner spinnerSemester;
    RecyclerView recyclerMarks;

    String studentId, departmentId, courseId;
    MarksAdapter marksAdapter;
    ArrayList<MarksModel> marksList;

    FirebaseFirestore db;
    boolean isDataLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_marks);

        // Initialize views
        spinnerSemester = findViewById(R.id.spinner_semester);
        recyclerMarks = findViewById(R.id.recycler_marks);
        tvStudentId = findViewById(R.id.tv_student_id);
        tvDepartment = findViewById(R.id.tv_department);
        tvCourse = findViewById(R.id.tv_course);
        tvNoData = findViewById(R.id.tv_no_data);

        // Firestore init
        db = FirebaseFirestore.getInstance();

        // Recycler setup
        marksList = new ArrayList<>();
        marksAdapter = new MarksAdapter(marksList);
        recyclerMarks.setLayoutManager(new LinearLayoutManager(this));
        recyclerMarks.setAdapter(marksAdapter);

        // Begin loading data
        fetchStudentDetailsAndSetupSemester();
    }

    private void fetchStudentDetailsAndSetupSemester() {
        studentId = getIntent().getStringExtra("studentId");

        if (studentId == null || studentId.isEmpty()) {
            Toast.makeText(this, "Student ID not provided.", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("all_grades")
                .whereEqualTo("studentId", studentId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            // Extract student data
                            departmentId = doc.getString("departmentId");
                            courseId = doc.getString("courseId");

                            // Set values to UI
                            tvStudentId.setText("Student ID: " + studentId);
                            tvDepartment.setText("Department: " + departmentId);
                            tvCourse.setText("Course: " + courseId);

                            isDataLoaded = true;
                            setupSemesterSpinner(); // Now you can set up spinner after loading
                            break;
                        }
                    } else {
                        Toast.makeText(this, "Student not found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error fetching student info.", Toast.LENGTH_SHORT).show();
                });
    }


    private void setupSemesterSpinner() {
        ArrayList<String> semesters = new ArrayList<>();
        semesters.add("Select Semester");
        for (int i = 1; i <= 6; i++) {
            semesters.add("Semester " + i);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, semesters);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSemester.setAdapter(adapter);

        spinnerSemester.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0 || !isDataLoaded) {
                    marksList.clear();
                    marksAdapter.notifyDataSetChanged();
                    tvNoData.setVisibility(View.GONE);
                    return;
                }

                String selectedSemester = "Semester " + position;
                tvNoData.setVisibility(View.GONE);
                fetchMarks(selectedSemester);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void fetchMarks(String semester) {
        marksList.clear();
        marksAdapter.notifyDataSetChanged();
        tvNoData.setVisibility(View.GONE);

        CollectionReference gradesRef = db.collection("all_grades");

        gradesRef.whereEqualTo("departmentId", departmentId)
                .whereEqualTo("courseId", courseId)
                .whereEqualTo("studentId", studentId)
                .whereEqualTo("semester", semester)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            String paper = doc.getString("paperId");
                            Long marks = doc.getLong("marks");
                            String time = doc.getString("time");

                            if (paper != null && marks != null) {
                                marksList.add(new MarksModel(paper, marks.intValue(), time));
                            }
                        }

                        if (marksList.isEmpty()) {
                            tvNoData.setVisibility(View.VISIBLE);
                        } else {
                            tvNoData.setVisibility(View.GONE);
                        }

                        marksAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(MarksActivity.this, "Failed to fetch marks.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
