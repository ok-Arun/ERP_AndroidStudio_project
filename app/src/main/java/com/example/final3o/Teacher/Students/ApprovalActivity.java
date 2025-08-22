package com.example.final3o.Teacher.Students;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.final3o.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ApprovalActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FirebaseFirestore db;
    private ChangeRequestAdapter adapter;
    private List<ChangeRequest> changeRequestsList;
    private String studentId="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_approval);

        db = FirebaseFirestore.getInstance();
        recyclerView = findViewById(R.id.requestsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        changeRequestsList = new ArrayList<>();
        adapter = new ChangeRequestAdapter(this, changeRequestsList);
        recyclerView.setAdapter(adapter);

        // Fetch change requests from Firestore
        fetchChangeRequests();
    }

    private void fetchChangeRequests() {
        CollectionReference requestsRef = db.collection("approval_requests");
        requestsRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot documents = task.getResult();
                if (documents != null && !documents.isEmpty()) {
                    for (DocumentSnapshot document : documents) {
                        String fullName = document.getString("fullName");
                        String address = document.getString("address");
                        String courseName = document.getString("courseName");
                        String dob = document.getString("dob");
                        String gender = document.getString("gender");
                        String parentName = document.getString("parentName");
                        String phoneNumber = document.getString("phoneNumber");
                        String semester = document.getString("semester");
                        String status = document.getString("status");
                        String studentId = document.getString("studentId");
                        String formattedSemester = "Semester " + semester;

                        ChangeRequest request = new ChangeRequest(fullName, address, courseName, dob, gender,
                                parentName, phoneNumber, semester, status, studentId);

                        changeRequestsList.add(request);
                    }
                    adapter.notifyDataSetChanged();
                } else {
                    // Show a popup dialog if no requests are found
                    showNoRequestsDialog();
                }
            } else {
                Toast.makeText(this, "Failed to fetch requests", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void showNoRequestsDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("No Requests")
                .setMessage("There are currently no approval requests.")
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }

    public void approveRequest(ChangeRequest request) {
        String studentId = request.getStudentId(); // Get the studentId from the request object

        // 1) Fetch the change request document from "approval_requests" collection
        db.collection("approval_requests").document(studentId).get()
                .addOnSuccessListener(docSnap -> {
                    if (docSnap.exists()) {
                        // 2) Get the request data from the document
                        Map<String, Object> requestData = docSnap.getData();

                        if (requestData.containsKey("semester")) {
                            String semester = (String) requestData.get("semester");
                            requestData.put("semester", "Semester " + semester);
                        }
                        // Update the student data in "students" collection
                        db.collection("students").document(studentId)
                                .set(requestData, SetOptions.merge()) // Use merge to preserve other existing fields
                                .addOnSuccessListener(aVoid -> {
                                    // 3) Once the student is updated, delete the request from "approval_requests" collection
                                    db.collection("approval_requests").document(studentId).delete()
                                            .addOnSuccessListener(aVoid1 -> {
                                                // Remove the item from the list of change requests
                                                for (ChangeRequest req : changeRequestsList) {
                                                    if (req.getStudentId().equals(studentId)) {
                                                        changeRequestsList.remove(req);
                                                        break;
                                                    }
                                                }
                                                // Notify the adapter to refresh the data
                                                adapter.notifyDataSetChanged();

                                                // Notify the teacher that the request has been approved and deleted
                                                Toast.makeText(this, "Request Approved and Deleted", Toast.LENGTH_SHORT).show();
                                            })
                                            .addOnFailureListener(e -> Toast.makeText(this, "Failed to delete request", Toast.LENGTH_SHORT).show());
                                })
                                .addOnFailureListener(e -> Toast.makeText(this, "Failed to update student data", Toast.LENGTH_SHORT).show());
                    } else {
                        Toast.makeText(this, "Request not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to fetch request data", Toast.LENGTH_SHORT).show());
    }




    // Handle Reject logic for each request
    public void rejectRequest(ChangeRequest request) {
        String studentId = request.getStudentId(); // Get the studentId from the request object

        // 1) Delete the request from the "approval_requests" collection if rejected
        db.collection("approval_requests").document(studentId) // Use the studentId from the request to find the document
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // 2) Remove the item from the list of change requests
                    changeRequestsList.removeIf(req -> req.getStudentId().equals(studentId));

                    // Notify the adapter to refresh the data
                    adapter.notifyDataSetChanged();

                    // Notify the teacher that the request has been rejected
                    Toast.makeText(this, "Request Rejected", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to reject request", Toast.LENGTH_SHORT).show());
    }


}
