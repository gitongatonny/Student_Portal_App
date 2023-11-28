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

public class fullMissingMarksActivity extends AppCompatActivity {

    private static final String TAG = "FullMissingMarksActivity";
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_missing_marks);

        firestore = FirebaseFirestore.getInstance();

        // Fetch and display full missing marks
        fetchAndDisplayFullMissingMarks();
    }

    private void fetchAndDisplayFullMissingMarks() {
        firestore.collection("scores")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Map<String, Boolean> fullMissingMarksMap = new HashMap<>();
                            for (DocumentSnapshot document : task.getResult()) {
                                // Iterate through semester units
                                for (String semesterUnit : document.getData().keySet()) {
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
                                                            String selectedDocumentId = unit.getId();

                                                            firestore.collection("scores")
                                                                    .document(document.getId())
                                                                    .collection(semesterUnit)
                                                                    .document(selectedDocumentId)
                                                                    .get()
                                                                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                            if (task.isSuccessful()) {
                                                                                DocumentSnapshot studentSnapshot = task.getResult();
                                                                                if (studentSnapshot.exists()) {
                                                                                    // Check if the student has all fields blank in assignments, CATs, and examScore
                                                                                    if (hasFullMissingMarks(studentSnapshot)) {
                                                                                        fullMissingMarksMap.put(selectedDocumentId, true);
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
                            }

                            // Display the list of students with full missing marks
                            displayFullMissingMarks(fullMissingMarksMap);
                        } else {
                            Log.e(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    private boolean hasFullMissingMarks(DocumentSnapshot studentSnapshot) {
        // You need to replace these with the actual field names in your database
        return studentSnapshot.getString("assignment1").isEmpty()
                && studentSnapshot.getString("assignment2").isEmpty()
                && studentSnapshot.getString("cat1").isEmpty()
                && studentSnapshot.getString("cat2").isEmpty()
                && !studentSnapshot.contains("examScore");
    }

    private void displayFullMissingMarks(Map<String, Boolean> fullMissingMarksMap) {
        ListView fullMissingMarksListView = findViewById(R.id.fullMissingMarksListView);
        List<String> displayList = new ArrayList<>();

        for (Map.Entry<String, Boolean> entry : fullMissingMarksMap.entrySet()) {
            String studentId = entry.getKey();
            boolean hasFullMissingMarks = entry.getValue();

            if (hasFullMissingMarks) {
                displayList.add(studentId);
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, displayList);
        fullMissingMarksListView.setAdapter(adapter);
    }
}
