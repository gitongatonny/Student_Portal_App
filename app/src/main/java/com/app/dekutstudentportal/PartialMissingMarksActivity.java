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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PartialMissingMarksActivity extends AppCompatActivity {

    private static final String TAG = "PartialMissingMarksActivity";
    private FirebaseFirestore firestore;
    private String selectedDocumentId; // Replace with the actual logic to get the selected document ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_partial_missing_marks);

        firestore = FirebaseFirestore.getInstance();

        // Replace with the actual logic to get the selected document ID
        selectedDocumentId = "ReplaceWithSelectedDocumentId";

        // Fetch and display partial missing marks
        fetchAndDisplayPartialMissingMarks();
    }

    private void fetchAndDisplayPartialMissingMarks() {
        firestore.collection("scores")
                .document(selectedDocumentId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot subCollectionSnapshot = task.getResult();
                            if (subCollectionSnapshot.exists()) {
                                Map<String, Boolean> partialMissingMarksMap = new HashMap<>();
                                // Iterate through the sub-collection document ID
                                for (String studentId : subCollectionSnapshot.getData().keySet()) {
                                    firestore.collection("scores")
                                            .document(selectedDocumentId)
                                            .collection(studentId)
                                            .document(selectedDocumentId)
                                            .get()
                                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                    if (task.isSuccessful()) {
                                                        DocumentSnapshot studentSnapshot = task.getResult();
                                                        if (studentSnapshot.exists()) {
                                                            // Check if the student has any blank field in assignments, CATs, and examScore
                                                            if (hasPartialMissingMarks(studentSnapshot)) {
                                                                partialMissingMarksMap.put(studentId, true);
                                                            }
                                                        }
                                                    } else {
                                                        Log.e(TAG, "Error getting document: ", task.getException());
                                                    }
                                                }
                                            });
                                }

                                // Display the list of students with partial missing marks
                                displayPartialMissingMarks(partialMissingMarksMap);
                            }
                        } else {
                            Log.e(TAG, "Error getting document: ", task.getException());
                        }
                    }
                });
    }

    private boolean hasPartialMissingMarks(DocumentSnapshot studentSnapshot) {
        // You need to replace these with the actual field names in your database
        return studentSnapshot.getString("assignment1").isEmpty()
                || studentSnapshot.getString("assignment2").isEmpty()
                || studentSnapshot.getString("cat1").isEmpty()
                || studentSnapshot.getString("cat2").isEmpty()
                || !studentSnapshot.contains("examScore");
    }

    private void displayPartialMissingMarks(Map<String, Boolean> partialMissingMarksMap) {
        ListView partialMissingMarksListView = findViewById(R.id.partialMissingMarksListView);
        List<String> displayList = new ArrayList<>();

        for (Map.Entry<String, Boolean> entry : partialMissingMarksMap.entrySet()) {
            String studentId = entry.getKey();
            boolean hasPartialMissingMarks = entry.getValue();

            if (hasPartialMissingMarks) {
                displayList.add(studentId);
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, displayList);
        partialMissingMarksListView.setAdapter(adapter);
    }
}
