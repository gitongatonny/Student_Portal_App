package com.app.dekutstudentportal;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class InsertMarksActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String selectedStudent;

    private TextView studentNameTextView;
    private EditText assignment1EditText, assignment2EditText, cat1EditText, cat2EditText, examScoreEditText;
    private Button saveMarksButton;

    // Define Marks class here
    private static class Marks {
        private String assignment1;
        private String assignment2;
        private String cat1;
        private String cat2;
        private String examScore;

        // Constructor
        public Marks(String assignment1, String assignment2, String cat1, String cat2, String examScore) {
            this.assignment1 = assignment1;
            this.assignment2 = assignment2;
            this.cat1 = cat1;
            this.cat2 = cat2;
            this.examScore = examScore;
        }

        // Getters (optional, depending on your usage)
        public String getAssignment1() {
            return assignment1;
        }

        public String getAssignment2() {
            return assignment2;
        }

        public String getCat1() {
            return cat1;
        }

        public String getCat2() {
            return cat2;
        }

        public String getExamScore() {
            return examScore;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insert_marks);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Get the selected student from the intent
        selectedStudent = getIntent().getStringExtra("selectedStudent");

        // Initialize Views
        studentNameTextView = findViewById(R.id.studentNameTextView);
        assignment1EditText = findViewById(R.id.assignment1EditText);
        assignment2EditText = findViewById(R.id.assignment2EditText);
        cat1EditText = findViewById(R.id.cat1EditText);
        cat2EditText = findViewById(R.id.cat2EditText);
        examScoreEditText = findViewById(R.id.examScoreEditText);
        saveMarksButton = findViewById(R.id.saveMarksButton);

        // Set the student name text
        studentNameTextView.setText("Student Name: " + selectedStudent);

        // Set Click Listener for Save Marks Button
        saveMarksButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveMarks();
            }
        });
    }

    private void saveMarks() {
        // Get marks from EditText fields
        String assignment1 = assignment1EditText.getText().toString();
        String assignment2 = assignment2EditText.getText().toString();
        String cat1 = cat1EditText.getText().toString();
        String cat2 = cat2EditText.getText().toString();
        String examScore = examScoreEditText.getText().toString();

        // Check if any field is empty
        if (assignment1.isEmpty() || assignment2.isEmpty() || cat1.isEmpty() || cat2.isEmpty() || examScore.isEmpty()) {
            Toast.makeText(this, "Please enter marks for all fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create an instance of the Marks class
        Marks marks = new Marks(assignment1, assignment2, cat1, cat2, examScore);

        // Save the marks to Firestore under the selected student
        DocumentReference studentDocument = db.collection("marks").document(selectedStudent);
        studentDocument
                .set(marks)
                .addOnSuccessListener(aVoid -> {
                    // Successfully saved
                    Toast.makeText(this, "Marks saved successfully.", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    // Failed to save
                    Toast.makeText(this, "Failed to save marks.", Toast.LENGTH_SHORT).show();
                });
    }
}
