// ViewMarksActivity.java
package com.app.dekutstudentportal;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class ViewMarksActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String teachingCourse;

    private ListView marksListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_marks);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Get the teaching course from the intent
        teachingCourse = getIntent().getStringExtra("teachingCourse");

        // Initialize Views
        marksListView = findViewById(R.id.marksListView);

        // Set the title text
        TextView viewMarksTitle = findViewById(R.id.viewMarksTitle);
        viewMarksTitle.setText("View Marks for " + teachingCourse);

        // Back to Portal Btn
        com.google.android.material.button.MaterialButton backToPortalBtn = findViewById(R.id.backToPortalBtn);
        backToPortalBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish(); // Close the ViewMarksActivity and go back to the LecPortalActivity
            }
        });

        fetchAndDisplayMarks();
    }

    private void fetchAndDisplayMarks() {
        db.collection("marks")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // List to store marks information
                        ArrayList<String> marksList = new ArrayList<>();

                        for (DocumentSnapshot document : task.getResult()) {
                            String studentName = document.getId();
                            String mark = document.getString("marks");
                            marksList.add(studentName + ": " + mark);
                        }

                        // Display the marks information in the ListView
                        displayMarksList(marksList);
                    } else {
                        // Handle the error
                        handleFetchMarksError(task.getException());
                    }
                });
    }


    private void displayMarksList(ArrayList<String> marksList) {
        // Create an ArrayAdapter to display the marks information in the ListView
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, marksList);

        // Set the adapter for the ListView
        marksListView.setAdapter(adapter);
    }

    private void handleFetchMarksError(Exception exception) {
        // Display a toast message to the user
        Toast.makeText(this, "Failed to fetch marks: " + exception.getMessage(), Toast.LENGTH_SHORT).show();

        // Log the error for debugging purposes
        exception.printStackTrace();
    }
}
