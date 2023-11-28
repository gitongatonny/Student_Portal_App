package com.app.dekutstudentportal;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class StudentLoginActivity extends AppCompatActivity {

    private EditText studentEmailEditText, studentPassEditText;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_login);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        studentEmailEditText = findViewById(R.id.studentEmail);
        studentPassEditText = findViewById(R.id.studentPass);

        MaterialButton studentLoginBtn = findViewById(R.id.studentLoginBtn);

        studentLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginUser();
            }
        });

        // Register Btn
        MaterialButton studentRegisterBtn = findViewById(R.id.student_registerbtn);

        studentRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(StudentLoginActivity.this, StudentRegisterActivity.class);
                startActivity(intent);
            }
        });

        // Check if a user is already logged in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            checkIfStudentUserExists(currentUser.getEmail());
        }
    }

    private void loginUser() {
        // Get user inputs
        String emailText = studentEmailEditText.getText().toString().trim();
        String passwordText = studentPassEditText.getText().toString().trim();

        // Validate inputs
        if (TextUtils.isEmpty(emailText) || TextUtils.isEmpty(passwordText)) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate email format
        if (!emailText.endsWith("students.dkut.ac.ke")) {
            Toast.makeText(this, "Email should end with 'students.dkut.ac.ke'", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate password length
        if (passwordText.length() < 6) {
            Toast.makeText(this, "Password should be 6 or more characters", Toast.LENGTH_SHORT).show();
            return;
        }

        // Authenticate user with Firebase
        mAuth.signInWithEmailAndPassword(emailText, passwordText)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        checkIfStudentUserExists(emailText);
                    } else {
                        Toast.makeText(StudentLoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkIfStudentUserExists(String email) {
        db.collection("student_details")
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            Toast.makeText(StudentLoginActivity.this, "Login is a success!!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(StudentLoginActivity.this, StudentLandingActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(StudentLoginActivity.this, "Only students can log in", Toast.LENGTH_SHORT).show();
                            mAuth.signOut(); // Sign out the user as they are not authorized
                        }
                    } else {
                        Toast.makeText(StudentLoginActivity.this, "Error checking user authorization", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
