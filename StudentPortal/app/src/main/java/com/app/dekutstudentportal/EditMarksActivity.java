// EditMarksActivity.java

package com.app.dekutstudentportal;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class EditMarksActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private Spinner spinnerStudentNames;
    private EditText editTextAssignment1;
    private EditText editTextAssignment2;
    private EditText editTextCat1;
    private EditText editTextCat2;
    private EditText editTextExamScore;
    private Button buttonSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_marks);

        db = FirebaseFirestore.getInstance();

        // Initialize views
        spinnerStudentNames = findViewById(R.id.spinnerStudentNames);
        editTextAssignment1 = findViewById(R.id.editTextAssignment1);
        editTextAssignment2 = findViewById(R.id.editTextAssignment2);
        editTextCat1 = findViewById(R.id.editTextCat1);
        editTextCat2 = findViewById(R.id.editTextCat2);
        editTextExamScore = findViewById(R.id.editTextExamScore);
        buttonSave = findViewById(R.id.buttonSave);

        // Populate the spinner with student names from Firestore
        populateStudentNamesSpinner();

        // Spinner item selection listener
        spinnerStudentNames.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // Load marks for the selected student
                loadMarksForSelectedStudent(spinnerStudentNames.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });

        // Save button click listener
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveMarks();
            }
        });
    }

    private void populateStudentNamesSpinner() {
        // Retrieve student names from Firestore "marks" collection
        db.collection("marks")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> studentNamesList = new ArrayList<>();

                        for (DocumentSnapshot document : task.getResult().getDocuments()) {
                            studentNamesList.add(document.getId());
                        }

                        // Create an ArrayAdapter using the string array and a default spinner layout
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, studentNamesList);

                        // Specify the layout to use when the list of choices appears
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                        // Apply the adapter to the spinner
                        spinnerStudentNames.setAdapter(adapter);
                    } else {
                        showToast("Failed to retrieve student names.");
                    }
                });
    }

    private void loadMarksForSelectedStudent(String studentName) {
        // Retrieve marks for the selected student from Firestore
        db.collection("marks")
                .document(studentName)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Populate the EditText fields with the retrieved marks
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            editTextAssignment1.setText(String.valueOf(document.getLong("assignment1")));
                            editTextAssignment2.setText(String.valueOf(document.getLong("assignment2")));
                            editTextCat1.setText(String.valueOf(document.getLong("cat1")));
                            editTextCat2.setText(String.valueOf(document.getLong("cat2")));
                            editTextExamScore.setText(String.valueOf(document.getLong("examScore")));
                        }
                    } else {
                        showToast("Failed to retrieve marks for the selected student.");
                    }
                });
    }

    private void saveMarks() {
        // Retrieve data from the views
        String studentName = spinnerStudentNames.getSelectedItem().toString();
        long assignment1 = Long.parseLong(editTextAssignment1.getText().toString());
        long assignment2 = Long.parseLong(editTextAssignment2.getText().toString());
        long cat1 = Long.parseLong(editTextCat1.getText().toString());
        long cat2 = Long.parseLong(editTextCat2.getText().toString());
        long examScore = Long.parseLong(editTextExamScore.getText().toString());

        // Update marks for the selected student in Firestore
        db.collection("marks")
                .document(studentName)
                .update("assignment1", assignment1, "assignment2", assignment2, "cat1", cat1, "cat2", cat2, "examScore", examScore)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        showToast("Marks saved successfully.");
                    } else {
                        showToast("Failed to save marks.");
                    }
                });
    }

    private void showToast(String message) {
        // Replace with actual logic to show a toast message
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
