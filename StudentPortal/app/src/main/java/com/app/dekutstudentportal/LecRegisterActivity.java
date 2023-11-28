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
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class LecRegisterActivity extends AppCompatActivity {

    private EditText name, email, phone, password, confirmPassword;
    private RadioGroup genderRadioGroup;
    private RadioButton selectedGender;
    private MaterialButton registerBtn;
    private TextView loginLink;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lec_register);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize Views
        name = findViewById(R.id.lecName);
        email = findViewById(R.id.lecEmail);
        phone = findViewById(R.id.lecPhone);
        genderRadioGroup = findViewById(R.id.lecGenderRadioGroup);
        password = findViewById(R.id.lecPass);
        confirmPassword = findViewById(R.id.lecConfirmPass);
        registerBtn = findViewById(R.id.lecRegisterBtn);
        loginLink = findViewById(R.id.lec_loginNow);

        // Register Btn
        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerUser();
            }
        });

        // Already registered Link [to Login]
        loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LecRegisterActivity.this, LecLoginActivity.class);
                startActivity(intent);
            }
        });
    }

    private void registerUser() {
        // Get user inputs
        String nameText = name.getText().toString().trim();
        String emailText = email.getText().toString().trim();
        String phoneText = phone.getText().toString().trim();
        int selectedGenderId = genderRadioGroup.getCheckedRadioButtonId();
        selectedGender = findViewById(selectedGenderId);
        String genderText = selectedGender.getText().toString();
        String passwordText = password.getText().toString().trim();
        String confirmPasswordText = confirmPassword.getText().toString().trim();

        // Validate inputs
        if (TextUtils.isEmpty(nameText) || TextUtils.isEmpty(emailText) ||
                TextUtils.isEmpty(phoneText) || TextUtils.isEmpty(genderText) ||
                TextUtils.isEmpty(passwordText) || TextUtils.isEmpty(confirmPasswordText)) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate email format
        if (!emailText.endsWith("dkut.ac.ke")) {
            Toast.makeText(this, "Email should end with 'dkut.ac.ke'", Toast.LENGTH_SHORT).show();
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
                        storeUserDataInFirestore(user.getUid(), nameText, emailText, phoneText, genderText);
                    } else {
                        Toast.makeText(LecRegisterActivity.this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void storeUserDataInFirestore(String userId, String name, String email, String phone, String gender) {
        // Create a new user map
        Map<String, Object> user = new HashMap<>();
        user.put("name", name);
        user.put("email", email);
        user.put("phone", phone);
        user.put("gender", gender);
        user.put("role", "lecturer");

        // Check if a user with the same email already exists
        db.collection("lecturer_details")
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        Toast.makeText(LecRegisterActivity.this, "User with this email already exists", Toast.LENGTH_SHORT).show();
                    } else {
                        // Update the existing doc in the "lecturer_details" collection
                        db.collection("lecturer_details")
                                .document(userId)
                                .set(user)
                                .addOnSuccessListener(documentReference -> {
                                    Toast.makeText(LecRegisterActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(LecRegisterActivity.this, LecLoginActivity.class);
                                    startActivity(intent);
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(LecRegisterActivity.this, "Error updating document: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    Log Log = null;
                                    Log.e("FirestoreError", "Error updating document", e);
                                });
                    }
                });
    }
}
