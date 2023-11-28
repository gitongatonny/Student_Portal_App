package com.app.dekutstudentportal;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LecPortalActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String teachingCourse;

    private TableLayout lecturerDetailsTable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lec_portal);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        lecturerDetailsTable = findViewById(R.id.lecturerDetailsTable);

        // View Student List Btn
        Button viewStudentListBtn = findViewById(R.id.viewStudentListBtn);
        viewStudentListBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Pass the teaching course variable to the student list activity
                Intent intent = new Intent(LecPortalActivity.this, SelectStudentActivity.class);
                intent.putExtra("teachingCourse", teachingCourse);
                startActivity(intent);
            }
        });



        // Lec Logout Btn
        Button lecLogoutBtn = findViewById(R.id.lecLogoutPortalBtn);
        lecLogoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logoutLecturer();
            }
        });

        fetchAndDisplayLecDetails();
    }

    private void fetchAndDisplayLecDetails() {
        db.collection("lecturer_details")
                .document(mAuth.getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot lecDocument = task.getResult();
                        if (lecDocument.exists()) {
                            // Retrieve the teaching course value
                            teachingCourse = lecDocument.getString("teaching_course");
                            populateLecDetailsTable(lecDocument);
                        } else {
                            Toast.makeText(this, "Lec details not found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Failed to get lec details", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void populateLecDetailsTable(DocumentSnapshot lecDocument) {
        String email = lecDocument.getString("email");
        String name = lecDocument.getString("name");
        String gender = lecDocument.getString("gender");
        String phone = lecDocument.getString("phone");
        String teaching_course = lecDocument.getString("teaching_course");

        TableRow row1 = new TableRow(this);
        addRowToTable(row1, "Email", email);

        TableRow row2 = new TableRow(this);
        addRowToTable(row2, "Name", name);

        TableRow row3 = new TableRow(this);
        addRowToTable(row3, "Gender", gender);

        TableRow row4 = new TableRow(this);
        addRowToTable(row4, "Phone Number", phone);

        TableRow row5 = new TableRow(this);
        addRowToTable(row5, "Teaching Course", teaching_course);
    }

    private void addRowToTable(TableRow row, String key, String value) {
        TextView keyView = new TextView(this);
        keyView.setText(key);
        keyView.setTextColor(getResources().getColor(R.color.white));
        keyView.setPadding(10, 0, 20, 0);

        TextView valueView = new TextView(this);
        valueView.setText(value);
        valueView.setTextColor(getResources().getColor(R.color.white));
        valueView.setPadding(20, 0, 10, 0);

        row.addView(keyView);
        row.addView(valueView);

        lecturerDetailsTable.addView(row);
    }

    private void logoutLecturer() {
        mAuth.signOut();

        Intent intent = new Intent(LecPortalActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
