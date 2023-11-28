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

public class LecLoginActivity extends AppCompatActivity {

    private EditText lecEmailEditText, lecPassEditText;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lec_login);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        lecEmailEditText = findViewById(R.id.lecEmail);
        lecPassEditText = findViewById(R.id.lecPass);

        MaterialButton lecSigninBtn = findViewById(R.id.lecSiginBtn);

        lecSigninBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginUser();
            }
        });

        // Register Btn
        MaterialButton lecSignupBtn = findViewById(R.id.lecSignupBtn);

        lecSignupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LecLoginActivity.this, LecRegisterActivity.class);
                startActivity(intent);
            }
        });

        // Check if a user is already logged in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            checkIfLecturerUserExists(currentUser.getEmail());
        }
    }

    private void loginUser() {
        // Get user inputs
        String emailText = lecEmailEditText.getText().toString().trim();
        String passwordText = lecPassEditText.getText().toString().trim();

        // Validate inputs
        if (TextUtils.isEmpty(emailText) || TextUtils.isEmpty(passwordText)) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate email format
        if (!emailText.endsWith("dkut.ac.ke")) {
            Toast.makeText(this, "Email should end with 'dkut.ac.ke'", Toast.LENGTH_SHORT).show();
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
                        checkIfLecturerUserExists(emailText);
                    } else {
                        Toast.makeText(LecLoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkIfLecturerUserExists(String email) {
        // Admin Exception
        if (email.equals("admin@dkut.ac.ke")) {
            mAuth.signInWithEmailAndPassword(email, "mseewaIT")
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            Intent intent = new Intent(LecLoginActivity.this, AdminPortalActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(LecLoginActivity.this, "Authentication failed for admin", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            // Authenticate lecturer login details
            db.collection("lecturer_details")
                    .whereEqualTo("email", email)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            if (!task.getResult().isEmpty()) {
                                Intent intent = new Intent(LecLoginActivity.this, LecLandingActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(LecLoginActivity.this, "You are not authorized", Toast.LENGTH_SHORT).show();
                                mAuth.signOut(); // Sign out the user as they are not authorized
                            }
                        } else {
                            Toast.makeText(LecLoginActivity.this, "Error checking user authorization", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}
