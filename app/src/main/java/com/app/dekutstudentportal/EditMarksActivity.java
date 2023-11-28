package com.app.dekutstudentportal;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class EditMarksActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private Spinner spinnerSemester;
    private Spinner spinnerUnit;
    private Spinner spinnerStudent;
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
        spinnerSemester = findViewById(R.id.spinnerSemester);
        spinnerUnit = findViewById(R.id.spinnerUnit);
        spinnerStudent = findViewById(R.id.spinnerStudent);
        editTextAssignment1 = findViewById(R.id.editTextAssignment1);
        editTextAssignment2 = findViewById(R.id.editTextAssignment2);
        editTextCat1 = findViewById(R.id.editTextCat1);
        editTextCat2 = findViewById(R.id.editTextCat2);
        editTextExamScore = findViewById(R.id.editTextExamScore);
        buttonSave = findViewById(R.id.buttonSave);

        populateSemesterSpinner();

        // Spinner for Semesters
        spinnerSemester.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String selectedSemester = spinnerSemester.getSelectedItem() != null ? spinnerSemester.getSelectedItem().toString() : "";
                if (!selectedSemester.isEmpty()) {
                    populateUnitSpinner(selectedSemester);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });

        // Spinner for Units
        spinnerUnit.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String selectedSemester = spinnerSemester.getSelectedItem() != null ? spinnerSemester.getSelectedItem().toString() : "";
                String selectedUnit = spinnerUnit.getSelectedItem() != null ? spinnerUnit.getSelectedItem().toString() : "";
                if (!selectedSemester.isEmpty() && !selectedUnit.isEmpty()) {
                    populateStudentSpinner(selectedSemester, selectedUnit);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });

        // Save Marks btn
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String selectedSemester = spinnerSemester.getSelectedItem() != null ? spinnerSemester.getSelectedItem().toString() : "";
                String selectedUnit = spinnerUnit.getSelectedItem() != null ? spinnerUnit.getSelectedItem().toString() : "";
                if (!selectedSemester.isEmpty() && !selectedUnit.isEmpty()) {
                    saveMarks(selectedSemester, selectedUnit);
                } else {
                    showToast("Selected semester or unit is empty.");
                }
            }
        });
    }

    private void populateSemesterSpinner() {
        db.collection("scores")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> semesterList = new ArrayList<>();

                        for (DocumentSnapshot document : task.getResult().getDocuments()) {
                            semesterList.add(document.getId());
                        }

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(EditMarksActivity.this, android.R.layout.simple_spinner_item, semesterList);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                        spinnerSemester.setAdapter(adapter);
                    } else {
                        showToast("Failed to retrieve semesters: " + task.getException());
                    }
                });
    }

    private void populateUnitSpinner(String selectedSemester) {
        db.collection("semester_units")
                .document(selectedSemester)
                .collection("units")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<String> unitList = new ArrayList<>();

                        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                            unitList.add(document.getId());
                        }

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(EditMarksActivity.this, android.R.layout.simple_spinner_item, unitList);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerUnit.setAdapter(adapter);

                        // Fetch and populate students based on the selected semester and unit
                        String selectedUnit = spinnerUnit.getSelectedItem() != null ? spinnerUnit.getSelectedItem().toString() : "";
                        if (!selectedUnit.isEmpty()) {
                            populateStudentSpinner(selectedSemester, selectedUnit);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        showToast("Can't get units for the selected sem: " + e.getMessage());
                    }
                });
    }

    private void populateStudentSpinner(String selectedSemester, String selectedUnit) {
        db.collection("scores")
                .document(selectedSemester)
                .collection(selectedUnit)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<String> studentList = new ArrayList<>();

                        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                            studentList.add(document.getId());
                        }

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(EditMarksActivity.this, android.R.layout.simple_spinner_item, studentList);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerStudent.setAdapter(adapter);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        showToast("Failed to retrieve students for the selected unit: " + e.getMessage());
                    }
                });
    }

    private void saveMarks(String selectedSemester, String selectedUnit) {
        String studentName = spinnerStudent.getSelectedItem() != null ? spinnerStudent.getSelectedItem().toString() : "";
        if (!studentName.isEmpty()) {
            long assignment1 = parseLong(editTextAssignment1.getText().toString());
            long assignment2 = parseLong(editTextAssignment2.getText().toString());
            long cat1 = parseLong(editTextCat1.getText().toString());
            long cat2 = parseLong(editTextCat2.getText().toString());
            long examScore = parseLong(editTextExamScore.getText().toString());

            db.collection("scores")
                    .document(selectedSemester)
                    .collection(selectedUnit)
                    .document(studentName)
                    .update("assignment1", assignment1, "assignment2", assignment2, "cat1", cat1, "cat2", cat2, "examScore", examScore)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            showToast("Marks updated successfully.");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(Exception e) {
                            showToast("Failed to update marks: " + e.getMessage());
                        }
                    });
        } else {
            showToast("Selected student is empty.");
        }
    }

    private long parseLong(String s) {
        try {
            return Long.parseLong(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}