package com.app.dekutstudentportal;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class GenerateReportsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_reports);

        // Pass All Courses in a Semester Btn
        Button passAllCoursesSemesterBtn = findViewById(R.id.passAllCoursesSemesterBtn);
        passAllCoursesSemesterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(GenerateReportsActivity.this, PassAllCoursesSemesterActivity.class));
            }
        });

        // Pass All Courses in a Year Btn
        Button passAllCoursesYearBtn = findViewById(R.id.passAllCoursesYearBtn);
        passAllCoursesYearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(GenerateReportsActivity.this, PassAllCoursesYearActivity.class));
            }
        });

        // Failed Courses in a Semester Btn
        Button failCoursesSemesterBtn = findViewById(R.id.failCoursesSemesterBtn);
        failCoursesSemesterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(GenerateReportsActivity.this, FailCoursesSemesterActivity.class));
            }
        });

        // Failed Courses in a Year Btn
        Button failCoursesYearBtn = findViewById(R.id.failCoursesYearBtn);
        failCoursesYearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(GenerateReportsActivity.this, FailCoursesYearActivity.class));
            }
        });

        // Special Cases Btn
        Button specialCasesBtn = findViewById(R.id.specialCasesBtn);
        specialCasesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(GenerateReportsActivity.this, SpecialCasesActivity.class));
            }
        });

        // Partial Missing Marks Btn
        Button partialMissingMarksBtn = findViewById(R.id.partialMissingMarksBtn);
        partialMissingMarksBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(GenerateReportsActivity.this, PartialMissingMarksActivity.class));
            }
        });

// Full Missing Marks Btn
        Button fullMissingMarksBtn = findViewById(R.id.fullMissingMarksBtn);
        fullMissingMarksBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(GenerateReportsActivity.this, fullMissingMarksActivity.class));
            }
        });

// Retake Btn
        Button retakeBtn = findViewById(R.id.retakeBtn);
        retakeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(GenerateReportsActivity.this, retakeActivity.class));
            }
        });


    }
}
