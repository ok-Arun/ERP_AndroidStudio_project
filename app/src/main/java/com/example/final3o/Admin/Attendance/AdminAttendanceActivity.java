package com.example.final3o.Admin.Attendance;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.final3o.R;
import com.google.firebase.firestore.*;

import java.text.SimpleDateFormat;
import java.util.*;

public class AdminAttendanceActivity extends AppCompatActivity {

    Spinner spinnerDepartment, spinnerCourse, spinnerSemester, spinnerPaper;
    TextView textViewDate;
    Button buttonFetch;
    RecyclerView recyclerView;

    FirebaseFirestore db;
    String selectedDate = "";

    AttendanceAdapter adapter;
    List<AttendanceModel> attendanceList = new ArrayList<>();
    private List<String> departments = new ArrayList<>();
    private List<String> courses = new ArrayList<>();
    private ArrayAdapter<String> deptAdapter;
    private ArrayAdapter<String> courseAdapter;
    private ArrayAdapter<String> paperAdapter;

    // Store selected values here
    private String selectedCourse = "";
    private String selectedSemester = "";
    private String selectedPaper = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_attendance);

        spinnerDepartment = findViewById(R.id.spinnerDepartment);
        spinnerCourse = findViewById(R.id.spinnerCourse);
        spinnerSemester = findViewById(R.id.spinnerSemester);
        spinnerPaper = findViewById(R.id.spinnerPaper);
        textViewDate = findViewById(R.id.textViewDate);
        buttonFetch = findViewById(R.id.buttonFetchAttendance);
        recyclerView = findViewById(R.id.recyclerViewAttendance);

        db = FirebaseFirestore.getInstance();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AttendanceAdapter(attendanceList);
        recyclerView.setAdapter(adapter);

        setupSpinners();
        setupDatePicker();

        buttonFetch.setOnClickListener(v -> fetchAttendance());

        // Listen to department spinner
        spinnerDepartment.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedDepartment = departments.get(position);
                if (!selectedDepartment.equals("Select Department")) {
                    loadCourses(selectedDepartment);
                } else {
                    courses.clear();
                    courseAdapter = new ArrayAdapter<>(AdminAttendanceActivity.this, android.R.layout.simple_spinner_item, courses);
                    courseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerCourse.setAdapter(courseAdapter);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        // Listen to course spinner
        spinnerCourse.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCourse = courses.get(position); // Store selected course
                if (!selectedCourse.equals("Select Course") && !selectedSemester.equals("Select Semester")) {
                    loadPapers(selectedCourse, selectedSemester); // Call load papers with selected values
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        // Listen to semester spinner
        spinnerSemester.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedSemester = parent.getItemAtPosition(position).toString(); // Store selected semester
                if (!selectedCourse.equals("Select Course") && !selectedSemester.equals("Select Semester")) {
                    loadPapers(selectedCourse, selectedSemester); // Call load papers with selected values
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        // Listen to paper spinner
        spinnerPaper.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedPaper = parent.getItemAtPosition(position).toString(); // Store selected paper
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private void setupSpinners() {
        loadDepartments();
        loadSemesters(); // If semesters are constant like 1-6, we can keep hardcoded
    }

    private void loadDepartments() {
        db.collection("departments")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    departments.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        departments.add(doc.getString("depid"));
                    }
                    deptAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, departments);
                    deptAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerDepartment.setAdapter(deptAdapter);
                });
    }

    private void loadCourses(String depid) {
        db.collection("courses")
                .whereEqualTo("depid", depid)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    courses.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        courses.add(doc.getString("courseId"));
                    }
                    courseAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, courses);
                    courseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerCourse.setAdapter(courseAdapter);
                });
    }

    private void loadSemesters() {
        List<String> semesters = Arrays.asList("Semester 1", "Semester 2", "Semester 3", "Semester 4", "Semester 5", "Semester 6");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, semesters);
        spinnerSemester.setAdapter(adapter);
    }

    private void loadPapers(String courseId, String semester) {
        db.collection("papers")
                .whereEqualTo("courseId", courseId)
                .whereEqualTo("semester", semester)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> papers = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        String paperName = doc.getString("paperName");
                        if (paperName != null) {
                            papers.add(paperName);
                        }
                    }
                    paperAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, papers);
                    spinnerPaper.setAdapter(paperAdapter);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AdminAttendanceActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void setupDatePicker() {
        textViewDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            new DatePickerDialog(AdminAttendanceActivity.this, (view, year, month, dayOfMonth) -> {
                calendar.set(year, month, dayOfMonth);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-dd", Locale.getDefault());
                selectedDate = sdf.format(calendar.getTime());
                textViewDate.setText(selectedDate);
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
        });
    }

    private void fetchAttendance() {
        String dep = spinnerDepartment.getSelectedItem().toString();
        String course = spinnerCourse.getSelectedItem().toString();
        String sem = spinnerSemester.getSelectedItem().toString();
        String paper = spinnerPaper.getSelectedItem().toString();

        if (dep == null || course == null || sem == null || paper == null || selectedDate.isEmpty()) {
            return;
        }

        attendanceList.clear();

        db.collection("allAttendance")
                .whereEqualTo("deptId", dep)
                .whereEqualTo("courseId", course)
                .whereEqualTo("paperName", paper)
                .whereEqualTo("date", selectedDate)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        String name = doc.getString("studentName");
                        String studentId = doc.getString("studentId");
                        String attendanceStatus = doc.getString("attendanceStatus");

                        AttendanceModel model = new AttendanceModel(name, studentId, attendanceStatus);
                        attendanceList.add(model);
                    }
                    adapter.notifyDataSetChanged();
                    if (attendanceList.isEmpty()) {
                        Toast.makeText(this, "No attendance found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

}
