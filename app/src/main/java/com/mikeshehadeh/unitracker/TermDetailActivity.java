package com.mikeshehadeh.unitracker;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class TermDetailActivity extends AppCompatActivity {
    private SQLiteDatabase dB;
    private Cursor mCursor;
    private int term;
    private TextView tvStartDate;
    private TextView tvEndDate;
    //private TextView tvTerm;
    private TextView tvTermStatus;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private CoursesInTermAdapter mAdapter;
    private FloatingActionButton buttonAddCrseToTerm;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_term_detail);
        Bundle b = getIntent().getExtras();
        term = b.getInt("term");


        DBHelper dbHelper = new DBHelper(this);

        dB = dbHelper.getWritableDatabase();

        setTextViews();


        mCursor = getAllItems();

        configureAddCourseToTermButton();
        configureBackButton();
        configureEditButton();
        buildRecyclerView();


    }

    private void buildRecyclerView() {
        mRecyclerView = findViewById(R.id.term_details_recycler_view_courses);
        mLayoutManager = new LinearLayoutManager(this);
        mAdapter = new CoursesInTermAdapter(TermDetailActivity.this, getAssignedCrses());

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

        //Slide to delete alerts
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                removeItem((long) viewHolder.itemView.getTag());

            }
        }).attachToRecyclerView(mRecyclerView);

        mAdapter.setOnItemClickListener(new CoursesInTermAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                RecyclerView.ViewHolder viewHolder = mRecyclerView.findViewHolderForAdapterPosition(position);
                //Put code here for what happens when an item is clicked
                long tag = (long) viewHolder.itemView.getTag();
                showCourseDetails(tag);
            }
        });
    }

    private void showCourseDetails(long tag) {
        Intent i = new Intent(this, CourseDetailActivity.class);
        i.putExtra("courseTag", tag);
        startActivity(i);
    }

    private void removeItem(long tag) {
        //update course table, set term ID to ""
        ContentValues cv = new ContentValues();
        cv.put(DBTables.courseTable.COLUMN_TERM_ID, "");

        String whereClause = DBTables.courseTable.COLUMN_COURSE_ID + "= ?";
        String[] whereArgs = new String[]{Long.toString(tag)};
        dB.update(DBTables.courseTable.TABLE_NAME,
                cv,
                whereClause,
                whereArgs);
        mAdapter.swapCursor(getAssignedCrses());


    }




    private Cursor getAssignedCrses() {
        String whereClause = DBTables.courseTable.COLUMN_TERM_ID + "= ?";
        String[] whereArgs = new String[]{Long.toString(term)};
        return dB.query(DBTables.courseTable.TABLE_NAME,
                null,
                whereClause,
                whereArgs,
                null,
                null,
                null
        );
    }

    private void configureAddCourseToTermButton() {
        buttonAddCrseToTerm = findViewById(R.id.term_details_button_add_course);
        buttonAddCrseToTerm.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                addCourseToTerm();
            }
        });
    }


    private void setTextViews() {
        tvStartDate = (TextView)findViewById(R.id.term_details_textview_start_date);
        tvEndDate = (TextView)findViewById(R.id.term_details_textview_end_date);
        tvTermStatus = (TextView)findViewById(R.id.term_details_textview_term_status);
        String title = "Term " + term + " Details";
        setTitle(title);

        String sql = "SELECT " + DBTables.termTable.COLUMN_TERM_STARTDATE + " FROM " +
                DBTables.termTable.TABLE_NAME + " WHERE " +
                DBTables.termTable.COLUMN_TERM_ID + "=" + term +";";
        SQLiteStatement statement = dB.compileStatement(sql);
        String startDate = statement.simpleQueryForString();

        sql = "SELECT " + DBTables.termTable.COLUMN_TERM_ENDDATE + " FROM " +
                DBTables.termTable.TABLE_NAME + " WHERE " +
                DBTables.termTable.COLUMN_TERM_ID + "=" + term +";";
        statement = dB.compileStatement(sql);
        String endDate = statement.simpleQueryForString();

        tvTermStatus.setText("Status:  " + getTermStatus());
        tvStartDate.setText("Start Date:  " + startDate);
        tvEndDate.setText("End Date:  " + endDate);

    }

    private String getTermStatus() {
        String selection = DBTables.termTable.COLUMN_TERM_ID + "=?";
        String[] termStringArray = {String.valueOf(term)};
        Cursor c = dB.query(DBTables.termTable.TABLE_NAME, null,
                selection, termStringArray, null, null,null);
        c.moveToFirst();
        String startDateStr = c.getString(c.getColumnIndex(DBTables.termTable.COLUMN_TERM_STARTDATE));
        String endDateStr = c.getString(c.getColumnIndex(DBTables.termTable.COLUMN_TERM_ENDDATE));
        Calendar startDateCal = parseStringToCal(startDateStr);
        Calendar endDateCal = parseStringToCal(endDateStr);
        Calendar todayWithTime = Calendar.getInstance();
        String todayWithoutTime = parseCalToString(todayWithTime);
        Calendar today = parseStringToCal(todayWithoutTime);

        if(startDateCal.after(today)) {
            return "Future Term";
        }
        if((startDateCal.before(today) || startDateCal.equals(today)) && (endDateCal.after(today) || endDateCal.equals(today))){
            return "Currently Enrolled";
        }
        else return "Completed";


        //get today, get start date,
        // if term start date is after today, term status is "future term"
        // if term start date is before or equal today, and end date is equal or after today, status is "currently enrolled"
        // else term is completed

    }

    private void configureBackButton() {
        FloatingActionButton backButton = (FloatingActionButton) findViewById(R.id.term_details_button_back);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }


    private void configureEditButton() {
        FloatingActionButton editButton = (FloatingActionButton) findViewById(R.id.term_details_button_edit_term);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!restrictedFromDelete()){
                    showConfirmDialog();

                }
            }
        });
    }

    //check if there are subsequent terms or courses assigned.
    private boolean restrictedFromDelete() {
        boolean bool = false;
        int maxTerm = 0;
        Cursor c = dB.query(DBTables.termTable.TABLE_NAME, null,null,null,null,null,null);
        c.moveToFirst();
        for(c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
            int i = c.getInt(c.getColumnIndex(DBTables.termTable.COLUMN_TERM_ID));
            if(i>maxTerm){maxTerm = i;}
        }
        if (maxTerm > term) {
            bool = true;
        }
        String whereClause = DBTables.courseTable.COLUMN_TERM_ID + "=?";
        String[] whereArgs = new String[]{Integer.toString(term)};
        Cursor c2 = dB.query(DBTables.courseTable.TABLE_NAME, null,
                whereClause, whereArgs, null,null,null);
        if(c2!=null && c2.getCount()>0) {bool = true;}
        if(bool){
            //toast
            String toastString = "You must delete subsequent terms and/or remove assigned courses prior to deleting this term.";
            Toast.makeText(getApplicationContext(), toastString, Toast.LENGTH_LONG).show();
        }
        return bool;
    }


    private void showConfirmDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Term " + term +"?");
        builder.setMessage("You are about to delete Term " + term + ". Do you really want to proceed?");
        builder.setCancelable(false);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dB.delete(DBTables.termTable.TABLE_NAME,
                        DBTables.termTable.COLUMN_TERM_ID + "=" + term, null);
                Toast.makeText(getApplicationContext(), "Term deleted!", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getApplicationContext(), "Cancelled.", Toast.LENGTH_SHORT).show();
            }
        });

        builder.show();
    }


    private String parseCalToString(Calendar lastTermEndCal) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
        return dateFormat.format(lastTermEndCal.getTime());

    }

    private Calendar parseStringToCal(String lastTermEndDate) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
        try {
            cal.setTime(dateFormat.parse(lastTermEndDate));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return cal;
    }


    public void swapCursor(Cursor newCursor) {
        if (mCursor != null) {
            mCursor.close();
        }
        mCursor = newCursor;
        //if (newCursor != null) {
            //notifyDataSetChanged();
        //}
    }


    private Cursor getAllItems() {
        return dB.query(DBTables.termTable.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                DBTables.termTable.COLUMN_TERM_ID + " ASC"

        );
    }

    private void addCourseToTerm(){
        LayoutInflater factory = LayoutInflater.from(this);
        final View addCrseToTermDialogView = factory.inflate(R.layout.dialog_course_list_dropdown, null);
        final AlertDialog addCrseToTerm = new AlertDialog.Builder(this).create();
        addCrseToTerm.setView(addCrseToTermDialogView);

        final Spinner spinner = (Spinner) addCrseToTermDialogView.findViewById(R.id.dialog_crse_lst_dropdown_spinner);
        String[] spinnerItems = getAllCourseNames();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, spinnerItems);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);


        //save new term button clicked
        addCrseToTermDialogView.findViewById(R.id.dialog_crse_lst_dropdown_btn_add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String spinnerSelection = spinner.getSelectedItem().toString();
                String designatorToAdd = spinnerSelection.substring(0, spinnerSelection.indexOf(" "));

                String whereClause = DBTables.courseTable.COLUMN_COURSE_DESIGNATOR + "= ?";
                String[] whereArgs = new String[]{designatorToAdd};
                ContentValues cv = new ContentValues();
                cv.put(DBTables.courseTable.COLUMN_TERM_ID, term);

                dB.update(DBTables.courseTable.TABLE_NAME,
                        cv,
                        whereClause,
                        whereArgs);
                mAdapter.swapCursor(getAssignedCrses());

                addCrseToTerm.dismiss();

            }
        });

        //cancel button clicked
        addCrseToTermDialogView.findViewById(R.id.dialog_crse_lst_dropdown_btn_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addCrseToTerm.dismiss();
            }
        });


        addCrseToTerm.show();

    }

    private String[] getAllCourseNames() {
        ArrayList<String> courseNameList = new ArrayList<>();
        String[] columnName = new String[]{DBTables.courseTable.COLUMN_COURSE_NAME};

        Cursor c = dB.query(DBTables.courseTable.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null);
        c.moveToFirst();

        if(c != null) {
            while(c.isAfterLast() == false) {
                String stringToAdd;
                stringToAdd = c.getString(c.getColumnIndex(DBTables.courseTable.COLUMN_COURSE_DESIGNATOR));
                stringToAdd = stringToAdd + " - " + c.getString(c.getColumnIndex(DBTables.courseTable.COLUMN_COURSE_NAME));
                courseNameList.add(stringToAdd);
                c.moveToNext();
            }
        }


        return courseNameList.toArray(new String[courseNameList.size()]);
    }
}
