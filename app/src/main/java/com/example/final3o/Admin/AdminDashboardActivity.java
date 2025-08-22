package com.example.final3o.Admin;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.final3o.Admin.Attendance.AdminAttendanceActivity;
import com.example.final3o.Admin.Course.ManageCoursesActivity;
import com.example.final3o.Admin.Department.ManageDepartmentActivity;
import com.example.final3o.Admin.User.ManageUsersActivity;
import com.example.final3o.LoginActivity;
import com.example.final3o.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

public class AdminDashboardActivity extends AppCompatActivity {

    private TextView welcomeAdminText, adminIdText, totalUsersCount, activeUsersCount;
    private Button manageUsersButton, manageDepartmentButton, manageCourseButton, manageAttendanceButton, logoutButton;
    ;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private static final String TAG = "ADMIN_DASHBOARD";

    private ListenerRegistration userStatsListener; // Listener for real-time updates

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        // Initialize views
        welcomeAdminText = findViewById(R.id.welcomeAdminText);
        adminIdText = findViewById(R.id.adminIdText);
        totalUsersCount = findViewById(R.id.totalUsersCount);
        activeUsersCount = findViewById(R.id.activeUsersCount);
        manageUsersButton = findViewById(R.id.manageUsersButton);
        manageDepartmentButton = findViewById(R.id.manageDepartmentButton);
        manageCourseButton = findViewById(R.id.manageCourseButton);
        manageAttendanceButton = findViewById(R.id.manageAttendanceButton);
        logoutButton = findViewById(R.id.logoutButton);


        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Fetch admin info
        fetchAdminInfo();

        // Real-time fetch user stats
        fetchUserStatsFromFirestore();

        // Set up button actions
        manageUsersButton.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, ManageUsersActivity.class);
            startActivity(intent);
        });

        manageDepartmentButton.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, ManageDepartmentActivity.class);
            startActivity(intent);
        });

        manageCourseButton.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, ManageCoursesActivity.class);
            startActivity(intent);
        });

        manageAttendanceButton.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, AdminAttendanceActivity.class);
            startActivity(intent);
        });

        logoutButton.setOnClickListener(v -> {
            mAuth.signOut(); // Sign out from Firebase
            Intent intent = new Intent(AdminDashboardActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear back stack
            startActivity(intent);
            finish(); // Finish current activity
        });

    }

    private void fetchAdminInfo() {
        String adminUid = mAuth.getCurrentUser().getUid();

        db.collection("admins")
                .document(adminUid)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            String adminName = document.getString("name");
                            String adminEmail = document.getString("email");

                            welcomeAdminText.setText("Welcome, " + adminName);
                            adminIdText.setText("Admin ID: " + adminEmail);
                            Log.d(TAG, "Admin Info: " + adminName + " | " + adminEmail);
                        } else {
                            Log.d(TAG, "No admin found with UID: " + adminUid);
                        }
                    } else {
                        Log.e(TAG, "Error fetching admin info: ", task.getException());
                    }
                });
    }

    private void fetchUserStatsFromFirestore() {
        userStatsListener = db.collection("users")
                .addSnapshotListener((querySnapshot, e) -> {
                    if (e != null) {
                        Log.e(TAG, "Listen failed: ", e);
                        return;
                    }

                    if (querySnapshot != null) {
                        int totalCount = querySnapshot.size();
                        int activeCount = 0;

                        for (QueryDocumentSnapshot document : querySnapshot) {
                            String userStatus = document.getString("userStatus");
                            if ("active".equalsIgnoreCase(userStatus)) {
                                activeCount++;
                            }
                        }

                        totalUsersCount.setText(String.valueOf(totalCount));
                        activeUsersCount.setText(String.valueOf(activeCount));
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (userStatsListener != null) {
            userStatsListener.remove();
        }
    }
}
