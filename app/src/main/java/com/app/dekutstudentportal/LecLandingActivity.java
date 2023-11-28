package com.app.dekutstudentportal;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
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

public class LecLandingActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private Spinner categorySpinner, courseSpinner;
    private MaterialButton lecLogoutBtn, regLecCourseBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lec_landing);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        categorySpinner = findViewById(R.id.categorySpinner);
        courseSpinner = findViewById(R.id.courseSpinner);
        lecLogoutBtn = findViewById(R.id.lecLogoutBtn);
        regLecCourseBtn = findViewById(R.id.regLecCourseBtn);

        // Logout Btn
        lecLogoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
                finish();
            }
        });

        // Reg Course Btn
        regLecCourseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Add the selected course to the lec's details
                registerSelectedCourse();
            }
        });

        fetchAndPopulateCategories();

        categorySpinner.setOnItemSelectedListener(new CustomOnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // Fetch and populate courses based on the selected category
                fetchAndPopulateCourses(categorySpinner.getSelectedItem().toString());
            }
        });

        // Check if the user has already registered for a course
        checkRegistrationStatus();
    }

    private void checkRegistrationStatus() {
        db.collection("lecturer_details")
                .document(mAuth.getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot lecDocument = task.getResult();
                        if (lecDocument.exists() && lecDocument.contains("teaching_course")) {
                            String registeredCourse = lecDocument.getString("teaching_course");
                            Toast.makeText(this, "You have already registered for " + registeredCourse, Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(LecLandingActivity.this, LecPortalActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    } else {
                        Toast.makeText(this, "Failed to check registration status", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchAndPopulateCategories() {
        // Fetch Courses
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
                        Toast.makeText(this, "Failed to fetch categories", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchAndPopulateCourses(String selectedCategory) {
        // Fetch Units
        db.collection("courses")
                .document(selectedCategory)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            List<String> courses = new ArrayList<>();
                            for (String course : document.getData().keySet()) {
                                courses.add(course);
                            }

                            ArrayAdapter<String> courseAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, courses);
                            courseSpinner.setAdapter(courseAdapter);
                        } else {
                            Toast.makeText(this, "Category does not exist", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Failed to fetch courses", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void registerSelectedCourse() {
        String selectedCourse = courseSpinner.getSelectedItem().toString();
        String selectedSemester = categorySpinner.getSelectedItem().toString();

        // Check if the selected course is already registered by another lecturer
        db.collection("lecturer_details")
                .whereEqualTo("teaching_course", selectedCourse)
                .whereEqualTo("teaching_semester", selectedSemester)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            Toast.makeText(this, "This course is already registered by another lecturer", Toast.LENGTH_SHORT).show();
                        } else {
                            // Course is not registered by another lecturer, proceed with registration
                            performCourseRegistration(selectedCourse, selectedSemester);
                        }
                    } else {
                        Toast.makeText(this, "Failed to check course registration status", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void performCourseRegistration(String selectedCourse, String selectedSemester) {
        // Update the lec's details in Firestore DB
        db.collection("lecturer_details")
                .document(mAuth.getUid())
                .update("teaching_course", selectedCourse, "teaching_semester", selectedSemester)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Course registered successfully", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(LecLandingActivity.this, LecPortalActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(this, "Failed to register course", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
