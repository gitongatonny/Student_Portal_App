package com.app.dekutstudentportal;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class GenerateReportsActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_reports);

        db = FirebaseFirestore.getInstance(); // Initialize Firestore
        mAuth = FirebaseAuth.getInstance();

        // Pass mark
        final int PASS_MARK = 40;

        // Pass All Courses in a Semester Button
        Button passAllCoursesSemesterBtn = findViewById(R.id.passAllCoursesSemesterBtn);
        passAllCoursesSemesterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                generateReport("PassAllCoursesSemester");
            }
        });

        // Pass All Courses in a Year Button
        Button passAllCoursesYearBtn = findViewById(R.id.passAllCoursesYearBtn);
        passAllCoursesYearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                generateReport("PassAllCoursesYear");
            }
        });

        // Failed Courses in a Semester Button
        Button failCoursesSemesterBtn = findViewById(R.id.failCoursesSemesterBtn);
        failCoursesSemesterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                generateReport("FailCoursesSemester");
            }
        });

        // Failed Courses in a Year Button
        Button failCoursesYearBtn = findViewById(R.id.failCoursesYearBtn);
        failCoursesYearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                generateReport("FailCoursesYear");
            }
        });

        // Special Cases Button
        Button specialCasesBtn = findViewById(R.id.specialCasesBtn);
        specialCasesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                generateReport("SpecialCases");
            }
        });

        // Students with Missing Marks Button
        Button missingMarksBtn = findViewById(R.id.missingMarksBtn);
        missingMarksBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                generateReport("MissingMarks");
            }
        });

        // Students Missing All Marks Button
        Button missingAllMarksBtn = findViewById(R.id.missingAllMarksBtn);
        missingAllMarksBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                generateReport("MissingAllMarks");
            }
        });

        // Students Attempting a Course for the Second Time Button
        Button secondAttemptBtn = findViewById(R.id.secondAttemptBtn);
        secondAttemptBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                generateReport("SecondAttempt");
            }
        });

        // Admin Logout Button
        Button generateReportsLogoutBtn = findViewById(R.id.generateReportsLogoutBtn);
        generateReportsLogoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logoutAdmin();
            }
        });
    }

    private void logoutAdmin() {
        mAuth.signOut();

        // Navigate to the MainActivity
        Intent intent = new Intent(GenerateReportsActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish(); // Finish the current activity
    }

    private void generateReport(String reportType) {
        switch (reportType) {
            case "PassAllCoursesSemester":
                generatePassAllCoursesSemesterReport();
                break;

            case "PassAllCoursesYear":
                generatePassAllCoursesYearReport();
                break;

            case "FailCoursesSemester":
                generateFailCoursesSemesterReport();
                break;

            case "FailCoursesYear":
                generateFailCoursesYearReport();
                break;

            case "SpecialCases":
                generateSpecialCasesReport();
                break;

            case "MissingMarks":
                generateMissingMarksReport();
                break;

            case "MissingAllMarks":
                generateMissingAllMarksReport();
                break;

            case "SecondAttempt":
                generateSecondAttemptReport();
                break;

            default:
                break;
        }
    }

    // Methods for each report type
    private void generatePassAllCoursesSemesterReport() {
        int totalStudents = getTotalStudents();
        int passedStudents = getPassedStudents();

        int failedStudents = totalStudents - passedStudents;

        showToast("Pass All Courses in a Semester Report:\n" +
                "Total Students: " + totalStudents + "\n" +
                "Passed: " + passedStudents + "\n" +
                "Failed: " + failedStudents);
    }

    private void generatePassAllCoursesYearReport() {
        int totalStudents = getTotalStudents();
        int passedStudents = getPassedStudents();

        int failedStudents = totalStudents - passedStudents;

        showToast("Pass All Courses in a Year Report:\n" +
                "Total Students: " + totalStudents + "\n" +
                "Passed: " + passedStudents + "\n" +
                "Failed: " + failedStudents);
    }

    private void generateFailCoursesSemesterReport() {
        int totalStudents = getTotalStudents();
        int failedStudents = getFailedStudents();

        int passedStudents = totalStudents - failedStudents;

        showToast("Fail Courses in a Semester Report:\n" +
                "Total Students: " + totalStudents + "\n" +
                "Passed: " + passedStudents + "\n" +
                "Failed: " + failedStudents);
    }

    private void generateFailCoursesYearReport() {
        int totalStudents = getTotalStudents();
        int failedStudents = getFailedStudents();

        int passedStudents = totalStudents - failedStudents;

        showToast("Fail Courses in a Year Report:\n" +
                "Total Students: " + totalStudents + "\n" +
                "Passed: " + passedStudents + "\n" +
                "Failed: " + failedStudents);
    }

    private void generateSpecialCasesReport() {
        int specialCases = getSpecialCases();

        showToast("Special Cases Report:\n" +
                "Total Special Cases: " + specialCases);
    }

    private void generateMissingMarksReport() {
            int missingMarksStudents = generateMissingAllMarksReport();

        showToast("Missing Marks Report:\n" +
                "Total Students with Missing Marks: " + missingMarksStudents);
    }

    private int generateMissingAllMarksReport() {
        int missingAllMarksStudents = generateMissingAllMarksReport();

        showToast("Missing All Marks Report:\n" +
                "Total Students Missing All Marks: " + missingAllMarksStudents);
        return missingAllMarksStudents;
    }

    private void generateSecondAttemptReport() {
        int secondAttemptStudents = getSecondAttemptStudents();

        showToast("Second Attempt Report:\n" +
                "Total Students Attempting a Course for the Second Time: " + secondAttemptStudents);
    }

    private void showToast(String message) {
        // Replace with actual logic to show a toast message
         Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

// Methods for actual data retrieval logic

    private int getTotalStudents() {
        // Retrieve total students from Firestore
        db.collection("student_details")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        int totalStudents = task.getResult().size();
                        showToast("Total Students: " + totalStudents);
                    } else {
                        showToast("Failed to retrieve total students.");
                    }
                });
        return 0;
    }

    private int getPassedStudents() {
        // Retrieve passed students from Firestore
        db.collection("marks")
                .whereGreaterThanOrEqualTo("examScore", 40)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        int passedStudents = task.getResult().size();
                        showToast("Passed Students: " + passedStudents);
                    } else {
                        showToast("Failed to retrieve passed students.");
                    }
                });
        return 0;
    }

    private int getFailedStudents() {
        // Retrieve failed students from Firestore
        db.collection("marks")
                .whereLessThan("examScore", 40)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        int failedStudents = task.getResult().size();
                        showToast("Failed Students: " + failedStudents);
                    } else {
                        showToast("Failed to retrieve failed students.");
                    }
                });
        return 0;
    }

    private int getSpecialCases() {
        // Retrieve special cases from Firestore
        db.collection("marks")
                .whereEqualTo("examScore", null)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        int specialCases = task.getResult().size();
                        showToast("Special Cases: " + specialCases);
                    } else {
                        showToast("Failed to retrieve special cases.");
                    }
                });
        return 0;
    }

    private void getMissingMarksStudents() {
        // Retrieve students with missing marks from Firestore
        db.collection("marks")
                .whereEqualTo("assignment1", null)
                .whereEqualTo("assignment2", null)
                .whereEqualTo("cat1", null)
                .whereEqualTo("cat2", null)
                .whereEqualTo("examScore", null)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        int missingMarksStudents = task.getResult().size();
                        showToast("Students with Missing Marks: " + missingMarksStudents);
                    } else {
                        showToast("Failed to retrieve students with missing marks.");
                    }
                });
    }

    private int getSecondAttemptStudents() {
        // Retrieve second attempt students from Firestore
        db.collection("marks")
                .whereLessThan("examScore", 40)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        int secondAttemptStudents = task.getResult().size();
                        showToast("Students Attempting a Course for the Second Time: " + secondAttemptStudents);
                    } else {
                        showToast("Failed to retrieve students attempting a course for the second time.");
                    }
                });
        return 0;
    }


}
