// StudentPortalActivity.java
package com.app.dekutstudentportal;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class StudentPortalActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TableLayout studentDetailsTable;
    private TableLayout studentUnitsTable;  // Added TableLayout for student units
    private MaterialButton studentLogoutPortalBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_portal);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        studentDetailsTable = findViewById(R.id.studentDetailsTable);
        studentUnitsTable = findViewById(R.id.studentUnitsTable);  // Initialize studentUnitsTable

        // Add error handling - check if the user is authenticated
        if (mAuth.getCurrentUser() == null) {
            // If not authenticated, return to the login screen
            Toast.makeText(this, "User not authenticated. Please log in.", Toast.LENGTH_SHORT).show();
            navigateToLogin();
            return;
        }

        // Fetch and display the score data
        fetchAndDisplayScoreData();

        // Fetch and display student details
        fetchAndDisplayStudentDetails();

        // Fetch and display selected units
        fetchAndDisplaySelectedUnits();

        studentLogoutPortalBtn = findViewById(R.id.studentLogoutPortalBtn);

        // OnClickListener for the logout button
        studentLogoutPortalBtn.setOnClickListener(v -> logoutAndNavigateToMain());
    }


    private void fetchAndDisplaySelectedUnits() {
        // Fetch the selected units from the Firestore collection
        db.collection("student_details")
                .document(mAuth.getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot studentDocument = task.getResult();
                        if (studentDocument.exists()) {
                            String currentSemester = studentDocument.getString("current_semester");

                            if (currentSemester != null) {
                                String unitsCollectionName = currentSemester + " Units";

                                db.collection("student_details")
                                        .document(mAuth.getUid())
                                        .collection(unitsCollectionName)
                                        .document("selected_units")
                                        .get()
                                        .addOnCompleteListener(unitsTask -> {
                                            if (unitsTask.isSuccessful()) {
                                                DocumentSnapshot unitsDocument = unitsTask.getResult();
                                                if (unitsDocument.exists()) {
                                                    // Selected units data found, populate the table
                                                    populateUnitTable(unitsDocument);
                                                } else {
                                                    // No selected units data found
                                                    Toast.makeText(this, "No selected units found.", Toast.LENGTH_SHORT).show();
                                                }
                                            } else {
                                                // Failed to fetch selected units data
                                                Toast.makeText(this, "Failed to fetch selected units data.", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            } else {
                                // Handle the case where current_semester is null
                                Toast.makeText(this, "Current semester is null.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // Handle the case where student details document doesn't exist
                            Toast.makeText(this, "Student details not found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // Failed to fetch student details
                        Toast.makeText(this, "Failed to fetch student details", Toast.LENGTH_SHORT).show();
                    }
                });
    }



    private void populateUnitTable(DocumentSnapshot unitsDocument) {
        // Add header row
        TableRow headerRow = new TableRow(this);
        addCellToRow(headerRow, "Unit Name");
        addCellToRow(headerRow, "Unit Code");
        studentUnitsTable.addView(headerRow);

        // Add data rows
        for (String unitName : unitsDocument.getData().keySet()) {
            String unitCode = unitsDocument.getString(unitName);

            TableRow dataRow = new TableRow(this);
            addCellToRow(dataRow, unitName, true); // True indicates it's a unit name
            addCellToRow(dataRow, unitCode, false); // False indicates it's a unit code
            studentUnitsTable.addView(dataRow);
        }
    }

    private void addCellToRow(TableRow row, String text, boolean isUnitName) {
        TextView cell = new TextView(this);
        cell.setText(text);
        cell.setPadding(16, 16, 16, 16);
        cell.setGravity(Gravity.CENTER_VERTICAL);

        // Set text color to white
        cell.setTextColor(getResources().getColor(R.color.white));

        // Adjust formatting for unit names
        if (isUnitName) {
            // Calculate the maximum lines based on the available width
            int maxWidth = getResources().getDisplayMetrics().widthPixels / 2; // Assuming the screen is divided into two columns
            int maxLines = (int) Math.ceil(cell.getPaint().measureText(text) / maxWidth);
            cell.setSingleLine(false);
            cell.setMaxLines(maxLines);
        }

        row.addView(cell);
    }



    private void fetchAndDisplayStudentDetails() {
        // Fetch and display student details from the Firestore collection
        db.collection("student_details")
                .document(mAuth.getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot studentDocument = task.getResult();
                        if (studentDocument.exists()) {
                            populateStudentDetailsTable(studentDocument);
                        } else {
                            Toast.makeText(this, "Student details not found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Failed to fetch student details", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void populateStudentDetailsTable(DocumentSnapshot studentDocument) {
        // Extract student details and populate the studentDetailsTable
        String email = studentDocument.getString("email");
        String name = studentDocument.getString("fullName");
        String gender = studentDocument.getString("gender");
        String regNumber = studentDocument.getString("regNum");
        String sem = studentDocument.getString("current_semester");

        // Dynamically add rows to the table
        TableRow row1 = new TableRow(this);
        addRowToTable(row1, "Email", email);

        TableRow row2 = new TableRow(this);
        addRowToTable(row2, "Name", name);

        TableRow row3 = new TableRow(this);
        addRowToTable(row3, "Gender", gender);

        TableRow row4 = new TableRow(this);
        addRowToTable(row4, "Reg Number", regNumber);

        TableRow row5 = new TableRow(this);
        addRowToTable(row5, "Semester", sem);
    }

    private void addRowToTable(TableRow row, String key, String value) {
        // Dynamically add cells to a row
        TextView keyView = new TextView(this);
        keyView.setText(key);
        keyView.setTextColor(getResources().getColor(R.color.white));
        keyView.setPadding(10, 0, 20, 0);

        TextView valueView = new TextView(this);
        valueView.setText(value);
        valueView.setTextColor(getResources().getColor(R.color.white));
        valueView.setPadding(20, 0, 10, 0);

        row.addView(keyView);
        row.addView(valueView);

        studentDetailsTable.addView(row);
    }

    private void fetchAndDisplayScoreData() {
        // Fetch the score data from the Firestore collection
        db.collection("scores")
                .document(mAuth.getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot scoreDocument = task.getResult();
                            if (scoreDocument.exists()) {
                                // Score data found, populate tables dynamically
                                populateTable(findViewById(R.id.tableAssignment), "Assignment", scoreDocument, "assignments");
                                populateTable(findViewById(R.id.tableCATs), "CATs", scoreDocument, "cats");
                                populateTable(findViewById(R.id.tableExamScores), "Exam Scores", scoreDocument, "exams");
                                populateTable(findViewById(R.id.tableFinalScore), "Final Score", scoreDocument, "finalScore");
                            } else {
                                // No score data found
                                Toast.makeText(StudentPortalActivity.this, "Score data not found.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // Failed to fetch score data
                            Toast.makeText(StudentPortalActivity.this, "Failed to fetch score data.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void populateTable(TableLayout tableLayout, String header, DocumentSnapshot scoreDocument, String fieldName) {
        // Add header row
        TableRow headerRow = new TableRow(this);
        addCellToRow(headerRow, header);
        for (String course : getResources().getStringArray(R.array.course_names)) {
            addCellToRow(headerRow, course);
        }
        tableLayout.addView(headerRow);

        // Add data rows
        TableRow dataRow = new TableRow(this);
        addCellToRow(dataRow, "Score");

        // Fetch scores for each course
        String[] scores = new String[getResources().getStringArray(R.array.course_names).length];
        for (int i = 0; i < scores.length; i++) {
            if (scoreDocument.contains(fieldName)) {
                String fieldNameWithCourse = fieldName + "." + getResources().getStringArray(R.array.course_names)[i];
                if (scoreDocument.contains(fieldNameWithCourse)) {
                    String courseScore = scoreDocument.getString(fieldNameWithCourse);
                    scores[i] = courseScore != null ? courseScore : "-";
                } else {
                    // Log a message if the specific course is not found
                    Log.d("StudentPortalActivity", "Score for " + getResources().getStringArray(R.array.course_names)[i] + " not found.");
                    scores[i] = "-";
                }
            } else {
                // Handle the case where the field doesn't exist in the document
                scores[i] = "-";
            }
        }

        for (String score : scores) {
            addCellToRow(dataRow, score);
        }
        tableLayout.addView(dataRow);
    }

    private void addCellToRow(TableRow row, String text) {
        TextView cell = new TextView(this);
        cell.setText(text);
        cell.setPadding(16, 16, 16, 16);
        cell.setGravity(Gravity.CENTER);
        row.addView(cell);
    }

    private void navigateToLogin() {
        // Navigate to the login screen
        startActivity(new Intent(this, StudentLoginActivity.class));
        finish();
    }

    private void logoutAndNavigateToMain() {
        // Sign out the user
        mAuth.signOut();

        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
