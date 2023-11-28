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

public class PassAllCoursesSemesterActivity extends AppCompatActivity {

    private static final String TAG = "PassAllCoursesSemesterActivity";
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pass_all_courses_semester);

        firestore = FirebaseFirestore.getInstance();

        // Fetch and display students who passed all courses in a semester
        fetchAndDisplayPassAllCourses();
    }

    private void fetchAndDisplayPassAllCourses() {
        firestore.collection("scores")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<String> passAllCoursesStudents = new ArrayList<>();
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
                                                        boolean passedAllCourses = true;
                                                        // Iterate through unit subcollections
                                                        for (DocumentSnapshot unit : task.getResult()) {
                                                            // Check if the student passed all courses in this semester
                                                            if (!isPassed(unit)) {
                                                                passedAllCourses = false;
                                                                break; // No need to check other units for this student
                                                            }
                                                        }
                                                        if (passedAllCourses) {
                                                            passAllCoursesStudents.add(document.getId());
                                                        }

                                                        // Display the list of students who passed all courses
                                                        displayPassAllCourses(passAllCoursesStudents);
                                                    } else {
                                                        Log.e(TAG, "Error getting documents: ", task.getException());
                                                    }
                                                }
                                            });
                                }
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

    private void displayPassAllCourses(List<String> passAllCoursesStudents) {
        ListView passAllCoursesListView = findViewById(R.id.passAllCoursesListView);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, passAllCoursesStudents);
        passAllCoursesListView.setAdapter(adapter);
    }
}
