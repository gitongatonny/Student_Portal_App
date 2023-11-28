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

import android.widget.ImageView;
import android.widget.Toast;


public class StudentRegisterActivity extends AppCompatActivity {

    private EditText fullName, email, regNum, password, confirmPassword;
    private RadioGroup genderRadioGroup;
    private RadioButton selectedGender;
    private MaterialButton registerButton;
    private TextView loginLink;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private ImageView visibilityTogglePass;
    private ImageView visibilityToggleConfirmPass;
    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_register);

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

        // View hidden pass
        visibilityTogglePass = findViewById(R.id.visibility_toggle_pass);
        visibilityToggleConfirmPass = findViewById(R.id.visibility_toggle_confirm_pass);

        // Register Btn
        registerButton.setOnClickListener(view -> registerUser());

        // Toggle visibility for Pass
        visibilityTogglePass.setOnClickListener(view -> {
            isPasswordVisible = !isPasswordVisible;
            togglePasswordVisibility(password, isPasswordVisible, visibilityTogglePass);
        });

        // Toggle visibility for Confirm Pass
        visibilityToggleConfirmPass.setOnClickListener(view -> {
            isConfirmPasswordVisible = !isConfirmPasswordVisible;
            togglePasswordVisibility(confirmPassword, isConfirmPasswordVisible, visibilityToggleConfirmPass);
        });



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

        // Validate reg num format
        if (!isValidRegNumber(regNumText)) {
            Toast.makeText(this, "Reg Format is: C026-01-XXXX/20XX", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if the reg num is already allocated to another student
        checkIfRegNumberExists(regNumText, () -> {
            // If the registration number is unique, create user in Firebase Auth
            mAuth.createUserWithEmailAndPassword(emailText, passwordText)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            // Store user details in Firestore DB
                            storeUserDataInFirestore(user.getUid(), fullNameText, emailText, regNumText, genderText);
                        } else {
                            Toast.makeText(StudentRegisterActivity.this, "Auth failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    //Logic for valid reg: C026-01-XXXX/20XX
    private boolean isValidRegNumber(String regNumber) {
        String regPattern = "C026-01-(?:\\d{4})/20(?:\\d{2})";
        return regNumber.matches(regPattern);
    }

    private void checkIfRegNumberExists(String regNumber, OnSuccessCallback onSuccessCallback) {
        db.collection("student_details")
                .whereEqualTo("regNum", regNumber)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        // Reg num already exists
                        Toast.makeText(StudentRegisterActivity.this, "Reg number is already allocated to another student", Toast.LENGTH_SHORT).show();
                    } else {
                        // Reg num is unique
                        onSuccessCallback.onSuccess();
                    }
                });
    }

    // Callback interface for onSuccess
    private interface OnSuccessCallback {
        void onSuccess();
    }





    private void storeUserDataInFirestore(String userId, String fullName, String email, String regNum, String gender) {
        // Create a new user map
        Map<String, Object> user = new HashMap<>();
        user.put("fullName", fullName);
        user.put("email", email);
        user.put("regNum", regNum);
        user.put("gender", gender);
        user.put("role", "student");

        // Update the existing doc for student's details
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



    private void togglePasswordVisibility(EditText editText, boolean isVisible, ImageView visibilityToggle) {
        if (isVisible) {
            editText.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            visibilityToggle.setImageResource(R.drawable.ic_visibility_on);
        } else {
            editText.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
            visibilityToggle.setImageResource(R.drawable.ic_visibility_off);
        }
        editText.setSelection(editText.length());
    }


}
