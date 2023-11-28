package com.app.dekutstudentportal;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class StudentRegisterActivity extends AppCompatActivity {

    private EditText fullName, email, regNum, password, confirmPassword;
    private RadioGroup genderRadioGroup;
    private RadioButton selectedGender;
    private MaterialButton registerButton;
    private TextView loginLink;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_register);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize Views
        fullName = findViewById(R.id.studentRegName);
        email = findViewById(R.id.studentRegEmail);
        regNum = findViewById(R.id.studentRegNum);
        genderRadioGroup = findViewById(R.id.stdGenderRadioGroup);
        password = findViewById(R.id.studentRegPass);
        confirmPassword = findViewById(R.id.studentConfirmPass);
        registerButton = findViewById(R.id.studentRegBtn);
        loginLink = findViewById(R.id.login_link);

        // Register Btn
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerUser();
            }
        });

        // Already registered Link [to Login]
        loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(StudentRegisterActivity.this, StudentLoginActivity.class);
                startActivity(intent);
            }
        });
    }

    private void registerUser() {
        // Get user inputs
        String fullNameText = fullName.getText().toString().trim();
        String emailText = email.getText().toString().trim();
        String regNumText = regNum.getText().toString().trim();
        int selectedGenderId = genderRadioGroup.getCheckedRadioButtonId();
        selectedGender = findViewById(selectedGenderId);
        String genderText = selectedGender.getText().toString();
        String passwordText = password.getText().toString().trim();
        String confirmPasswordText = confirmPassword.getText().toString().trim();

        // Validate inputs
        if (TextUtils.isEmpty(fullNameText) || TextUtils.isEmpty(emailText) ||
                TextUtils.isEmpty(regNumText) || TextUtils.isEmpty(genderText) ||
                TextUtils.isEmpty(passwordText) || TextUtils.isEmpty(confirmPasswordText)) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate email format
        if (!emailText.endsWith("students.dkut.ac.ke")) {
            Toast.makeText(this, "Email should end with 'students.dkut.ac.ke'", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate password length and match
        if (passwordText.length() < 6 || !passwordText.equals(confirmPasswordText)) {
            Toast.makeText(this, "Password should be 6 or more characters and both passwords should match", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create user in Firebase Auth
        mAuth.createUserWithEmailAndPassword(emailText, passwordText)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        // Store user details in Firestore
                        storeUserDataInFirestore(user.getUid(), fullNameText, emailText, regNumText, genderText);
                    } else {
                        Toast.makeText(StudentRegisterActivity.this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void storeUserDataInFirestore(String userId, String fullName, String email, String regNum, String gender) {
        // Create a new user map
        Map<String, Object> user = new HashMap<>();
        user.put("fullName", fullName);
        user.put("email", email);
        user.put("regNum", regNum);
        user.put("gender", gender);
        user.put("role", "student");

        // Check if a user with the same email already exists
        db.collection("student_details")
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        Toast.makeText(StudentRegisterActivity.this, "User with this email already exists", Toast.LENGTH_SHORT).show();
                    } else {
                        // Update the existing doc in the "student_details" collection
                        db.collection("student_details")
                                .document(userId)
                                .set(user)
                                .addOnSuccessListener(documentReference -> {
                                    Toast.makeText(StudentRegisterActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(StudentRegisterActivity.this, StudentLoginActivity.class);
                                    startActivity(intent);
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(StudentRegisterActivity.this, "Error updating document: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    Log Log = null;
                                    Log.e("FirestoreError", "Error updating document", e);
                                });
                    }
                });
    }
}
