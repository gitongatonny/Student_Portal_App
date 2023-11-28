package com.app.dekutstudentportal;

// Import statements
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
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
    private String teachingCourse; // Variable to store the teaching course

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_student);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize Views
        studentListView = findViewById(R.id.studentListView);

        // Retrieve teachingCourse from the intent
        Intent intent = getIntent();
        if (intent.hasExtra("teachingCourse")) {
            teachingCourse = intent.getStringExtra("teachingCourse");
        } else {
            // Handle the case where teachingCourse is not provided
            Toast.makeText(this, "Teaching course not provided.", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Fetch and display the list of students who selected the teachingCourse
        fetchAndDisplayStudentList();
    }

    private void fetchAndDisplayStudentList() {
        // Fetch and display the list of students who selected the teachingCourse
        db.collection("student_details")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // List to store student names
                        ArrayList<String> studentNames = new ArrayList<>();

                        int totalStudents = task.getResult().size();
                        AtomicInteger studentsProcessed = new AtomicInteger(0);

                        for (DocumentSnapshot studentDocument : task.getResult()) {
                            String studentUID = studentDocument.getId();
                            String currentSemester = studentDocument.getString("current_semester");

                            // Construct the path to the selected_units document
                            String pathToSelectedUnits = "student_details/" + studentUID + "/" + currentSemester + " Units/selected_units";

                            // Check if the teachingCourse exists in the selected_units document
                            checkTeachingCourseInSelectedUnits(pathToSelectedUnits, studentNames, totalStudents, studentsProcessed);
                        }
                    } else {
                        // Handle the error
                        Toast.makeText(SelectStudentActivity.this, "Failed to fetch student list.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkTeachingCourseInSelectedUnits(String pathToSelectedUnits, ArrayList<String> studentNames, int totalStudents, AtomicInteger studentsProcessed) {
        db.document(pathToSelectedUnits)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot selectedUnitsDocument = task.getResult();

                        // Check if the teachingCourse exists in the selected_units document
                        if (selectedUnitsDocument.contains(teachingCourse)) {
                            // If yes, get the student UID and retrieve the student's full name from the "student_details" collection
                            String studentUID = selectedUnitsDocument.getId();
                            fetchFullName(studentUID, studentNames, totalStudents, studentsProcessed);
                        } else {
                            // If the teachingCourse does not exist, check if all students have been processed
                            checkAllStudentsProcessed(totalStudents, studentsProcessed, studentNames);
                        }
                    } else {
                        // Handle the error
                        Toast.makeText(SelectStudentActivity.this, "Failed to check selected_units document.", Toast.LENGTH_SHORT).show();
                        // Ensure we check if all students have been processed even in case of an error
                        checkAllStudentsProcessed(totalStudents, studentsProcessed, studentNames);
                    }
                });
    }


    private void fetchFullName(String studentUID, ArrayList<String> studentNames, int totalStudents, AtomicInteger studentsProcessed) {
        db.collection("student_details")
                .document(studentUID)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot studentDetailsDocument = task.getResult();

                        // Get the full name from the "student_details" document
                        String studentName = studentDetailsDocument.getString("fullName");

                        // Add the student name to the list along with their UID
                        studentNames.add(studentName + " (" + studentUID + ")");

                        // Check if all students have been processed
                        checkAllStudentsProcessed(totalStudents, studentsProcessed, studentNames);
                    } else {
                        // Handle the error
                        Toast.makeText(SelectStudentActivity.this, "Failed to fetch student details for UID: " + studentUID, Toast.LENGTH_SHORT).show();
                        // Ensure we check if all students have been processed even in case of an error
                        checkAllStudentsProcessed(totalStudents, studentsProcessed, studentNames);
                    }
                });
    }


    private void checkAllStudentsProcessed(int totalStudents, AtomicInteger studentsProcessed, ArrayList<String> studentNames) {
        int processedCount = studentsProcessed.incrementAndGet();

        // Check if all students have been processed
        if (processedCount == totalStudents) {
            // Display the updated student names in the ListView
            displayStudentList(studentNames);
        }
    }


    private void displayStudentList(ArrayList<String> studentNames) {
        // Create an ArrayAdapter to display the student names in the ListView
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, studentNames);

        // Set the adapter for the ListView
        studentListView.setAdapter(adapter);

        // Set a click listener for the ListView items
        studentListView.setOnItemClickListener((parent, view, position, id) -> {
            // Get the selected student name
            String selectedStudent = studentNames.get(position);

            // Start a new activity to insert marks for the selected student
            Intent intent = new Intent(SelectStudentActivity.this, InsertMarksActivity.class);
            intent.putExtra("selectedStudent", selectedStudent);
            startActivity(intent);
        });
    }
}
