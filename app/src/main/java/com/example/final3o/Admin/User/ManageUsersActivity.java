package com.example.final3o.Admin.User;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.final3o.Admin.PrimaryAdminRegisterActivity;
import com.example.final3o.R;
import com.example.final3o.Teacher.TeacherRegistrationActivity;
import com.google.firebase.firestore.*;

import java.util.*;

public class ManageUsersActivity extends AppCompatActivity {

    private RecyclerView usersRecyclerView;
    private UserAdapter userAdapter;
    private List<User> fullUserList = new ArrayList<>();
    private FirebaseFirestore db;
    private SearchView searchView;
    private Spinner roleSpinner, cidSpinner;
    private List<String> courseIds = new ArrayList<>();
    private List<String> roles = Arrays.asList("All", "Student", "Teacher", "Admin");
    private TextView userCountTextView;
    private Button inviteButton, addAdminButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_users);

        usersRecyclerView = findViewById(R.id.usersRecyclerView);
        searchView = findViewById(R.id.searchView);
        roleSpinner = findViewById(R.id.roleSpinner);
        cidSpinner = findViewById(R.id.cidSpinner);
        userCountTextView = findViewById(R.id.userCountTextView);
        inviteButton = findViewById(R.id.inviteButton);
        addAdminButton = findViewById(R.id.addAdminButton);

        usersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        userAdapter = new UserAdapter(fullUserList, this);
        usersRecyclerView.setAdapter(userAdapter);

        db = FirebaseFirestore.getInstance();
        fetchCourses();

        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, roles);
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roleSpinner.setAdapter(roleAdapter);

        fetchUsers();

        inviteButton.setOnClickListener(v -> {
            startActivity(new Intent(ManageUsersActivity.this, TeacherRegistrationActivity.class));
        });

        addAdminButton.setOnClickListener(v -> {
            startActivity(new Intent(ManageUsersActivity.this, PrimaryAdminRegisterActivity.class));
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterUsers(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterUsers(newText);
                return true;
            }
        });

        roleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterUsers(searchView.getQuery().toString());
            }

            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        cidSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterUsers(searchView.getQuery().toString());
            }

            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void fetchCourses() {
        db.collection("courses").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                courseIds.clear();
                courseIds.add("All");
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    String courseId = doc.getString("courseId");
                    if (courseId != null) {
                        courseIds.add(courseId);
                    }
                }

                ArrayAdapter<String> cidAdapter = new ArrayAdapter<>(this,
                        android.R.layout.simple_spinner_item, courseIds);
                cidAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                cidSpinner.setAdapter(cidAdapter);
            }
        });
    }

    private void fetchUsers() {
        db.collection("users").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                fullUserList.clear();
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    User user = doc.toObject(User.class);
                    fullUserList.add(user);
                }
                userAdapter.updateList(fullUserList);
                updateUserCount(fullUserList.size());
            }
        });
    }

    private void filterUsers(String query) {
        List<User> filteredList = new ArrayList<>();
        String selectedRole = roleSpinner.getSelectedItem() != null ? roleSpinner.getSelectedItem().toString() : "All";
        String selectedCid = cidSpinner.getSelectedItem() != null ? cidSpinner.getSelectedItem().toString() : "All";

        for (User user : fullUserList) {
            boolean matchesRole = selectedRole.equals("All") ||
                    (user.getRole() != null && user.getRole().equalsIgnoreCase(selectedRole));
            boolean matchesCid = selectedCid.equals("All") ||
                    (user.getCid() != null && user.getCid().equalsIgnoreCase(selectedCid));
            boolean matchesSearch = (query == null || query.trim().isEmpty()) ||
                    (user.getFullName() != null && user.getFullName().toLowerCase().contains(query.toLowerCase()));

            if (matchesRole && matchesCid && matchesSearch) {
                filteredList.add(user);
            }
        }

        userAdapter.updateList(filteredList);
        updateUserCount(filteredList.size());
    }

    private void updateUserCount(int count) {
        userCountTextView.setText("Users Found: " + count);
    }
    private String getDisplayName(User user) {
        String role = user.getRole();
        if ("student".equalsIgnoreCase(role)) {
            return user.getFullName();
        } else {
            return user.getName();
        }
    }

    private String capitalize(String text) {
        if (text == null || text.isEmpty()) return "";
        return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
    }

    public void showOptionsDialog(User user) {
        String currentStatus = user.getUserStatus() != null ? user.getUserStatus().toLowerCase() : "active";
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("Select Action")
                .setMessage("What do you want to do with " + getDisplayName(user) + " (" + capitalize(user.getRole()) + ")?");


        String role = user.getRole() != null ? user.getRole().toLowerCase() : "";

        switch (role) {
            case "student":
                if (!"passout".equals(currentStatus)) {
                    builder.setPositiveButton("Mark Passout", (dialog, which) -> showConfirmationDialog("Passout", user));
                }
                builder.setNegativeButton("Remove", (dialog, which) -> showConfirmationDialog("Remove", user));
                break;

            case "teacher":
                if (!"retired".equals(currentStatus)) {
                    builder.setPositiveButton("Mark Retired", (dialog, which) -> showConfirmationDialog("Retired", user));
                }
                builder.setNegativeButton("Remove", (dialog, which) -> showConfirmationDialog("Remove", user));
                break;

            case "admin":
                builder.setNegativeButton("Remove", (dialog, which) -> showConfirmationDialog("Remove", user));
                break;

            default:
                builder.setNegativeButton("Remove", (dialog, which) -> showConfirmationDialog("Remove", user));
                break;
        }

        builder.setCancelable(true);
        builder.show();
    }

    public void showConfirmationDialog(String action, User user) {
        String message;
        switch (action) {
            case "Active":
                message = "Are you sure you want to set " + user.getFullName() + " as Active?";
                break;
            case "Passout":
                message = "Are you sure you want to mark " + user.getFullName() + " as Passout?";
                break;
            case "Retired":
                message = "Are you sure you want to mark " + user.getName() + " as Retired?";
                break;
            case "Remove":
                message = "Are you sure you want to remove " + user.getFullName() + " from the system?";
                break;
            default:
                message = "Are you sure?";
        }

        new AlertDialog.Builder(this)
                .setTitle("Confirm Action")
                .setMessage(message)
                .setPositiveButton("Yes", (dialog, which) -> performAction(action, user))
                .setNegativeButton("No", null)
                .show();
    }

    private void performAction(String action, User user) {
        if ("Remove".equals(action)) {
            removeUser(user);
        } else {
            updateUserStatus(user, action.toLowerCase());
        }
    }

    private void updateUserStatus(User user, String status) {
        db.collection("users").document(user.getUid())
                .update("userStatus", status)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, user.getFullName() + " marked as " + status, Toast.LENGTH_SHORT).show();
                    fetchUsers();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to update status", Toast.LENGTH_SHORT).show());
    }

    private void removeUser(User user) {
        db.collection("users").document(user.getUid())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, user.getFullName() + " removed successfully", Toast.LENGTH_SHORT).show();
                    fetchUsers();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to remove user", Toast.LENGTH_SHORT).show());
    }
}
