package com.app.dekutstudentportal;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpecialCasesActivity extends AppCompatActivity {

    private static final String TAG = "SpecialCasesActivity";
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_special_cases);

        firestore = FirebaseFirestore.getInstance();

        // Fetch and display special cases
        fetchAndDisplaySpecialCases();
    }

    private void fetchAndDisplaySpecialCases() {
        firestore.collection("scores")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<String> specialCasesStudents = new ArrayList<>();
                            Map<String, Boolean> specialCasesMap = new HashMap<>();
                            for (DocumentSnapshot document : task.getResult()) {
                                // Access the subcollections for all semesters within a year
                                firestore.collection("scores")
                                        .document(document.getId())
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    DocumentSnapshot yearSnapshot = task.getResult();
                                                    if (yearSnapshot.exists()) {
                                                        // Iterate through semester units
                                                        for (String semesterUnit : yearSnapshot.getData().keySet()) {
                                                            // Access the subcollection for the semester unit
                                                            firestore.collection("scores")
                                                                    .document(document.getId())
                                                                    .collection(semesterUnit)
                                                                    .get()
                                                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                                            if (task.isSuccessful()) {
                                                                                // Iterate through unit subcollections
                                                                                for (DocumentSnapshot unit : task.getResult()) {
                                                                                    // Check if the student is a special case
                                                                                    if (isSpecialCase(unit)) {
                                                                                        specialCasesMap.put(document.getId(), true);
                                                                                    }
                                                                                }

                                                                                // Display the list of special cases
                                                                                displaySpecialCases(specialCasesMap);
                                                                            } else {
                                                                                Log.e(TAG, "Error getting documents: ", task.getException());
                                                                            }
                                                                        }
                                                                    });
                                                        }
                                                    }
                                                } else {
                                                    Log.e(TAG, "Error getting document: ", task.getException());
                                                }
                                            }
                                        });
                            }
                        } else {
                            Log.e(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    private boolean isSpecialCase(DocumentSnapshot unit) {
        // You need to replace "examScore" with the actual field name for examScore in your database
        return unit.contains("assignment1") && unit.contains("assignment2") &&
                unit.contains("cat1") && unit.contains("cat2") && !unit.contains("examScore");
    }

    private void displaySpecialCases(Map<String, Boolean> specialCasesMap) {
        ListView specialCasesListView = findViewById(R.id.specialCasesListView);
        List<String> displayList = new ArrayList<>();

        for (Map.Entry<String, Boolean> entry : specialCasesMap.entrySet()) {
            String studentId = entry.getKey();
            boolean isSpecialCase = entry.getValue();

            if (isSpecialCase) {
                displayList.add(studentId);
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, displayList);
        specialCasesListView.setAdapter(adapter);
    }
}
