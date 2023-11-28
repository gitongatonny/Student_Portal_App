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
import com.google.firebase.firestore.QuerySnapshot;

public class retakeActivity extends AppCompatActivity {

    private static final String TAG = "RetakeActivity";
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_retake);

        firestore = FirebaseFirestore.getInstance();

        // Fetch and display retake students
        fetchAndDisplayRetakeStudents();
    }

    private void fetchAndDisplayRetakeStudents() {
        firestore.collection("scores")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Map<String, Boolean> retakeStudentsMap = new HashMap<>();
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
                                                            String studentId = unit.getId();

                                                            firestore.collection("scores")
                                                                    .document(document.getId())
                                                                    .collection(semesterUnit)
                                                                    .document(studentId)
                                                                    .get()
                                                                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                            if (task.isSuccessful()) {
                                                                                DocumentSnapshot studentSnapshot = task.getResult();
                                                                                if (studentSnapshot.exists()) {
                                                                                    // Check if the student is attempting a course for the second time
                                                                                    if (isRetakeStudent(studentSnapshot)) {
                                                                                        retakeStudentsMap.put(studentId, true);
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

                            // Display the list of retake students
                            displayRetakeStudents(retakeStudentsMap);
                        } else {
                            Log.e(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    private boolean isRetakeStudent(DocumentSnapshot studentSnapshot) {
        // You need to replace this with the actual logic to determine if the student is attempting a course for the second time
        // For example, you may check if the student has failed in the first attempt
        return studentSnapshot.getBoolean("failedFirstAttempt") != null
                && studentSnapshot.getBoolean("failedFirstAttempt");
    }

    private void displayRetakeStudents(Map<String, Boolean> retakeStudentsMap) {
        ListView retakeStudentsListView = findViewById(R.id.retakeStudentsListView);
        List<String> displayList = new ArrayList<>();

        for (Map.Entry<String, Boolean> entry : retakeStudentsMap.entrySet()) {
            String studentId = entry.getKey();
            boolean isRetakeStudent = entry.getValue();

            if (isRetakeStudent) {
                displayList.add(studentId);
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, displayList);
        retakeStudentsListView.setAdapter(adapter);
    }
}
