package com.example.final3o.Teacher.Attendance;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.final3o.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Calendar;

public class TeacherAttendanceActivity extends AppCompatActivity {

    private Spinner spinnerDepartment, spinnerCourse, spinnerPaper;
    private RecyclerView recyclerViewStudents;
    private Button btnSubmitAttendance, btnSelectDate;
    private TextView txtSelectedDate;

    private ArrayAdapter<String> deptAdapter, courseAdapter, paperAdapter;
    private List<String> departments = new ArrayList<>();
    private List<String> courses = new ArrayList<>();
    private List<String> papers = new ArrayList<>();
    private List<Student> studentList = new ArrayList<>();
    private StudentAdapter studentAdapter;
    private String selectedSemester;
    private String deptName = "";
    private String studentName = "";


    private String selectedDepartment, selectedCourse, selectedPaper, selectedDate;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_attendance);

        firestore = FirebaseFirestore.getInstance();

        spinnerDepartment = findViewById(R.id.spinnerDepartment);
        spinnerCourse = findViewById(R.id.spinnerCourse);
        spinnerPaper = findViewById(R.id.spinnerPaper);
        recyclerViewStudents = findViewById(R.id.recyclerViewStudents);
        btnSubmitAttendance = findViewById(R.id.btnSubmitAttendance);
        btnSelectDate = findViewById(R.id.btnSelectDate);
        txtSelectedDate = findViewById(R.id.txtSelectedDate);

        recyclerViewStudents.setLayoutManager(new LinearLayoutManager(this));
        studentAdapter = new StudentAdapter(studentList);
        recyclerViewStudents.setAdapter(studentAdapter);

        fetchDepartments();

        spinnerDepartment.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedDepartment = departments.get(position);
                fetchCourses(selectedDepartment);
            }

            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerCourse.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCourse = courses.get(position);
                fetchPapersForTeacher(selectedCourse);  // Now you can fetch papers for the selected course
            }

            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerPaper.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedPaper = papers.get(position);
                fetchStudents(selectedCourse, selectedSemester);
            }

            public void onNothingSelected(AdapterView<?> parent) {}
        });

        btnSubmitAttendance.setOnClickListener(v -> submitAttendance());

        // Date selection button click
        btnSelectDate.setOnClickListener(v -> showDatePickerDialog());
    }

    private void fetchDepartments() {
        firestore.collection("departments").get()
                .addOnSuccessListener(snapshot -> {
                    departments.clear();
                    for (DocumentSnapshot doc : snapshot) {
                        departments.add(doc.getString("depid"));
                    }
                    deptAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, departments);
                    deptAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerDepartment.setAdapter(deptAdapter);
                });
    }

    private void fetchCourses(String department) {
        firestore.collection("courses").whereEqualTo("depid", department).get()
                .addOnSuccessListener(snapshot -> {
                    courses.clear();
                    for (DocumentSnapshot doc : snapshot) {
                        courses.add(doc.getString("courseId"));
                        deptName = doc.getString("departmentName"); // Save department name when fetching courses
                    }
                    courseAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, courses);
                    courseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerCourse.setAdapter(courseAdapter);
                })
                .addOnFailureListener(e -> {
                    Log.e("TeacherFetch", "Failed to fetch courses", e);
                });
    }

    private void fetchPapersForTeacher(String courseId) {
        String currentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Step 1: Fetch teacherId from 'teachers' collection using the current user's UID
        firestore.collection("teachers")
                .whereEqualTo("uid", currentUserUid) // Find the logged-in teacher's record
                .get()
                .addOnSuccessListener(teacherSnapshot -> {
                    if (!teacherSnapshot.isEmpty()) {
                        DocumentSnapshot teacherDoc = teacherSnapshot.getDocuments().get(0);
                        String teacherId = teacherDoc.getString("teacherId");

                        if (teacherId != null) {
                            // Step 2: Use the teacherId to fetch relevant papers for the selected course
                            fetchPapers(courseId, teacherId);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("TeacherFetch", "Failed to get teacher ID", e);
                });
    }

    private void fetchPapers(String courseId, String teacherId) {
        firestore.collection("papers")
                .whereEqualTo("courseId", courseId)  // Filter by courseId
                .whereEqualTo("deptId", selectedDepartment)  // Filter by department ID
                .whereEqualTo("teacherId", teacherId)  // Filter by teacherId
                .get()
                .addOnSuccessListener(snapshot -> {
                    papers.clear();
                    for (DocumentSnapshot doc : snapshot) {
                        // Assuming 'paperName' and 'semester' are fields in the 'papers' collection
                        String paperName = doc.getString("paperName");
                        String semester = doc.getString("semester");

                        if (paperName != null) {
                            papers.add(paperName);
                        }

                        if (semester != null) {
                            selectedSemester = semester;  // Store the semester for use when fetching students
                        }
                    }
                    paperAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, papers);
                    paperAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerPaper.setAdapter(paperAdapter);
                })
                .addOnFailureListener(e -> {
                    Log.e("TeacherFetch", "Failed to fetch papers", e);
                });
    }

    private void fetchStudents(String courseCid, String semester) {
        firestore.collection("students")
                .whereEqualTo("cid", courseCid)
                .whereEqualTo("depid", selectedDepartment)
                .whereEqualTo("userStatus", "active")
                .whereEqualTo("semester", semester)
                .get()
                .addOnSuccessListener(snapshot -> {
                    studentList.clear();
                    for (DocumentSnapshot doc : snapshot) {
                        String studentName = doc.getString("fullName");
                        String studentId = doc.getString("studentId");

                        if (studentName != null && studentId != null) {
                            studentList.add(new Student(studentId, studentName, studentId, "Absent", deptName, studentName));
                        }
                    }
                    studentAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e("FetchStudents", "Failed to fetch students", e);
                });
    }



    private void submitAttendance() {
        if (selectedDate == null || selectedDate.isEmpty()) {
            Toast.makeText(this, "Please select a date first.", Toast.LENGTH_SHORT).show();
            return;
        }

        String currentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        firestore.collection("teachers").whereEqualTo("uid", currentUserUid).get()
                .addOnSuccessListener(teacherSnapshot -> {
                    if (teacherSnapshot.isEmpty()) {
                        Toast.makeText(this, "Teacher not found.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    DocumentSnapshot teacherDoc = teacherSnapshot.getDocuments().get(0);
                    String teacherName = teacherDoc.getString("name");
                    String teacherId = teacherDoc.getString("teacherId");

                    firestore.collection("courses").whereEqualTo("courseId", selectedCourse).get()
                            .addOnSuccessListener(courseSnapshot -> {
                                        if (courseSnapshot.isEmpty()) {
                                            Toast.makeText(this, "Course not found.", Toast.LENGTH_SHORT).show();
                                            return;
                                        }

                                        DocumentSnapshot courseDoc = courseSnapshot.getDocuments().get(0);
                                        String courseName = courseDoc.getString("courseName");
                                        String courseId = courseDoc.getString("courseId");
                                        String deptId = courseDoc.getString("depid");

                            firestore.collection("departments").document(deptId).get()
                                    .addOnSuccessListener(deptDoc -> {
                                        String deptName = deptDoc.exists() ? deptDoc.getString("departmentName") : "Unknown";

                                    firestore.collection("papers")
                                            .whereEqualTo("paperName", selectedPaper)
                                            .get()
                                            .addOnSuccessListener(paperSnapshot -> {
                                                if (paperSnapshot.isEmpty()) {
                                                    Toast.makeText(this, "Paper not found.", Toast.LENGTH_SHORT).show();
                                                    return;
                                                }

                                                DocumentSnapshot paperDoc = paperSnapshot.getDocuments().get(0);
                                                String paperCode = paperDoc.getString("paperCode");

                                                if (studentList == null || studentList.isEmpty()) {
                                                    Toast.makeText(this, "No students to submit attendance.", Toast.LENGTH_SHORT).show();
                                                    return;
                                                }

                                for (Student student : studentList) {
                                    String studentId = student.getStudentId();
                                    String studentName = student.getStudentName();
                                    String status = student.getAttendanceStatus();

                                    Log.d("DEBUG", "Processing: " + studentId + " | Status: " + status);

                                    DocumentReference attendanceDocRef = firestore.collection("attendance")
                                            .document(deptId)
                                            .collection(courseId)
                                            .document(selectedPaper)
                                            .collection(selectedDate)
                                            .document(studentId);

                                    Map<String, Object> attendanceData = new HashMap<>();
                                    attendanceData.put("status", status);

                                    attendanceDocRef.set(attendanceData)
                                            .addOnSuccessListener(unused -> Log.d("Attendance", "Saved for: " + studentId))
                                            .addOnFailureListener(e -> Log.e("Attendance", "Failed to save for: " + studentId, e));

                                    // Save to allAttendance
                                    Map<String, Object> allAttendanceData = new HashMap<>();
                                    allAttendanceData.put("teacherId", teacherId);
                                    allAttendanceData.put("teacherName", teacherName);
                                    allAttendanceData.put("courseName", courseName);
                                    allAttendanceData.put("courseId", courseId);
                                    allAttendanceData.put("deptName", deptName);
                                    allAttendanceData.put("deptId", deptId);
                                    allAttendanceData.put("paperName", selectedPaper);
                                    allAttendanceData.put("paperCode", paperCode);
                                    allAttendanceData.put("date", selectedDate);
                                    allAttendanceData.put("studentId", studentId);
                                    allAttendanceData.put("studentName", studentName);
                                    allAttendanceData.put("attendanceStatus", status);

                                    firestore.collection("allAttendance")
                                            .whereEqualTo("studentId", studentId)
                                            .whereEqualTo("date", selectedDate)
                                            .whereEqualTo("paperName", selectedPaper)
                                            .get()
                                            .addOnSuccessListener(querySnapshot -> {
                                                if (!querySnapshot.isEmpty()) {
                                                    DocumentReference docRef = querySnapshot.getDocuments().get(0).getReference();
                                                    docRef.update(allAttendanceData)
                                                            .addOnSuccessListener(aVoid -> Log.d("AllAttendance", "Updated for: " + studentId))
                                                            .addOnFailureListener(e -> Log.e("AllAttendance", "Update failed for: " + studentId, e));
                                                } else {
                                                    firestore.collection("allAttendance").add(allAttendanceData)
                                                            .addOnSuccessListener(documentReference -> Log.d("AllAttendance", "Added for: " + studentId))
                                                            .addOnFailureListener(e -> Log.e("AllAttendance", "Add failed for: " + studentId, e));
                                                }
                                            })
                                            .addOnFailureListener(e -> Log.e("AllAttendance", "Query failed for: " + studentId, e));
                                }

                                Toast.makeText(this, "Attendance submitted successfully.", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Log.e("CourseFetch", "Course fetch failed", e);
                                Toast.makeText(this, "Failed to fetch course", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e("TeacherFetch", "Teacher fetch failed", e);
                    Toast.makeText(this, "Failed to fetch teacher", Toast.LENGTH_SHORT).show();
                });
        });
    });
}

    private void fetchExistingAttendanceAndUpdateUI(String selectedDate) {
        if (selectedCourse == null || selectedPaper == null || selectedDepartment == null) {
            Toast.makeText(this, "Please select department, course and paper first.", Toast.LENGTH_SHORT).show();
            return;
        }

        firestore.collection("attendance")
                .document(selectedDepartment)
                .collection(selectedCourse)
                .document(selectedPaper)
                .collection(selectedDate)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    Map<String, String> existingAttendanceMap = new HashMap<>();

                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        String studentId = doc.getId();
                        String status = doc.getString("status");
                        existingAttendanceMap.put(studentId, status);
                    }


                    for (Student student : studentList) {
                        String status = existingAttendanceMap.get(student.getStudentId());
                        if ("Present".equals(status)) {
                            student.setPresent(true); // Check the box
                        } else {
                            student.setPresent(false); // Unchecked
                        }
                    }

                    studentAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e("Attendance", "Failed to fetch existing attendance", e);
                });
    }


    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year1, month1, dayOfMonth) -> {
            selectedDate = year1 + "-" + (month1 + 1) + "-" + dayOfMonth; // Format: YYYY-MM-DD
            txtSelectedDate.setText("Selected Date: " + selectedDate);

            fetchExistingAttendanceAndUpdateUI(selectedDate);
        }, year, month, day);

        datePickerDialog.show();
    }
}
