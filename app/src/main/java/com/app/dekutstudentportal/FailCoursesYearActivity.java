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

public class FailCoursesYearActivity extends AppCompatActivity {

    private static final String TAG = "FailCoursesYearActivity";
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fail_courses_year);

        firestore = FirebaseFirestore.getInstance();

        // Fetch and display students who failed courses in a year
        fetchAndDisplayFailCoursesYear();
    }

    private void fetchAndDisplayFailCoursesYear() {
        firestore.collection("scores")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<String> failCoursesYearStudents = new ArrayList<>();
                            Map<String, Boolean> failCoursesYearMap = new HashMap<>();
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
                                                                                    // Check if the student failed any courses in this semester
                                                                                    if (isFailed(unit)) {
                                                                                        failCoursesYearMap.put(document.getId(), true);
                                                                                    }
                                                                                }

                                                                                // Display the list of students who failed courses in the year
                                                                                displayFailCoursesYear(failCoursesYearMap);
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

    private boolean isFailed(DocumentSnapshot unit) {
        // You need to replace "overallScore" with the actual field name for overallScore in your database
        Long overallScore = unit.getLong("overallScore");
        return overallScore != null && overallScore < 40;
    }

    private void displayFailCoursesYear(Map<String, Boolean> failCoursesYearMap) {
        ListView failCoursesYearListView = findViewById(R.id.failCoursesYearListView);
        List<String> displayList = new ArrayList<>();

        for (Map.Entry<String, Boolean> entry : failCoursesYearMap.entrySet()) {
            String studentId = entry.getKey();
            boolean hasFailedCourses = entry.getValue();

            if (hasFailedCourses) {
                displayList.add(studentId);
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, displayList);
        failCoursesYearListView.setAdapter(adapter);
    }
}
