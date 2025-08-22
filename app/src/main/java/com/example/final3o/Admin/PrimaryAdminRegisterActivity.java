package com.example.final3o.Admin;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.final3o.Admin.User.ManageUsersActivity;
import com.example.final3o.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class PrimaryAdminRegisterActivity extends AppCompatActivity {

    EditText nameEditText, emailEditText, passwordEditText, confirmPasswordEditText;
    Button registerButton;

    FirebaseAuth mAuth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_primary_admin);

        // Initialize views
        nameEditText = findViewById(R.id.nameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        registerButton = findViewById(R.id.registerButton);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Register button click
        registerButton.setOnClickListener(v -> registerAdmin());
    }

    private void registerAdmin() {
        String name = nameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

        // Validation
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email)
                || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        // Generate current year for ID
        String year = new SimpleDateFormat("yy", Locale.getDefault()).format(new Date());

        // Count existing admins for this year
        db.collection("admins")
                .whereGreaterThanOrEqualTo("adminId", "admin" + year + "000")
                .whereLessThan("adminId", "admin" + year + "999")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int count = queryDocumentSnapshots.size() + 1;
                    String formattedCount = String.format("%03d", count);
                    String customAdminID = "admin" + year + formattedCount;

                    // Register with Firebase Auth
                    mAuth.createUserWithEmailAndPassword(email, password)
                            .addOnSuccessListener(authResult -> {
                                String firebaseUID = mAuth.getCurrentUser().getUid();

                                HashMap<String, Object> adminData = new HashMap<>();
                                adminData.put("adminId", customAdminID); // Custom ID
                                adminData.put("firebaseUID", firebaseUID);
                                adminData.put("name", name);
                                adminData.put("email", email);
                                adminData.put("role", "admin");
                                adminData.put("userStatus", "active");
                                adminData.put("isSuperAdmin", true);

                                // Save to Firestore admins collection
                                db.collection("admins").document(customAdminID)
                                        .set(adminData)
                                        .addOnSuccessListener(aVoid -> {
                                            // Also save to users collection
                                            db.collection("users").document(customAdminID)
                                                    .set(adminData)
                                                    .addOnSuccessListener(aVoid2 -> {
                                                        Toast.makeText(this, "Primary Admin Registered!", Toast.LENGTH_SHORT).show();
                                                        // Redirect to Admin Dashboard
                                                        startActivity(new Intent(this, ManageUsersActivity.class));
                                                        finish();
                                                    })
                                                    .addOnFailureListener(e ->
                                                            Toast.makeText(this, "Failed to add to users: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                                        })
                                        .addOnFailureListener(e ->
                                                Toast.makeText(this, "Firestore error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Auth error: " + e.getMessage(), Toast.LENGTH_SHORT).show());

                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to generate admin ID: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

}
