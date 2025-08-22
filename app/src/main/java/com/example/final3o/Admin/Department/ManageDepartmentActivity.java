package com.example.final3o.Admin.Department;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.final3o.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.ArrayList;
import java.util.List;
import android.util.Log;

public class ManageDepartmentActivity extends AppCompatActivity implements DepartmentAdapter.OnDepartmentActionListener {

    private RecyclerView departmentRecyclerView;
    private DepartmentAdapter departmentAdapter;
    private List<DepartmentModel> departmentList;
    private FirebaseFirestore db;
    private CourseAdapter courseAdapter;

    private static final String TAG = "ManageDepartmentActivity";  // Added for logging

    // Declare views for the department name, dep ID, and teacher spinner
    private EditText depNameText;
    private TextView depIdText;
    private Spinner teacherAssignMainSpinner;
    private Button addDepartmentButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_department);

        db = FirebaseFirestore.getInstance();

        departmentRecyclerView = findViewById(R.id.departmentRecyclerView);
        departmentRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        departmentRecyclerView.setHasFixedSize(true);  // Optimized RecyclerView

        departmentList = new ArrayList<>();
        departmentAdapter = new DepartmentAdapter(departmentList, this, this);
        departmentRecyclerView.setAdapter(departmentAdapter);

        // Initialize views
        depNameText = findViewById(R.id.depNameText);
        depIdText = findViewById(R.id.depIdText);
        teacherAssignMainSpinner = findViewById(R.id.teacherAssignMainSpinner);
        addDepartmentButton = findViewById(R.id.addDepartmentButton);

        // Fetch teachers and populate the spinner
        fetchTeachersAndPopulateSpinner(teacherAssignMainSpinner);

        // Add department button logic
        addDepartmentButton.setOnClickListener(v -> {
            String departmentName = depNameText.getText().toString().trim();
            String depId = generateDepId(departmentName);
            String selectedTeacher = teacherAssignMainSpinner.getSelectedItem().toString();

            if (!departmentName.isEmpty() && !selectedTeacher.isEmpty()) {
                addDepartmentToFirestore(departmentName, depId, selectedTeacher);
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            }
        });

        fetchDepartments();
    }

    private void fetchDepartments() {
        db.collection("departments")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    departmentList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String departmentName = document.getString("departmentName");
                        String depid = document.getString("depid");
                        String teacher = document.getString("assignedTeacher");
                        if (departmentName != null && depid != null) {
                            departmentList.add(new DepartmentModel(departmentName, depid, teacher));
                        }
                    }
                    departmentAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to fetch departments", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error fetching departments: ", e);
                });
    }

    private void addDepartmentToFirestore(String departmentName, String depId, String assignedTeacher) {
        DepartmentModel newDepartment = new DepartmentModel(departmentName, depId, assignedTeacher);

        db.collection("departments")
                .document(depId)
                .set(newDepartment)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Department added successfully", Toast.LENGTH_SHORT).show();
                    fetchDepartments(); // Refresh the department list

                    depNameText.setText("");
                    depIdText.setText("");
                    teacherAssignMainSpinner.setSelection(0);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to add department", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error adding department: ", e);
                });
    }

    // Generate department ID based on department name (e.g., Information Technology => IT)
    private String generateDepId(String departmentName) {
        String[] words = departmentName.split(" ");
        StringBuilder depId = new StringBuilder();
        for (String word : words) {
            depId.append(word.charAt(0)); // Add the first letter of each word
        }
        return depId.toString().toUpperCase(); // Convert to uppercase (e.g., IT)
    }

    private void fetchTeachersAndPopulateSpinner(Spinner spinner) {
        db.collection("teachers")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> teacherNames = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String name = document.getString("name");
                        if (name != null) teacherNames.add(name);
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, teacherNames);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinner.setAdapter(adapter);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load teachers", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error fetching teachers: ", e);
                });
    }


    @Override
    public void onDepartmentClick(DepartmentModel department) {
        showDepartmentDetailsDialog(department);
    }

    private void showDepartmentDetailsDialog(DepartmentModel department) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_update_department, null);

        Spinner teacherAssignSpinner = dialogView.findViewById(R.id.teacherAssignSpinner);
        Button updateTeacherButton = dialogView.findViewById(R.id.updateTeacherButton);
        Button addCourseButton = dialogView.findViewById(R.id.addCourseButton);
        RecyclerView courseRecyclerView = dialogView.findViewById(R.id.courseRecyclerView);

        setupCourseRecyclerView(courseRecyclerView, department.getDepid());

        fetchTeachersAndPopulateSpinner(teacherAssignSpinner);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Department: " + department.getDepartmentName());
        builder.setView(dialogView);
        builder.setCancelable(true);

        AlertDialog dialog = builder.create();
        dialog.show();

// Moved this after dialog.show()
        fetchTeachersAndPopulateSpinner(teacherAssignMainSpinner);


        updateTeacherButton.setOnClickListener(v -> {
            String selectedTeacher = teacherAssignSpinner.getSelectedItem() != null ?
                    teacherAssignSpinner.getSelectedItem().toString() : "";
            if (!selectedTeacher.isEmpty()) {
                updateTeacher(department, selectedTeacher);
            } else {
                Toast.makeText(this, "Please select a teacher", Toast.LENGTH_SHORT).show();
            }
        });

        addCourseButton.setOnClickListener(v -> {
            showAddCourseDialog(department.getDepid(), () -> setupCourseRecyclerView(courseRecyclerView, department.getDepid()));
        });

        builder.setNegativeButton("Close", (d, which) -> dialog.dismiss());
    }

    private void updateTeacher(DepartmentModel department, String newTeacher) {
        db.collection("departments")
                .document(department.getDepid())
                .update("assignedTeacher", newTeacher)
                .addOnSuccessListener(aVoid -> {
                    department.setAssignedTeacher(newTeacher);
                    Toast.makeText(this, "Teacher updated successfully", Toast.LENGTH_SHORT).show();
                    fetchDepartments(); // Refresh department list
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to update teacher", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error updating teacher: ", e);
                });
    }

    private void setupCourseRecyclerView(RecyclerView recyclerView, String depid) {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);  // Optimized RecyclerView
        courseAdapter = new CourseAdapter(new ArrayList<>());
        recyclerView.setAdapter(courseAdapter);

        db.collection("courses")
                .whereEqualTo("depid", depid)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<CourseModel> courseList = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        CourseModel course = doc.toObject(CourseModel.class);
                        if (course != null) {
                            courseList.add(course);
                        }
                    }
                    courseAdapter.updateCourseList(courseList);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load courses", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error fetching courses: ", e);
                });
    }

    private void showAddCourseDialog(String depid, Runnable onCourseAdded) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New Course");

        // Inflate the dialog layout
        View view = getLayoutInflater().inflate(R.layout.dialog_add_course, null);

        // Find views in the dialog layout
        EditText courseNameInput = view.findViewById(R.id.courseNameInput);
        EditText cidInput = view.findViewById(R.id.courseIdInput);
        EditText semesterInput = view.findViewById(R.id.semesterInput);  // Correct reference for semester input

        builder.setView(view);

        builder.setPositiveButton("Add", (dialog, which) -> {
            if (courseNameInput != null && cidInput != null && semesterInput != null) {
                String courseName = courseNameInput.getText().toString().trim();
                String cid = cidInput.getText().toString().trim();
                String semesterStr = semesterInput.getText().toString().trim();

                if (!courseName.isEmpty() && !cid.isEmpty() && !semesterStr.isEmpty()) {
                    try {
                        int semester = Integer.parseInt(semesterStr); // Parse semester input
                        // Create a new CourseModel with the depid, course name, course ID, and semester
                        CourseModel newCourse = new CourseModel(cid, courseName, depid, semester);

                        db.collection("courses")
                                .document(cid)
                                .set(newCourse)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Course added", Toast.LENGTH_SHORT).show();
                                    onCourseAdded.run(); // Refresh the course list
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Failed to add course", Toast.LENGTH_SHORT).show();
                                    Log.e(TAG, "Error adding course: ", e);
                                });
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Please enter a valid semester number", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Error: Input fields not found.", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        // Show the dialog
        builder.show();
    }
}
