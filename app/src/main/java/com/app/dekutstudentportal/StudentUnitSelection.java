package com.app.dekutstudentportal;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StudentUnitSelection extends AppCompatActivity {

    private List<CheckBox> unitCheckBoxes;
    private String currentSemester;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_unit_selection);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize Views
        unitCheckBoxes = new ArrayList<>();

        // Confirm Selection Btn
        findViewById(R.id.confirmSelectionBtn).setOnClickListener(view -> confirmUnitSelection());

        fetchCurrentSemester();

    }


    private void fetchCurrentSemester() {
        db.collection("student_details")
                .document(mAuth.getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot studentDocument = task.getResult();
                        if (studentDocument.exists() && studentDocument.contains("current_semester")) {
                            currentSemester = studentDocument.getString("current_semester");
                            // Display the units for the current sem
                            displayUnitsForSemester();

                            // Check if the user has already selected units
                            checkIfUnitsAlreadySelected();

                        } else {
                            Toast.makeText(this, "Failed to fetch current semester", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Failed to fetch current semester", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void displayUnitsForSemester() {
        // Display the checkboxes dynamically
        db.collection("courses")
                .document(currentSemester)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot courseDocument = task.getResult();
                        if (courseDocument.exists()) {
                            // Get all fields from the course doc
                            Map<String, Object> unitFields = courseDocument.getData();
                            for (Map.Entry<String, Object> entry : unitFields.entrySet()) {
                                CheckBox checkBox = new CheckBox(this);
                                String unitFieldName = entry.getKey();
                                checkBox.setText(unitFieldName);
                                checkBox.setChecked(courseDocument.contains(unitFieldName));
                                ((LinearLayout) findViewById(R.id.checkboxContainer)).addView(checkBox);
                                unitCheckBoxes.add(checkBox);
                            }
                        } else {
                            Toast.makeText(this, "Course document does not exist", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Failed to fetch course details", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void confirmUnitSelection() {
        int selectedUnitsCount = 0;
        ArrayList<String> selectedUnits = new ArrayList<>();

        // Count the number of selected units and store their names
        for (int i = 0; i < unitCheckBoxes.size(); i++) {
            CheckBox checkBox = unitCheckBoxes.get(i);
            if (checkBox.isChecked()) {
                selectedUnitsCount++;
                String courseName = checkBox.getText().toString();
                selectedUnits.add(courseName);
            }
        }

        // Check if the required num of units is selected
        if (selectedUnitsCount >= 5) {
            Toast.makeText(this, "Units selected successfully!", Toast.LENGTH_SHORT).show();

            saveSelectedUnitsToFirestore(selectedUnits);

            // Redirect to StudentPortalActivity
            Intent intent = new Intent(StudentUnitSelection.this, StudentPortalActivity.class);
            startActivity(intent);
            finish();
        } else {
            // Error if the required number of units is not selected
            Toast.makeText(this, "Please select at least 5 units.", Toast.LENGTH_SHORT).show();
        }
    }


    private void saveSelectedUnitsToFirestore(ArrayList<String> selectedUnits) {
        // Create a map to store the selected units
        Map<String, Object> unitsMap = new HashMap<>();

        // Fetch unit codes from the DB and store them with corresponding field names
        db.collection("courses")
                .document(currentSemester)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot courseDocument = task.getResult();
                        if (courseDocument.exists()) {
                            // Get all fields from the course doc
                            Map<String, Object> unitFields = courseDocument.getData();

                            // Iterate through the selected units and fetch their corresponding codes
                            for (String selectedUnit : selectedUnits) {
                                // Check if the selected unit is present in the course doc
                                if (unitFields.containsKey(selectedUnit)) {
                                    // Add the unit code to the unitsMap with the selected unit as the key
                                    unitsMap.put(selectedUnit, unitFields.get(selectedUnit));
                                } else {
                                    // Case: selected unit is not found in the course doc
                                    // Add a placeholder value "NotAvailable" to the map
                                    unitsMap.put(selectedUnit, "NotAvailable");
                                }
                            }

                            // Save the selected units to Firestore DB
                            db.collection("student_details")
                                    .document(mAuth.getUid())
                                    .collection(currentSemester + " Units")
                                    .document("selected_units")
                                    .set(unitsMap)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(this, "Units saved successfully.", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(this, "Failed to save selected units.", Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            Toast.makeText(this, "Course document does not exist", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Failed to fetch course details", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkIfUnitsAlreadySelected() {
        db.collection("student_details")
                .document(mAuth.getUid())
                .collection(currentSemester + " Units")
                .document("selected_units")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot selectedUnitsDocument = task.getResult();
                        if (selectedUnitsDocument.exists()) {
                            // User has already selected units
                            Toast.makeText(this, "You already selected units", Toast.LENGTH_SHORT).show();

                            // Redirect to the StudentPortalActivity
                            Intent intent = new Intent(StudentUnitSelection.this, StudentPortalActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    } else {
                        Toast.makeText(this, "Failed to check selected units", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
