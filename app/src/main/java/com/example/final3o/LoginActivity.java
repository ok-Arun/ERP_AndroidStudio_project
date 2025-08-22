package com.example.final3o;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.example.final3o.Admin.AdminDashboardActivity;
import com.example.final3o.Registration.RegisterActivity;
import com.example.final3o.Teacher.TeacherDashboardActivity;
import com.example.final3o.Student.StudentDashboardActivity;
import com.google.firebase.auth.*;
import com.google.firebase.firestore.*;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private Button loginButton, registerButton;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // UI References
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        registerButton = findViewById(R.id.registerButton);

        // Login Button
        loginButton.setOnClickListener(v -> loginUser());

        // Register Button - Redirect to Registration Activity
        registerButton.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void loginUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    // Fetch user data based on email from Firestore
                    fetchUserData(email);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(LoginActivity.this, "Login failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    private void fetchUserData(String email) {
        // Check if user is in 'users' or 'admins' collection
        db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // User found in the 'users' collection
                        DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                        String role = document.getString("role");

                        // Based on role, navigate to the respective dashboard
                        if ("student".equals(role)) {
                            openStudentDashboard();
                        } else if ("teacher".equals(role)) {
                            openTeacherDashboard();
                        }
                        else if ("admin".equals(role)) {
                            openAdminDashboard();
                        }else {
                            Toast.makeText(this, "Invalid role", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // User not found in 'users' collection, check in 'admins'
                        checkAdmin(email);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error fetching user data: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void checkAdmin(String email) {
        // Check if user is in 'admins' collection
        db.collection("admins")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Admin found, open Admin Dashboard
                        openAdminDashboard();
                    } else {
                        Toast.makeText(this, "You are not authorized as an admin.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error fetching admin data: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void openStudentDashboard() {
        Intent intent = new Intent(LoginActivity.this, StudentDashboardActivity.class);
        startActivity(intent);
        finish();
    }

    private void openTeacherDashboard() {
        Intent intent = new Intent(LoginActivity.this, TeacherDashboardActivity.class);
        startActivity(intent);
        finish();
    }

    private void openAdminDashboard() {
        Intent intent = new Intent(LoginActivity.this, AdminDashboardActivity.class);
        startActivity(intent);
        finish();
    }
}
