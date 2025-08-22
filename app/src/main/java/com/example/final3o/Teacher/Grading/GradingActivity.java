package com.example.final3o.Teacher.Grading;

import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.final3o.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class GradingActivity extends AppCompatActivity {
    private TextView semesterTextView;
    private Spinner deptSpinner, courseSpinner, paperSpinner;
    private RecyclerView studentRecyclerView;
    private FirebaseFirestore db;
    private String currentUserUid;
    private List<StudentModel> studentList;
    private StudentAdapter adapter;

    // Store selected values
    private String selectedDepartment, selectedCourse, selectedPaper, selectedSemester;
    private List<String> departments = new ArrayList<>();
    private List<String> courses = new ArrayList<>();
    private List<String> papers = new ArrayList<>();
    private List<String> paperIds = new ArrayList<>(); // Store paper IDs separately

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grading);

        deptSpinner = findViewById(R.id.departmentSpinner);
        courseSpinner = findViewById(R.id.courseSpinner);
        paperSpinner = findViewById(R.id.paperSpinner);
        semesterTextView = findViewById(R.id.semesterTextView);
        studentRecyclerView = findViewById(R.id.studentRecyclerView);

        db = FirebaseFirestore.getInstance();
        currentUserUid = FirebaseAuth.getInstance().getUid();

        studentList = new ArrayList<>();
        adapter = new StudentAdapter(studentList);
        studentRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        studentRecyclerView.setAdapter(adapter);

        setupSpinnerListeners();
        loadDepartments();
    }

    private void setupSpinnerListeners() {
        deptSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedDepartment = departments.get(position);
                loadCourses(selectedDepartment);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        courseSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCourse = courses.get(position);
                loadPapersForTeacher(selectedCourse);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        paperSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < papers.size() && position < paperIds.size()) {
                    selectedPaper = papers.get(position);
                    String selectedPaperId = paperIds.get(position);
                    Log.d("GradingFetch", "Paper selected: " + selectedPaper + " (ID: " + selectedPaperId + ")");

                    // Clear previous student data
                    studentList.clear();
                    adapter.notifyDataSetChanged();

                    // Load students with a small delay to ensure UI is updated
                    new android.os.Handler().postDelayed(() -> {
                        loadStudents(selectedCourse, selectedSemester, selectedPaperId);
                    }, 100);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void loadDepartments() {
        db.collection("departments").get()
                .addOnSuccessListener(snapshot -> {
                    departments.clear();
                    for (DocumentSnapshot doc : snapshot) {
                        departments.add(doc.getString("depid"));
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, departments);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    deptSpinner.setAdapter(adapter);
                })
                .addOnFailureListener(e -> {
                    Log.e("GradingFetch", "Failed to fetch departments", e);
                    Toast.makeText(this, "Failed to load departments", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadCourses(String department) {
        db.collection("courses").whereEqualTo("depid", department).get()
                .addOnSuccessListener(snapshot -> {
                    courses.clear();
                    for (DocumentSnapshot doc : snapshot) {
                        courses.add(doc.getString("courseId"));
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, courses);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    courseSpinner.setAdapter(adapter);
                })
                .addOnFailureListener(e -> {
                    Log.e("GradingFetch", "Failed to fetch courses", e);
                    Toast.makeText(this, "Failed to load courses", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadPapersForTeacher(String courseId) {
        // Step 1: Get teacher ID from current user UID (same as attendance system)
        db.collection("teachers")
                .whereEqualTo("uid", currentUserUid)
                .get()
                .addOnSuccessListener(teacherSnapshot -> {
                    if (!teacherSnapshot.isEmpty()) {
                        DocumentSnapshot teacherDoc = teacherSnapshot.getDocuments().get(0);
                        String teacherId = teacherDoc.getString("teacherId");

                        if (teacherId != null) {
                            // Step 2: Fetch papers assigned to this teacher for the selected course
                            fetchPapers(courseId, teacherId);
                        }
                    } else {
                        Toast.makeText(this, "Teacher not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("GradingFetch", "Failed to get teacher ID", e);
                    Toast.makeText(this, "Failed to get teacher information", Toast.LENGTH_SHORT).show();
                });
    }

    private void fetchPapers(String courseId, String teacherId) {
        db.collection("papers")
                .whereEqualTo("courseId", courseId)
                .whereEqualTo("deptId", selectedDepartment)
                .whereEqualTo("teacherId", teacherId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    papers.clear();
                    paperIds.clear();

                    Log.d("GradingFetch", "Found " + snapshot.size() + " papers for teacher");

                    for (DocumentSnapshot doc : snapshot) {
                        String paperName = doc.getString("paperName");
                        String semester = doc.getString("semester");
                        String paperId = doc.getId();

                        if (paperName != null) {
                            papers.add(paperName);
                            paperIds.add(paperId);
                            Log.d("GradingFetch", "Added paper: " + paperName + " (ID: " + paperId + ")");
                        }

                        if (semester != null) {
                            selectedSemester = semester;
                            semesterTextView.setText("Semester: " + semester);
                        }
                    }

                    if (papers.isEmpty()) {
                        Toast.makeText(this, "No papers assigned to you for this course", Toast.LENGTH_SHORT).show();
                        // Clear student list when no papers
                        studentList.clear();
                        adapter.notifyDataSetChanged();
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, papers);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    paperSpinner.setAdapter(adapter);
                })
                .addOnFailureListener(e -> {
                    Log.e("GradingFetch", "Failed to fetch papers", e);
                    Toast.makeText(this, "Failed to load papers", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadStudents(String courseId, String semester, String paperId) {
        if (courseId == null || semester == null || paperId == null) {
            Log.e("GradingFetch", "Missing required parameters for loading students");
            return;
        }

        Log.d("GradingFetch", "Loading students for: " + courseId + ", semester: " + semester + ", paper: " + paperId);

        // Fetch students exactly like in attendance system
        db.collection("students")
                .whereEqualTo("cid", courseId)
                .whereEqualTo("depid", selectedDepartment)
                .whereEqualTo("userStatus", "active")
                .whereEqualTo("semester", semester)
                .get()
                .addOnSuccessListener(snapshot -> {
                    studentList.clear();

                    List<DocumentSnapshot> studentDocs = snapshot.getDocuments();
                    final int totalStudents = studentDocs.size();
                    final AtomicInteger studentsProcessed = new AtomicInteger(0);

                    Log.d("GradingFetch", "Found " + totalStudents + " students");

                    if (totalStudents == 0) {
                        adapter.notifyDataSetChanged();
                        Toast.makeText(this, "No students found for this paper", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    for (DocumentSnapshot doc : studentDocs) {
                        String studentName = doc.getString("fullName");
                        String studentId = doc.getString("studentId");
                        String uid = doc.getId();

                        if (studentName != null && studentId != null) {
                            // Create student model with default marks (-1 means not assigned)
                            StudentModel studentModel = new StudentModel(uid, studentName, -1);
                            studentList.add(studentModel);

                            // Fetch existing marks for this student and paper
                            fetchStudentMarks(studentModel, paperId, studentsProcessed, totalStudents);
                        } else {
                            // Still increment counter for students with missing data
                            if (studentsProcessed.incrementAndGet() == totalStudents) {
                                adapter.notifyDataSetChanged();
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("GradingFetch", "Failed to fetch students", e);
                    Toast.makeText(this, "Failed to load students", Toast.LENGTH_SHORT).show();
                });
    }

    private void fetchStudentMarks(StudentModel studentModel, String paperId, AtomicInteger studentsProcessed, int totalStudents) {
        String currentYear = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));

        // Build the document reference path
        String gradesPath = "grades/" + selectedDepartment + "/" + selectedCourse + "/" + currentYear + "/" + selectedSemester + "/" + paperId + "/students/" + studentModel.uid;
        Log.d("GradingFetch", "Fetching marks from path: " + gradesPath);

        DocumentReference gradeDocRef = db.collection("grades")
                .document(selectedDepartment)
                .collection(selectedCourse)
                .document(currentYear)
                .collection(selectedSemester)
                .document(paperId)
                .collection("students")
                .document(studentModel.uid);

        gradeDocRef.get()
                .addOnSuccessListener(gradeDoc -> {
                    if (gradeDoc.exists()) {
                        Long marks = gradeDoc.getLong("marks");
                        if (marks != null) {
                            studentModel.marks = marks.intValue();
                            Log.d("GradingFetch", "Found marks for " + studentModel.name + ": " + marks);
                        } else {
                            Log.d("GradingFetch", "Marks field is null for " + studentModel.name);
                            studentModel.marks = -1;
                        }
                    } else {
                        Log.d("GradingFetch", "No grade document found for " + studentModel.name);
                        studentModel.marks = -1;
                    }

                    // Update UI when all students' marks are fetched
                    if (studentsProcessed.incrementAndGet() == totalStudents) {
                        Log.d("GradingFetch", "All students processed, updating UI");
                        runOnUiThread(() -> adapter.notifyDataSetChanged());
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("GradingFetch", "Failed to fetch marks for student: " + studentModel.name, e);
                    studentModel.marks = -1;

                    // Still increment counter even on failure
                    if (studentsProcessed.incrementAndGet() == totalStudents) {
                        Log.d("GradingFetch", "All students processed (with errors), updating UI");
                        runOnUiThread(() -> adapter.notifyDataSetChanged());
                    }
                });
    }

    class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.StudentViewHolder> {
        List<StudentModel> students;

        StudentAdapter(List<StudentModel> list) {
            this.students = list;
        }

        class StudentViewHolder extends RecyclerView.ViewHolder {
            TextView nameText, markText;

            public StudentViewHolder(View itemView) {
                super(itemView);
                nameText = itemView.findViewById(R.id.studentName);
                markText = itemView.findViewById(R.id.studentMarks);

                itemView.setOnClickListener(v -> {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        showMarkEntryDialog(students.get(position), position);
                    }
                });
            }

            private void showMarkEntryDialog(StudentModel student, int position) {
                AlertDialog.Builder builder = new AlertDialog.Builder(itemView.getContext());
                builder.setTitle("Enter Marks for " + student.name);

                final EditText input = new EditText(itemView.getContext());
                input.setInputType(InputType.TYPE_CLASS_NUMBER);
                input.setHint("Enter marks (0-100)");

                // Pre-fill with existing marks if available
                if (student.marks >= 0) {
                    input.setText(String.valueOf(student.marks));
                }

                builder.setView(input);

                builder.setPositiveButton("Save", (dialog, which) -> {
                    String marksInput = input.getText().toString().trim();
                    if (!marksInput.isEmpty()) {
                        try {
                            int marks = Integer.parseInt(marksInput);
                            if (marks >= 0 && marks <= 100) {
                                saveMarksToFirebase(student, marks, position);
                            } else {
                                Toast.makeText(itemView.getContext(), "Marks should be between 0-100", Toast.LENGTH_SHORT).show();
                            }
                        } catch (NumberFormatException e) {
                            Toast.makeText(itemView.getContext(), "Invalid input. Please enter valid marks.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(itemView.getContext(), "Please enter marks.", Toast.LENGTH_SHORT).show();
                    }
                });

                builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
                builder.show();
            }

            private void saveMarksToFirebase(StudentModel student, int marks, int position) {
                String currentYear = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
                String selectedPaperId = paperIds.get(paperSpinner.getSelectedItemPosition());

                // Reference for the current paper in grades (hierarchical structure)
                DocumentReference studentDocRef = db.collection("grades")
                        .document(selectedDepartment)
                        .collection(selectedCourse)
                        .document(currentYear)
                        .collection(selectedSemester)
                        .document(selectedPaperId)
                        .collection("students")
                        .document(student.uid);

                // Data to be saved
                Map<String, Object> studentData = new HashMap<>();
                studentData.put("fullName", student.name);
                studentData.put("marks", marks);
                studentData.put("studentId", student.uid);
                studentData.put("paperId", selectedPaperId);
                studentData.put("courseId", selectedCourse);
                studentData.put("departmentId", selectedDepartment);
                studentData.put("semester", selectedSemester);
                studentData.put("timestamp", FieldValue.serverTimestamp());

                // Save to hierarchical grades collection
                studentDocRef.set(studentData, SetOptions.merge())
                        .addOnSuccessListener(aVoid -> {
                            student.marks = marks;
                            adapter.notifyItemChanged(position);
                            Toast.makeText(itemView.getContext(), "Marks saved successfully", Toast.LENGTH_SHORT).show();

                            // Also save to flat all_grades collection for reporting
                            saveToAllGrades(studentData, student.uid, selectedPaperId);
                        })
                        .addOnFailureListener(e -> {
                            Log.e("GradingSave", "Failed to save marks", e);
                            Toast.makeText(itemView.getContext(), "Failed to save marks", Toast.LENGTH_SHORT).show();
                        });
            }

            private void saveToAllGrades(Map<String, Object> studentData, String studentUid, String paperId) {
                // Check if record already exists in all_grades
                db.collection("all_grades")
                        .whereEqualTo("studentId", studentUid)
                        .whereEqualTo("paperId", paperId)
                        .whereEqualTo("semester", selectedSemester)
                        .get()
                        .addOnSuccessListener(querySnapshot -> {
                            if (!querySnapshot.isEmpty()) {
                                // Update existing record
                                DocumentReference docRef = querySnapshot.getDocuments().get(0).getReference();
                                docRef.update(studentData)
                                        .addOnSuccessListener(aVoid -> Log.d("GradingSave", "Updated all_grades for: " + studentUid))
                                        .addOnFailureListener(e -> Log.e("GradingSave", "Failed to update all_grades", e));
                            } else {
                                // Create new record
                                db.collection("all_grades").add(studentData)
                                        .addOnSuccessListener(documentReference -> Log.d("GradingSave", "Added to all_grades for: " + studentUid))
                                        .addOnFailureListener(e -> Log.e("GradingSave", "Failed to add to all_grades", e));
                            }
                        })
                        .addOnFailureListener(e -> Log.e("GradingSave", "Failed to query all_grades", e));
            }
        }

        @NonNull
        @Override
        public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_grading, parent, false);
            return new StudentViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull StudentViewHolder holder, int position) {
            StudentModel student = students.get(position);
            holder.nameText.setText(student.name);

            // Update marks display with better formatting
            String marksText;
            if (student.marks >= 0) {
                marksText = "Marks: " + student.marks;
            } else {
                marksText = "Marks: Not Assigned";
            }
            holder.markText.setText(marksText);

            Log.d("GradingUI", "Binding student: " + student.name + " with marks: " + student.marks);
        }

        @Override
        public int getItemCount() {
            return students.size();
        }
    }

    class StudentModel {
        String uid, name;
        int marks;

        StudentModel(String uid, String name, int marks) {
            this.uid = uid;
            this.name = name;
            this.marks = marks;
        }
    }
}