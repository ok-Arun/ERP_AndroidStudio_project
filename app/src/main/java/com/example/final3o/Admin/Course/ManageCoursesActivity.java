package com.example.final3o.Admin.Course;

import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.final3o.R;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.*;

public class ManageCoursesActivity extends AppCompatActivity {

    private EditText etPaperName, etPaperCode;
    private AutoCompleteTextView etCourseId, etDeptId, etTeacherName;
    private Spinner spinnerSemester, spinnerPaperType;
    private Button btnAddPaper;
    private RecyclerView recyclerView;

    private FirebaseFirestore db;
    private ArrayList<Paper> paperList;
    private PaperAdapter paperAdapter;

    private String selectedDepId = "";
    private String selectedCourseId = "";
    private String selectedTeacherId = "";
    private String selectedTeacherName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_course);

        // Initialize UI elements
        etPaperName = findViewById(R.id.etPaperName);
        etPaperCode = findViewById(R.id.etPaperCode);
        etCourseId = findViewById(R.id.etCourseName);
        etDeptId = findViewById(R.id.etDeptName);
        spinnerSemester = findViewById(R.id.spinnerSemester);
        spinnerPaperType = findViewById(R.id.spinnerPaperType);
        btnAddPaper = findViewById(R.id.btnAddPaper);
        etTeacherName = findViewById(R.id.etTeacherName);
        recyclerView = findViewById(R.id.recycleView);

        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance();
        paperList = new ArrayList<>();
        paperAdapter = new PaperAdapter(paperList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(paperAdapter);

        // Load semester options
        ArrayAdapter<String> semesterAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                new String[]{"Semester 1", "Semester 2", "Semester 3", "Semester 4", "Semester 5", "Semester 6"});
        semesterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSemester.setAdapter(semesterAdapter);

        // Load paper type options
        ArrayAdapter<String> paperTypeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                new String[]{"Theory", "Practical"});
        paperTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPaperType.setAdapter(paperTypeAdapter);

        // Load department, course, and teacher suggestions
        loadCourseSuggestions();
        loadDepartmentSuggestions();
        loadTeacherSuggestions(); // Load teacher suggestions

        // Add Paper button action
        btnAddPaper.setOnClickListener(v -> addPaperToFirestore());

        // Fetch and display existing papers
        fetchPapers();

        // Set item click listener for PaperAdapter
        paperAdapter.setOnItemClickListener(paper -> {
            // Populate the input fields with paper details when clicked
            populateFieldsWithPaperDetails(paper);
        });
    }

    private void populateFieldsWithPaperDetails(Paper paper) {
        etPaperName.setText(paper.getPaperName() != null && !paper.getPaperName().isEmpty() ? paper.getPaperName() : "Enter Paper Name");
        etPaperCode.setText(paper.getPaperCode() != null && !paper.getPaperCode().isEmpty() ? paper.getPaperCode() : "Enter Paper Code");
        etCourseId.setText(paper.getCourseName() != null && !paper.getCourseName().isEmpty() ? paper.getCourseName() : "Select Course");
        etDeptId.setText(paper.getDepartmentName() != null && !paper.getDepartmentName().isEmpty() ? paper.getDepartmentName() : "Select Department");
        etTeacherName.setText(paper.getTeacherName() != null && !paper.getTeacherName().isEmpty() ? paper.getTeacherName() : "Select Teacher");

        spinnerSemester.setSelection(getSemesterIndex(paper.getSemester() != null ? paper.getSemester() : ""));
        spinnerPaperType.setSelection(getPaperTypeIndex(paper.getPaperType() != null ? paper.getPaperType() : ""));
    }


    private int getSemesterIndex(String semester) {
        switch (semester) {
            case "Semester 1": return 0;
            case "Semester 2": return 1;
            case "Semester 3": return 2;
            case "Semester 4": return 3;
            case "Semester 5": return 4;
            case "Semester 6": return 5;
            default: return 0; // Default to Semester 1
        }
    }

    private int getPaperTypeIndex(String type) {
        return type.equals("Theory") ? 0 : 1; // Default to Theory if no match
    }


    private void loadDepartmentSuggestions() {
        db.collection("departments").get().addOnSuccessListener(query -> {
            ArrayList<String> list = new ArrayList<>();
            Map<String, String> map = new HashMap<>();
            for (var doc : query) {
                String name = doc.getString("departmentName");
                list.add(name);
                map.put(name, doc.getId());
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, list);
            etDeptId.setAdapter(adapter);
            etDeptId.setOnItemClickListener((p, v, i, l) -> selectedDepId = map.get(p.getItemAtPosition(i).toString()));
        });
    }

    private void loadCourseSuggestions() {
        db.collection("courses").get().addOnSuccessListener(query -> {
            ArrayList<String> list = new ArrayList<>();
            Map<String, String> map = new HashMap<>();
            for (var doc : query) {
                String name = doc.getString("courseName");
                list.add(name);
                map.put(name, doc.getId());
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, list);
            etCourseId.setAdapter(adapter);
            etCourseId.setOnItemClickListener((p, v, i, l) -> selectedCourseId = map.get(p.getItemAtPosition(i).toString()));
        });
    }

    private void loadTeacherSuggestions() {
        db.collection("teachers").get().addOnSuccessListener(query -> {
            ArrayList<String> teacherList = new ArrayList<>();
            Map<String, String> teacherMap = new HashMap<>();
            for (var doc : query.getDocuments()) {  // <-- FIX here: query.getDocuments()
                String teacherName = doc.getString("name");
                if (teacherName != null) {
                    teacherList.add(teacherName);
                    teacherMap.put(teacherName, doc.getId());
                }
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, teacherList);
            etTeacherName.setAdapter(adapter);
            etTeacherName.setOnItemClickListener((p, v, i, l) -> {
                selectedTeacherId = teacherMap.get(p.getItemAtPosition(i).toString());
                selectedTeacherName = p.getItemAtPosition(i).toString();
            });
        });
    }



    private void fetchPapers() {
        db.collection("papers").get().addOnSuccessListener(snapshot -> {
            paperList.clear();
            paperList.addAll(snapshot.toObjects(Paper.class));
            paperAdapter.notifyDataSetChanged();
        }).addOnFailureListener(e -> Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void addPaperToFirestore() {
        String name = etPaperName.getText().toString().trim();
        String code = etPaperCode.getText().toString().trim();
        String dept = selectedDepId;
        String course = selectedCourseId;
        String courseName = etCourseId.getText().toString().trim();
        String departmentName = etDeptId.getText().toString().trim();
        String semester = spinnerSemester.getSelectedItem().toString();
        String type = spinnerPaperType.getSelectedItem().toString();

        // Ensure that the teacher is assigned
        if (selectedTeacherId.isEmpty()) {
            Toast.makeText(this, "Please assign a teacher to the paper", Toast.LENGTH_SHORT).show();
            return;
        }

        // Ensure that all required fields are filled
        if (name.isEmpty() || code.isEmpty() || dept.isEmpty() || course.isEmpty() || courseName.isEmpty() || departmentName.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a new Paper object to store in Firestore
        Paper paper = new Paper(code,code, name, course, courseName, dept, departmentName, semester, type, selectedTeacherId, selectedTeacherName);

        // Save the paper information to Firestore
        db.collection("papers").document(code)  // Use paper code as the document ID
                .set(paper)
                .addOnSuccessListener(v -> {
                    Toast.makeText(this, "Paper added successfully", Toast.LENGTH_SHORT).show();
                    fetchPapers();  // Refresh the list of papers
                    clearFields();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void clearFields() {
        etPaperName.setText("");
        etPaperCode.setText("");
        etCourseId.setText("");
        etDeptId.setText("");
        etTeacherName.setText("");
        spinnerSemester.setSelection(0);  // Set to the first item in the spinner (default semester)
        spinnerPaperType.setSelection(0);  // Set to the first item in the spinner (default paper type)
        selectedDepId = "";
        selectedCourseId = "";
        selectedTeacherId = "";
        selectedTeacherName = "";
    }

}
