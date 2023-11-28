package com.app.dekutstudentportal;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class AdminFunctionalitiesActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_functionalities);

        mAuth = FirebaseAuth.getInstance();

        // Generate Reports Btn
        findViewById(R.id.generateReportsBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navigateToGenerateReports();
            }
        });

        // Edit Marks Btn
        findViewById(R.id.editMarksBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navigateToEditMarks();
            }
        });

        // Print Summary Btn
        findViewById(R.id.printSummaryBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navigateToPrintSummary();
            }
        });

        // Admin Logout Btn
        findViewById(R.id.adminFunctionalitiesLogoutBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logoutAdmin();
            }
        });
    }

    private void navigateToGenerateReports() {
        Intent intent = new Intent(AdminFunctionalitiesActivity.this, GenerateReportsActivity.class);
        startActivity(intent);
    }

    private void navigateToEditMarks() {
        Intent intent = new Intent(AdminFunctionalitiesActivity.this, EditMarksActivity.class);
        startActivity(intent);
    }

    private void navigateToPrintSummary() {
        Intent intent = new Intent(AdminFunctionalitiesActivity.this, PrintSummaryActivity.class);
        startActivity(intent);
    }

    private void logoutAdmin() {
        mAuth.signOut();

        // Navigate to the MainActivity
        Intent intent = new Intent(AdminFunctionalitiesActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
