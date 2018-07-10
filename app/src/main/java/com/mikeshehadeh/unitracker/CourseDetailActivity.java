package com.mikeshehadeh.unitracker;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class CourseDetailActivity extends AppCompatActivity {

    private long courseID;
    private SQLiteDatabase dB;
    private String courseName;
    private String courseDesignator;
    private String courseStartDate;
    private String courseEndDate;
    private String courseCus;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_detail);

        Bundle b = getIntent().getExtras();
        courseID = b.getLong("courseTag");

        DBHelper dbHelper = new DBHelper(this);
        dB = dbHelper.getWritableDatabase();

        configureBackButton();
        configureAssessmentsButton();
        configureNotesButton();
        configureAlertButton();
        configureMentorsButton();
        getAllItems();
        setTextViews();

    }

    private void configureMentorsButton() {
        Button assmtButton = (Button) findViewById(R.id.crse_dtl_mentors_button);
        assmtButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(CourseDetailActivity.this, CourseMentorListActivity.class);
                i.putExtra("courseID", courseID);
                startActivity(i);

            }
        });
    }

    private void configureAlertButton() {
        Button alertButton = (Button) findViewById(R.id.crse_dtl_alerts_button);
        alertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(CourseDetailActivity.this, CourseAlertListActivity.class);
                i.putExtra("courseID", courseID);
                i.putExtra("courseDesignator", courseDesignator);
                startActivity(i);

            }
        });
    }

    private void configureNotesButton() {
        Button assmtButton = (Button) findViewById(R.id.crse_dtl_notes_button);
        assmtButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(CourseDetailActivity.this, NoteListActivity.class);
                i.putExtra("courseID", courseID);
                startActivity(i);

            }
        });
    }

    private void configureAssessmentsButton() {
        Button assmtButton = (Button) findViewById(R.id.crse_dtl_assmt_button);
        assmtButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(CourseDetailActivity.this, AssessmentListActivity.class);
                i.putExtra("courseID", courseID);
                startActivity(i);

            }
        });
    }

    private void setTextViews() {
        TextView tvCourseName = (TextView) findViewById(R.id.crse_dtl_name);
        TextView tvCourseDesignator = (TextView) findViewById(R.id.crse_dtl_textView_designator);
        TextView tvCourseStartDate = (TextView) findViewById(R.id.crse_dtl_textView_start_date);
        TextView tvCourseEndDate = (TextView) findViewById(R.id.crse_dtl_textView_endDate);
        TextView tvCus = (TextView) findViewById(R.id.crse_dtl_textView_cus);

        if (courseStartDate == null) {
            courseStartDate = "";
        }

        if (courseEndDate == null) {
            courseEndDate = "";
        }

        tvCourseName.setText(courseName);
        tvCourseDesignator.setText(courseDesignator);
        tvCourseStartDate.setText("Start Date:  " + courseStartDate);
        tvCourseEndDate.setText("End Date:  " + courseEndDate);
        tvCus.setText(courseCus + " CUs");


    }

    private void configureBackButton() {
        FloatingActionButton backButton = (FloatingActionButton) findViewById(R.id.crse_dtl_btn_back);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void getAllItems() {
        String whereClause = DBTables.courseTable.COLUMN_COURSE_ID + "= ?";
        String[] whereArgs = new String[]{Long.toString(courseID)};

        Cursor c = dB.query(DBTables.courseTable.TABLE_NAME,
                null,
                whereClause,
                whereArgs,
                null,
                null,
                null
        );
        //String startDateStr = c.getString(c.getColumnIndex(DBTables.termTable.COLUMN_TERM_STARTDATE));
        c.moveToFirst();


        courseName = c.getString(c.getColumnIndex(DBTables.courseTable.COLUMN_COURSE_NAME));
        String str = c.getString(c.getColumnIndex(DBTables.courseTable.COLUMN_COURSE_DESIGNATOR));
        courseDesignator = str + " - ";
        courseStartDate = c.getString(c.getColumnIndex(DBTables.courseTable.COLUMN_STARTDATE));

        courseEndDate = c.getString(c.getColumnIndex(DBTables.courseTable.COLUMN_ENDDATE_ANTIC));
        courseCus = c.getString(c.getColumnIndex(DBTables.courseTable.COLUMN_CU));


    }
}
