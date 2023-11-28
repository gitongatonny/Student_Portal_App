package com.app.dekutstudentportal;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button staffPortalButton = findViewById(R.id.lecLoginBtn);
        Button studentPortalButton = findViewById(R.id.studentLoginBtn);

        //Staff login btn
        staffPortalButton.setOnClickListener(v -> {
            Intent staffIntent = new Intent(MainActivity.this, LecLoginActivity.class);
            startActivity(staffIntent);
        });

        //Student Login btn
        studentPortalButton.setOnClickListener(v -> {
            Intent studentIntent = new Intent(MainActivity.this, StudentLoginActivity.class);
            startActivity(studentIntent);
        });
    }
}