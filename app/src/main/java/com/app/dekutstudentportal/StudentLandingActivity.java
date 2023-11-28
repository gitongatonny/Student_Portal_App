package com.app.dekutstudentportal;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class StudentLandingActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private Spinner categorySpinner;

    private MaterialButton studentLogoutBtn, registerCourseBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_landing);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        categorySpinner = findViewById(R.id.categorySpinner);
        studentLogoutBtn = findViewById(R.id.studentLogoutBtn);
        registerCourseBtn = findViewById(R.id.registerCourseBtn);

        // Logout Btn
        studentLogoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Log out the student
                mAuth.signOut();
                // Navigate back to the login page
                finish();
            }
        });

        fetchAndPopulateCategories();

        // Check if the student has already registered for a sem
        checkSemesterRegistrationStatus();

        // Reg Course Btn
        registerCourseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Add the selected course to the student's details
                registerSelectedCourse();
            }
        });
    }

    // Fetch and display the semesters
    private void fetchAndPopulateCategories() {
        db.collection("courses")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> categories = new ArrayList<>();
                        for (DocumentSnapshot document : task.getResult()) {
                            categories.add(document.getId());
                        }

                        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
                        categorySpinner.setAdapter(categoryAdapter);
                    } else {
                        Toast.makeText(this, "Failed to fetch semesters", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkSemesterRegistrationStatus() {
        // Check if the student has already registered for a semester
        db.collection("student_details")
                .document(mAuth.getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot studentDocument = task.getResult();
                        if (studentDocument.exists() && studentDocument.contains("current_semester")) {
                            // Student has already registered for a semester
                            String registeredSemester = studentDocument.getString("current_semester");
                            Toast.makeText(this, "You have already registered for " + registeredSemester, Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(StudentLandingActivity.this, StudentUnitSelection.class);
                            startActivity(intent);
                            finish();
                        }
                    } else {
                        Toast.makeText(this, "Failed to check your registration status", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void registerSelectedCourse() {
        // Get the selected semester from the categorySpinner
        String selectedSemester = categorySpinner.getSelectedItem().toString();

        // Update the student's details in the Firestore DB
        db.collection("student_details")
                .document(mAuth.getUid())
                .update("current_semester", selectedSemester)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Semester registered successfully", Toast.LENGTH_SHORT).show();

                        // Redirect to StudentUnitSelection activity
                        Intent intent = new Intent(StudentLandingActivity.this, StudentUnitSelection.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(this, "Failed to register semester", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
