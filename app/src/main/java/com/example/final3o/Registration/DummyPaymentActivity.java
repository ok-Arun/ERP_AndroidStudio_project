package com.example.final3o.Registration;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.example.final3o.LoginActivity;
import com.example.final3o.R;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.*;

public class DummyPaymentActivity extends AppCompatActivity {

    private EditText cardNumberEditText, expiryDateEditText, cvvEditText, cardHolderNameEditText;
    private Button payNowButton;

    private String fullName, email, dob, gender, parentName, course, courseName, password, studentId, depid;
    private int regYear;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private final Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dummy_payment);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // UI references
        cardNumberEditText = findViewById(R.id.cardNumberEditText);
        expiryDateEditText = findViewById(R.id.expiryDateEditText);
        cvvEditText = findViewById(R.id.cvvEditText);
        cardHolderNameEditText = findViewById(R.id.cardHolderNameEditText);
        payNowButton = findViewById(R.id.payNowButton);

        // Get data from intent
        Intent intent = getIntent();
        fullName = intent.getStringExtra("fullName");
        email = intent.getStringExtra("email");
        dob = intent.getStringExtra("dob");
        gender = intent.getStringExtra("gender");
        parentName = intent.getStringExtra("parentName");
        course = intent.getStringExtra("cid");
        courseName = intent.getStringExtra("courseName");
        depid = intent.getStringExtra("depid");
        password = intent.getStringExtra("password");
        regYear = intent.getIntExtra("regYear", 0);

        // Format expiry MM/YY
        expiryDateEditText.addTextChangedListener(new TextWatcher() {
            boolean isFormatting;
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!isFormatting && s.length() == 2) {
                    isFormatting = true;
                    expiryDateEditText.setText(s + "/");
                    expiryDateEditText.setSelection(3);
                    isFormatting = false;
                }
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void afterTextChanged(Editable s) { }
        });

        payNowButton.setOnClickListener(v -> {
            if (validateCardDetails()) {
                payNowButton.setEnabled(false);
                payNowButton.setText("Processing...");
                handler.postDelayed(this::generateStudentIdAndRegister, 2000);
            }
        });
    }

    private boolean validateCardDetails() {
        if (cardNumberEditText.getText().toString().trim().length() != 16) {
            Toast.makeText(this, "Card number must be 16 digits", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!expiryDateEditText.getText().toString().contains("/")) {
            Toast.makeText(this, "Expiry date must be in MM/YY format", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (cvvEditText.getText().toString().trim().length() != 3) {
            Toast.makeText(this, "CVV must be 3 digits", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (cardHolderNameEditText.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Enter card holder name", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void generateStudentIdAndRegister() {
        String counterDocId = course + regYear;
        DocumentReference counterRef = db.collection("counters").document(counterDocId);

        db.runTransaction(transaction -> {
            DocumentSnapshot snapshot = transaction.get(counterRef);
            long newCount = 1;
            if (snapshot.exists() && snapshot.contains("count")) {
                newCount = snapshot.getLong("count") + 1;
            }
            transaction.set(counterRef, Collections.singletonMap("count", newCount));
            return newCount;
        }).addOnSuccessListener(count -> {
            studentId = course + regYear + String.format("%03d", count);
            registerStudent();
        }).addOnFailureListener(e -> {
            Toast.makeText(DummyPaymentActivity.this, "Failed to generate student ID", Toast.LENGTH_SHORT).show();
            payNowButton.setEnabled(true);
            payNowButton.setText("Pay Now");
        });
    }

    private void registerStudent() {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    String firebaseUID = authResult.getUser().getUid();

                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("uid", firebaseUID);
                    userMap.put("fullName", fullName);
                    userMap.put("email", email);
                    userMap.put("dob", dob);
                    userMap.put("gender", gender);
                    userMap.put("parentName", parentName);
                    userMap.put("cid", course);
                    userMap.put("courseName", courseName);
                    userMap.put("depid", depid);
                    userMap.put("regYear", regYear);
                    userMap.put("studentId", studentId);
                    userMap.put("role", "student");
                    userMap.put("paymentStatus", "completed");
                    userMap.put("userStatus", "active");
                    userMap.put("semester", "Semester 1");
                    userMap.put("createdAt", Timestamp.now());

                    // Save to users collection with studentId as document ID
                    db.collection("users").document(studentId)
                            .set(userMap)
                            .addOnSuccessListener(aVoid -> {
                                // Save to students collection with studentId as document ID
                                db.collection("students").document(studentId)
                                        .set(userMap)
                                        .addOnSuccessListener(aVoid2 -> {
                                            Toast.makeText(DummyPaymentActivity.this, "Registration successful!", Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(DummyPaymentActivity.this, LoginActivity.class));
                                            finish();
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(DummyPaymentActivity.this, "Failed to save student data", Toast.LENGTH_SHORT).show();
                                            payNowButton.setEnabled(true);
                                            payNowButton.setText("Pay Now");
                                        });
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(DummyPaymentActivity.this, "Failed to save user data", Toast.LENGTH_SHORT).show();
                                payNowButton.setEnabled(true);
                                payNowButton.setText("Pay Now");
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(DummyPaymentActivity.this, "Registration failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    payNowButton.setEnabled(true);
                    payNowButton.setText("Pay Now");
                });
    }
}
