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
import java.util.List;

public class PassAllCoursesYearActivity extends AppCompatActivity {

    private static final String TAG = "PassAllCoursesYearActivity";
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pass_all_courses_year);

        firestore = FirebaseFirestore.getInstance();

        // Fetch and display students who passed all courses in a year
        fetchAndDisplayPassAllCoursesYear();
    }

    private void fetchAndDisplayPassAllCoursesYear() {
        firestore.collection("scores")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<String> passAllCoursesYearStudents = new ArrayList<>();
                            for (DocumentSnapshot document : task.getResult()) {
                                final boolean[] passedAllCoursesYear = {true}; // Wrapper array

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
                                                                                    // Check if the student passed all courses in this semester
                                                                                    if (!isPassed(unit)) {
                                                                                        passedAllCoursesYear[0] = false;
                                                                                        break; // No need to check other units for this student
                                                                                    }
                                                                                }
                                                                            } else {
                                                                                Log.e(TAG, "Error getting documents: ", task.getException());
                                                                            }
                                                                        }
                                                                    });
                                                        }

                                                        if (passedAllCoursesYear[0]) {
                                                            passAllCoursesYearStudents.add(document.getId());
                                                        }

                                                        // Display the list of students who passed all courses in the year
                                                        displayPassAllCoursesYear(passAllCoursesYearStudents);
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

    private boolean isPassed(DocumentSnapshot unit) {
        // You need to replace "overallScore" with the actual field name for overallScore in your database
        Long overallScore = unit.getLong("overallScore");
        return overallScore != null && overallScore >= 40;
    }

    private void displayPassAllCoursesYear(List<String> passAllCoursesYearStudents) {
        ListView passAllCoursesYearListView = findViewById(R.id.passAllCoursesYearListView);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, passAllCoursesYearStudents);
        passAllCoursesYearListView.setAdapter(adapter);
    }
}
