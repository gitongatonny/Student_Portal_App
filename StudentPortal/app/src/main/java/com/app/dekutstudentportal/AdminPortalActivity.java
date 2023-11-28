package com.app.dekutstudentportal;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class AdminPortalActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_portal);

        mAuth = FirebaseAuth.getInstance();

        // Admin Logout Button
        Button adminLogoutBtn = findViewById(R.id.adminLogoutBtn);
        adminLogoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logoutAdmin();
            }
        });

        // Admin Functionalities Button
        Button adminFunctionalitiesBtn = findViewById(R.id.adminFunctionalitiesBtn);
        adminFunctionalitiesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navigateToAdminFunctionalities();
            }
        });
    }

    private void logoutAdmin() {
        // Sign out the admin
        mAuth.signOut();

        // Navigate to the MainActivity
        Intent intent = new Intent(AdminPortalActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish(); // Finish the current activity
    }

    private void navigateToAdminFunctionalities() {
        // Navigate to the AdminFunctionalitiesActivity
        Intent intent = new Intent(AdminPortalActivity.this, AdminFunctionalitiesActivity.class);
        startActivity(intent);
    }
}
