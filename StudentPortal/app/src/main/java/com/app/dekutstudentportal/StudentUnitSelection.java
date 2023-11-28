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

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize Views
        unitCheckBoxes = new ArrayList<>();

        // Set Click Listener for Confirm Selection Button
        findViewById(R.id.confirmSelectionBtn).setOnClickListener(view -> confirmUnitSelection());

        // Fetch the current semester from the user's details
        fetchCurrentSemester();

    }


    private void fetchCurrentSemester() {
        // Fetch the current semester from the user's details
        db.collection("student_details")
                .document(mAuth.getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot studentDocument = task.getResult();
                        if (studentDocument.exists() && studentDocument.contains("current_semester")) {
                            currentSemester = studentDocument.getString("current_semester");
                            // Display the units for the current semester
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
        // Display the checkboxes dynamically generated based on the units for the current semester
        db.collection("courses")
                .document(currentSemester)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot courseDocument = task.getResult();
                        if (courseDocument.exists()) {
                            // Get all fields from the course document
                            Map<String, Object> unitFields = courseDocument.getData();
                            for (Map.Entry<String, Object> entry : unitFields.entrySet()) {
                                CheckBox checkBox = new CheckBox(this);
                                String unitFieldName = entry.getKey();
                                checkBox.setText(unitFieldName);
                                checkBox.setChecked(courseDocument.contains(unitFieldName));
                                // Add the checkbox to your layout
                                // (replace R.id.checkboxContainer with your actual layout id)
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

                // Use the actual course name instead of assuming "Unit1", "Unit2", ..., "Unit10"
                String courseName = checkBox.getText().toString();
                selectedUnits.add(courseName);
            }
        }

        // Check if the required number of units is selected
        if (selectedUnitsCount >= 5) {
            // Display success message
            Toast.makeText(this, "Units selected successfully!", Toast.LENGTH_SHORT).show();

            // Save selected units to Firestore
            saveSelectedUnitsToFirestore(selectedUnits);

            // Redirect to StudentPortalActivity
            Intent intent = new Intent(StudentUnitSelection.this, StudentPortalActivity.class);
            startActivity(intent);
            finish();
        } else {
            // Display an error message if the required number of units is not selected
            Toast.makeText(this, "Please select at least 5 units.", Toast.LENGTH_SHORT).show();
        }
    }


    private void saveSelectedUnitsToFirestore(ArrayList<String> selectedUnits) {
        // Create a map to store the selected units
        Map<String, Object> unitsMap = new HashMap<>();

        // Fetch the actual unit codes from the database and store them with corresponding field names
        db.collection("courses")
                .document(currentSemester)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot courseDocument = task.getResult();
                        if (courseDocument.exists()) {
                            // Get all fields from the course document
                            Map<String, Object> unitFields = courseDocument.getData();

                            // Iterate through the selected units and fetch their corresponding codes
                            for (String selectedUnit : selectedUnits) {
                                // Check if the selected unit is present in the course document
                                if (unitFields.containsKey(selectedUnit)) {
                                    // Add the unit code to the unitsMap with the selected unit as the key
                                    unitsMap.put(selectedUnit, unitFields.get(selectedUnit));
                                } else {
                                    // Handle the case where the selected unit is not found in the course document
                                    // You may want to log an error or handle it in a way that suits your app
                                    // For now, we're adding a placeholder value "NotAvailable" to the map
                                    unitsMap.put(selectedUnit, "NotAvailable");
                                }
                            }

                            // Save the selected units to Firestore under the current semester Units field in the student_details document
                            db.collection("student_details")
                                    .document(mAuth.getUid())
                                    .collection(currentSemester + " Units")
                                    .document("selected_units")
                                    .set(unitsMap)
                                    .addOnSuccessListener(aVoid -> {
                                        // Successfully saved
                                        Toast.makeText(this, "Units saved successfully.", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        // Failed to save
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
        // Check if the user has already selected units
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
