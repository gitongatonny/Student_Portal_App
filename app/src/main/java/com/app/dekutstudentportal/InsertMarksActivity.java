package com.app.dekutstudentportal;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class InsertMarksActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String selectedStudent;
    private String selectedStudentUID;
    private String teachingCourse;
    private boolean marksEntered = false;

    private TextView studentNameTextView, studentRegTextView;
    private EditText assignment1EditText, assignment2EditText, cat1EditText, cat2EditText, examScoreEditText, overallScoreEditText, gradeEditText;
    private Button saveMarksButton;

    private static class Marks {
        private String assignment1;
        private String assignment2;
        private String cat1;
        private String cat2;
        private String examScore;
        private int overallScore; // New field
        private String grade; // New field
        private boolean passedFirstAttempt; // New field
        private boolean failedFirstAttempt; // New field

        // Constructor
        public Marks(String assignment1, String assignment2, String cat1, String cat2, String examScore, int overallScore, String grade) {
            this.assignment1 = assignment1;
            this.assignment2 = assignment2;
            this.cat1 = cat1;
            this.cat2 = cat2;
            this.examScore = examScore;
            this.overallScore = overallScore;
            this.grade = grade;
            this.passedFirstAttempt = passedFirstAttempt;
            this.failedFirstAttempt = failedFirstAttempt;
        }

        // Getters for marks' fields
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

        public int getOverallScore() {
            return overallScore;
        }

        public String getGrade() {
            return grade;
        }

        // Getters and setters for the new fields
        public boolean isPassedFirstAttempt() {
            return passedFirstAttempt;
        }

        public void setPassedFirstAttempt(boolean passedFirstAttempt) {
            this.passedFirstAttempt = passedFirstAttempt;
        }

        public boolean isFailedFirstAttempt() {
            return failedFirstAttempt;
        }

        public void setFailedFirstAttempt(boolean failedFirstAttempt) {
            this.failedFirstAttempt = failedFirstAttempt;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insert_marks);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();



        // Fetch the Students' Names and UIDs
        selectedStudent = getIntent().getStringExtra("selectedStudent");
        selectedStudentUID = getIntent().getStringExtra("selectedStudentUID");

        // Initialize Views
        studentNameTextView = findViewById(R.id.studentNameTextView);
        studentRegTextView = findViewById(R.id.studentRegTextView);
        assignment1EditText = findViewById(R.id.assignment1EditText);
        assignment2EditText = findViewById(R.id.assignment2EditText);
        cat1EditText = findViewById(R.id.cat1EditText);
        cat2EditText = findViewById(R.id.cat2EditText);
        examScoreEditText = findViewById(R.id.examScoreEditText);
        overallScoreEditText = findViewById(R.id.overallScoreEditText);
        gradeEditText = findViewById(R.id.gradeEditText);
        saveMarksButton = findViewById(R.id.saveMarksButton);

        // Set the student name text
        studentNameTextView.setText("Student Name: " + selectedStudent);

        fetchAndSetRegNumber(selectedStudentUID);

        // Save Marks Btn
        saveMarksButton.setOnClickListener(view -> saveMarks());

        // Fetch and set the student's marks if already stored
        fetchAndSetStudentMarks();

        //  Disable editing if marks have already been entered.
        if (marksEntered) {
            disableEditing();
        }
    }

    private void fetchAndSetRegNumber(String studentUID) {
        DocumentReference studentDocRef = db.collection("student_details").document(studentUID);

        studentDocRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot documentSnapshot = task.getResult();
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    String regNumber = documentSnapshot.getString("regNum");
                    studentRegTextView.setText("Reg Number: " + regNumber);
                }
            }
        });
    }




    private void fetchAndSetStudentMarks() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String currentUserId = currentUser.getUid();
            DocumentReference lecturerDoc = db.collection("lecturer_details").document(currentUserId);

            lecturerDoc.get().addOnCompleteListener(lecturerTask -> {
                if (lecturerTask.isSuccessful() && lecturerTask.getResult().exists()) {
                    String teachingSemester = lecturerTask.getResult().getString("teaching_semester");
                    String teachingCourse = lecturerTask.getResult().getString("teaching_course");

                    DocumentReference studentDoc = db.collection("scores")
                            .document(teachingSemester + " " + "Units")
                            .collection(teachingCourse)
                            .document(selectedStudent);

                    studentDoc.get().addOnCompleteListener(studentTask -> {
                        if (studentTask.isSuccessful()) {
                            DocumentSnapshot documentSnapshot = studentTask.getResult();
                            if (documentSnapshot != null && documentSnapshot.exists()) {
                                // Marks already exist, set the marksEntered flag
                                marksEntered = true;
                                // Disable editing
                                disableEditing();
                                // If condition for displaying marks
                                if (marksEntered) {
                                    // Retrieval of marks [If already stored]
                                    String assignment1 = documentSnapshot.getString("assignment1");
                                    String assignment2 = documentSnapshot.getString("assignment2");
                                    String cat1 = documentSnapshot.getString("cat1");
                                    String cat2 = documentSnapshot.getString("cat2");
                                    String examScore = documentSnapshot.getString("examScore");
                                    String grade = documentSnapshot.getString("grade");
                                    int overallScore = documentSnapshot.getLong("overallScore") != null
                                            ? Math.toIntExact(documentSnapshot.getLong("overallScore"))
                                            : 0;

                                    // Display the student marks
                                    displayStudentMarks(new Marks(assignment1, assignment2, cat1, cat2, examScore, overallScore, grade));

                                    Toast.makeText(this, "Marks for this student have already been entered.", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(this, "No stored marks for this student.", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                // Failed to fetch marks
                                Toast.makeText(this, "There are no stored marks for this student.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            });
        }
    }




    private void displayStudentMarks(Marks studentMarks) {
        assignment1EditText.setText(studentMarks.getAssignment1());
        assignment2EditText.setText(studentMarks.getAssignment2());
        cat1EditText.setText(studentMarks.getCat1());
        cat2EditText.setText(studentMarks.getCat2());
        examScoreEditText.setText(studentMarks.getExamScore());
        overallScoreEditText.setText(String.valueOf(studentMarks.getOverallScore()));
        gradeEditText.setText(studentMarks.getGrade());
    }

    private boolean isValidScore(int score, int min, int max, String fieldName) {
        if (score < min || score > max) {
            Toast.makeText(this, fieldName + " should be between " + min + " and " + max, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void saveMarks() {
        // Check if marks have already been saved
        if (marksEntered) {
            Toast.makeText(this, "Marks have already been entered.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Fetch the current lecturer's details to get the teaching semester and course
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String currentUserId = currentUser.getUid();
            DocumentReference lecturerDoc = db.collection("lecturer_details").document(currentUserId);

            lecturerDoc.get().addOnCompleteListener(lecturerTask -> {
                if (lecturerTask.isSuccessful() && lecturerTask.getResult().exists()) {
                    String teachingSemester = lecturerTask.getResult().getString("teaching_semester");
                    String teachingCourse = lecturerTask.getResult().getString("teaching_course");

                    DocumentReference semesterDoc = db.collection("scores")
                            .document(teachingSemester + " " + "Units")
                            .collection(teachingCourse)
                            .document(selectedStudent);

                    // Check if the student's marks already exist
                    semesterDoc.get().addOnCompleteListener(studentTask -> {
                        if (studentTask.isSuccessful()) {
                            DocumentSnapshot documentSnapshot = studentTask.getResult();
                            if (documentSnapshot != null && documentSnapshot.exists()) {
                                // Marks already exist, display them in the fields
                                Marks studentMarks = documentSnapshot.toObject(Marks.class);
                                if (studentMarks != null) {
                                    displayStudentMarks(studentMarks);

                                    // Update the marksEntered flag
                                    marksEntered = true;

                                    Toast.makeText(this, "Marks for this student have already been entered.", Toast.LENGTH_SHORT).show();

                                    // Disable editing
                                    disableEditing();
                                }
                            } else {
                                // Marks don't exist, proceed to save
                                proceedToSaveMarks(semesterDoc);
                            }
                        } else {
                            // Fetching the student's marks failed
                            Toast.makeText(this, "Failed to fetch student marks.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        }
    }

    // Helper method to proceed with saving marks
    private void proceedToSaveMarks(DocumentReference semesterDoc) {
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

        // Validate and calculate overall score
        int assignment1Score = Integer.parseInt(assignment1);
        int assignment2Score = Integer.parseInt(assignment2);
        int cat1Score = Integer.parseInt(cat1);
        int cat2Score = Integer.parseInt(cat2);
        int examScoreValue = Integer.parseInt(examScore);

        if (!isValidScore(assignment1Score, 0, 10, "Assignment 1") ||
                !isValidScore(assignment2Score, 0, 10, "Assignment 2") ||
                !isValidScore(cat1Score, 0, 30, "CAT 1") ||
                !isValidScore(cat2Score, 0, 30, "CAT 2") ||
                !isValidScore(examScoreValue, 0, 70, "Exam Score")) {
            // Invalid score range. Toast messages will be displayed in the isValidScore function.
            return;
        }

        // Calculate overall score and grade
        int overallScore = calculateOverallScore(assignment1Score, assignment2Score, cat1Score, cat2Score, examScoreValue);
        String grade = calculateGrade(overallScore);

        Marks marks = new Marks(assignment1, assignment2, cat1, cat2, examScore, overallScore, grade);

        // Add logic for passedFirstAttempt and failedFirstAttempt fields
        boolean passedFirstAttempt = grade.equals("C") || grade.equals("B") || grade.equals("A");
        boolean failedFirstAttempt = grade.equals("E");

        // Set the new fields in the Marks object
        marks.setPassedFirstAttempt(passedFirstAttempt);
        marks.setFailedFirstAttempt(failedFirstAttempt);


        semesterDoc
                .set(marks)
                .addOnSuccessListener(aVoid -> {
                    // Successfully saved
                    Toast.makeText(this, "Marks saved successfully.", Toast.LENGTH_SHORT).show();
                    overallScoreEditText.setText(String.valueOf(overallScore));
                    gradeEditText.setText(grade);

                    // Disable editing of fields and button after saving
                    disableEditing();

                    // Update the marksEntered flag
                    marksEntered = true;
                })
                .addOnFailureListener(e -> {
                    // Failed to save
                    Toast.makeText(this, "Failed to save marks.", Toast.LENGTH_SHORT).show();
                });
    }


    private String calculateGrade(int overallScore) {
        // Calculate grade based on the specified ranges
        if (overallScore >= 0 && overallScore <= 40) {
            return "E";
        } else if (overallScore > 40 && overallScore <= 50) {
            return "D";
        } else if (overallScore > 50 && overallScore <= 60) {
            return "C";
        } else if (overallScore > 60 && overallScore <= 70) {
            return "B";
        } else if (overallScore > 70 && overallScore <= 100) {
            return "A";
        }
        return "";
    }

    private void disableEditing() {
        // Disable editing of all EditText fields and the button
        assignment1EditText.setEnabled(false);
        assignment2EditText.setEnabled(false);
        cat1EditText.setEnabled(false);
        cat2EditText.setEnabled(false);
        examScoreEditText.setEnabled(false);
        overallScoreEditText.setEnabled(false);
        gradeEditText.setEnabled(false);
        saveMarksButton.setEnabled(false);
    }

    private int calculateOverallScore(int assignment1, int assignment2, int cat1, int cat2, int examScore) {
        // Calculate overall score
        int overallScore = examScore + ((assignment1 + assignment2 + cat1 + cat2) * 3/8);
        return overallScore;
    }
}
