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

public class FailCoursesSemesterActivity extends AppCompatActivity {

    private static final String TAG = "FailCoursesSemesterActivity";
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fail_courses_semester);

        firestore = FirebaseFirestore.getInstance();

        // Fetch and display students who failed courses in a semester
        fetchAndDisplayFailCoursesSemester();
    }

    private void fetchAndDisplayFailCoursesSemester() {
        firestore.collection("scores")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<String> failCoursesSemesterStudents = new ArrayList<>();
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
                                                                                        failCoursesSemesterStudents.add(document.getId());
                                                                                        break; // No need to check other units for this student
                                                                                    }
                                                                                }
                                                                            } else {
                                                                                Log.e(TAG, "Error getting documents: ", task.getException());
                                                                            }
                                                                        }
                                                                    });
                                                        }

                                                        // Display the list of students who failed courses in the semester
                                                        displayFailCoursesSemester(failCoursesSemesterStudents);
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

    private void displayFailCoursesSemester(List<String> failCoursesSemesterStudents) {
        Map<String, List<String>> failCoursesMap = new HashMap<>();

        for (String studentId : failCoursesSemesterStudents) {
            firestore.collection("scores")
                    .document(studentId)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot studentSnapshot = task.getResult();
                                if (studentSnapshot.exists()) {
                                    // Iterate through semester units
                                    for (String semesterUnit : studentSnapshot.getData().keySet()) {
                                        // Access the subcollection for the semester unit
                                        firestore.collection("scores")
                                                .document(studentId)
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
                                                                    String courseName = unit.getId();
                                                                    failCoursesMap.putIfAbsent(studentId, new ArrayList<>());
                                                                    failCoursesMap.get(studentId).add(courseName);
                                                                }
                                                            }
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

        // Display the list of students who failed courses in the semester along with the courses
        displayFailCoursesWithCourses(failCoursesMap);
    }

    private void displayFailCoursesWithCourses(Map<String, List<String>> failCoursesMap) {
        ListView failCoursesSemesterListView = findViewById(R.id.failCoursesSemesterListView);
        List<String> displayList = new ArrayList<>();

        for (Map.Entry<String, List<String>> entry : failCoursesMap.entrySet()) {
            String studentId = entry.getKey();
            List<String> failedCourses = entry.getValue();

            StringBuilder displayString = new StringBuilder(studentId + ": ");
            for (String course : failedCourses) {
                displayString.append(course).append(", ");
            }
            displayString.delete(displayString.length() - 2, displayString.length()); // Remove the last comma and space
            displayList.add(displayString.toString());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, displayList);
        failCoursesSemesterListView.setAdapter(adapter);
    }
}
