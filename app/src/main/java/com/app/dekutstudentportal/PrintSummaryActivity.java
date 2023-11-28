package com.app.dekutstudentportal;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PrintSummaryActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private ListView listViewSummary;
    private ArrayAdapter<String> adapter;

    // List of collection paths to summarize
    private List<String> collectionPaths;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_print_summary);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        listViewSummary = findViewById(R.id.listViewSummary);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        listViewSummary.setAdapter(adapter);

        // Initialize the list of collection paths
        collectionPaths = new ArrayList<>();
        collectionPaths.add("student_details");
        collectionPaths.add("scores");

        Button printSummaryBtn = findViewById(R.id.btnPrintSummary);
        printSummaryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                printSummary();
            }
        });
    }

    private void printSummary() {
        // Clear previous summaries
        adapter.clear();

        // Get the summary for each collection
        for (String collectionPath : collectionPaths) {
            getCollectionSummary(collectionPath);
        }
    }

    private void getCollectionSummary(String collectionPath) {
        db.collection(collectionPath)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        int count = task.getResult().size();
                        String summary = "Total documents in " + collectionPath + ": " + count;
                        adapter.add(summary);

                        // If collectionPath is "scores", fetch and print marks summary
                        if ("scores".equals(collectionPath)) {
                            fetchAndPrintMarksSummary();
                        }
                    } else {
                        handleFirestoreError(collectionPath, task.getException());
                    }
                });
    }

    private void fetchAndPrintMarksSummary() {
        db.collection("scores")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot unitDocument : task.getResult()) {
                            try {
                                String unitID = unitDocument.getId();
                                StringBuilder marksSummary = new StringBuilder("Marks summary for Unit ID " + unitID + ":\n");

                                // Fetch all subcollections for each unit
                                db.collection("scores").document(unitID)
                                        .get()
                                        .addOnCompleteListener(subCollectionTask -> {
                                            if (subCollectionTask.isSuccessful()) {
                                                // Get a reference to the subcollection (assuming it's dynamic based on unitID)
                                                String subcollectionPath = unitID; // Update this to your actual subcollection path
                                                db.collection("scores").document(unitID)
                                                        .collection(subcollectionPath)
                                                        .get()
                                                        .addOnCompleteListener(scoresTask -> {
                                                            if (scoresTask.isSuccessful()) {
                                                                for (QueryDocumentSnapshot scoreDocument : scoresTask.getResult()) {
                                                                    String studentID = scoreDocument.getId();

                                                                    // Fetch student details for each studentID
                                                                    db.collection("student_details").document(studentID)
                                                                            .get()
                                                                            .addOnCompleteListener(studentDetailsTask -> {
                                                                                if (studentDetailsTask.isSuccessful()) {
                                                                                    DocumentSnapshot studentDetails = studentDetailsTask.getResult();
                                                                                    if (studentDetails.exists()) {
                                                                                        String studentName = studentDetails.getString("fullName");
                                                                                        marksSummary.append("\t\t").append("Student Name: ").append(studentName).append("\n");

                                                                                        // Fetch scores for assignments, cats, and exam
                                                                                        Map<String, Object> scoresData = scoreDocument.getData();
                                                                                        if (scoresData != null) {
                                                                                            for (Map.Entry<String, Object> entry : scoresData.entrySet()) {
                                                                                                marksSummary.append("\t\t\t").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
                                                                                            }
                                                                                        }
                                                                                    }
                                                                                } else {
                                                                                    logFirestoreError("FetchStudentDetails", studentDetailsTask.getException());
                                                                                }
                                                                            });
                                                                }
                                                                adapter.add(marksSummary.toString());
                                                            } else {
                                                                logFirestoreError("FetchScores", scoresTask.getException());
                                                            }
                                                        });
                                            } else {
                                                logFirestoreError("FetchSubcollection", subCollectionTask.getException());
                                            }
                                        });
                            } catch (Exception e) {
                                logFirestoreError("PrintMarksSummary", e);
                            }
                        }
                    } else {
                        logFirestoreError("PrintMarksSummary", task.getException());
                    }
                });
    }

    private void logFirestoreError(String operation, Exception exception) {
        String errorMessage = "Firestore operation failed: " + operation + "\nError: " + exception.getMessage();
        Log.e("FirestoreError", errorMessage);
    }


    private void handleFirestoreError(String operation, Exception exception) {
        String errorMessage = "Firestore operation failed: " + operation + "\nError: " + exception.getMessage();
        adapter.add(errorMessage);
        Toast.makeText(PrintSummaryActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
    }
}
