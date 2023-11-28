package com.app.dekutstudentportal;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class SelectStudentActivity extends AppCompatActivity {

    private ListView studentListView;
    private FirebaseFirestore db;
    private String teachingCourse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_student);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize Views
        studentListView = findViewById(R.id.studentListView);




        // Retrieve teachingCourse value
        Intent intent = getIntent();
        if (intent.hasExtra("teachingCourse")) {
            teachingCourse = intent.getStringExtra("teachingCourse");
        } else {
            // Handle the case where teachingCourse is not provided
            Toast.makeText(this, "Teaching course not provided.", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Set the Course Variable Heading
        TextView viewMarksTitle = findViewById(R.id.lecPortalCourseName);
        viewMarksTitle.setText("Course: " + teachingCourse);

        fetchAndDisplayStudentList();
    }



    // Fetch and display the list of students who selected the current course
    private void fetchAndDisplayStudentList() {
        db.collection("student_details")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // List to store student names and UIDs
                        ArrayList<String> studentNames = new ArrayList<>();
                        ArrayList<String> studentUIDs = new ArrayList<>();

                        int totalStudents = task.getResult().size();
                        AtomicInteger studentsProcessed = new AtomicInteger(0);

                        for (DocumentSnapshot studentDocument : task.getResult()) {
                            String studentUID = studentDocument.getId();
                            String currentSemester = studentDocument.getString("current_semester");

                            // Construct the path to the selected_units doc
                            String pathToSelectedUnits = "student_details/" + studentUID + "/" + currentSemester + " Units/selected_units";

                            // Check if the teachingCourse 'course' exists in the selected_units document
                            checkTeachingCourseInSelectedUnits(pathToSelectedUnits, studentNames, studentUIDs, totalStudents, studentsProcessed);
                        }
                    } else {
                        // Failed
                        Toast.makeText(SelectStudentActivity.this, "Failed to fetch student list.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkTeachingCourseInSelectedUnits(String pathToSelectedUnits, ArrayList<String> studentNames, ArrayList<String> studentUIDs, int totalStudents, AtomicInteger studentsProcessed) {
        db.document(pathToSelectedUnits)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot selectedUnitsDocument = task.getResult();

                        // Check if the teachingCourse exists in the selected_units doc
                        if (selectedUnitsDocument.contains(teachingCourse)) {
                            // If yes, get the student UID and retrieve the student's full name from the "student_details" collection
                            String studentUID = pathToSelectedUnits.split("/")[1];  // Extract student UID from the path
                            fetchFullName(studentUID, studentNames, studentUIDs, totalStudents, studentsProcessed);
                        } else {
                            // If the teachingCourse does not exist, check if all students have been processed
                            checkAllStudentsProcessed(totalStudents, studentsProcessed, studentNames, studentUIDs);
                        }
                    } else {
                        // Handle the error
                        Log.e("SelectStudentActivity", "Failed to check selected_units document. Path: " + pathToSelectedUnits, task.getException());
                        Toast.makeText(SelectStudentActivity.this, "Failed to check selected_units document.", Toast.LENGTH_SHORT).show();
                        // Ensure all students have been processed even in the case of an error
                        checkAllStudentsProcessed(totalStudents, studentsProcessed, studentNames, studentUIDs);
                    }
                });
    }

    private void fetchFullName(String studentUIDForPath, ArrayList<String> studentNames, ArrayList<String> studentUIDs, int totalStudents, AtomicInteger studentsProcessed) {
        String pathToStudentDocument = "student_details/" + studentUIDForPath;

        db.document(pathToStudentDocument)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot studentDetailsDocument = task.getResult();

                        // Check if the doc exists and contains the "fullName" field
                        if (studentDetailsDocument.exists() && studentDetailsDocument.contains("fullName")) {
                            // Get the full name
                            String studentName = studentDetailsDocument.getString("fullName");

                            // Add the student name and UID to the list
                            studentNames.add(studentName);
                            studentUIDs.add(studentUIDForPath);
                        } else {
                            // Handle the case where the doc does not exist or does not contain "fullName"
                            Toast.makeText(SelectStudentActivity.this, "Full name not found for UID: " + studentUIDForPath, Toast.LENGTH_SHORT).show();
                        }

                        // Check if all students have been processed
                        checkAllStudentsProcessed(totalStudents, studentsProcessed, studentNames, studentUIDs);
                    } else {
                        // Handle the error
                        Toast.makeText(SelectStudentActivity.this, "Failed to fetch student details for UID: " + studentUIDForPath, Toast.LENGTH_SHORT).show();
                        // Ensure we check if all students have been processed even in case of an error
                        checkAllStudentsProcessed(totalStudents, studentsProcessed, studentNames, studentUIDs);
                    }
                });
    }

    private void checkAllStudentsProcessed(int totalStudents, AtomicInteger studentsProcessed, ArrayList<String> studentNames, ArrayList<String> studentUIDs) {
        int processedCount = studentsProcessed.incrementAndGet();

        // Check if all students have been processed
        if (processedCount == totalStudents) {
            // Display the updated student names in the ListView
            displayStudentList(studentNames, studentUIDs);
        }
    }



    private void displayStudentList(ArrayList<String> studentNames, ArrayList<String> studentUIDs) {
        // Create an ArrayAdapter to display the student names in the ListView
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, studentNames);

        // Set the adapter for the ListView
        studentListView.setAdapter(adapter);

        // Set a click listener for the ListView items
        studentListView.setOnItemClickListener((parent, view, position, id) -> {
            // Get the selected student name and UID
            String selectedStudent = studentNames.get(position);
            String selectedStudentUID = studentUIDs.get(position);

            // Go to 'Insert Marks' and send the Names and UIDs
            Intent intent = new Intent(SelectStudentActivity.this, InsertMarksActivity.class);
            intent.putExtra("selectedStudent", selectedStudent);
            intent.putExtra("selectedStudentUID", selectedStudentUID);
            startActivity(intent);
        });
    }

}
