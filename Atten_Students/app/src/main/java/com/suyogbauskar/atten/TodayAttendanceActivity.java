package com.suyogbauskar.atten;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.suyogbauskar.atten.pojos.Subject;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class TodayAttendanceActivity extends AppCompatActivity {

    private TableLayout table;
    private boolean isFirstRow;
    private int studentRollNo, studentBatch, date, year;
    private Map<String, Subject> allSubjects;
    private String studentDivision, monthStr, completeDivisionName, completeBatchName;
    private final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_today_attendance);
        setTitle("Today's Attendance");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        table = findViewById(R.id.table);
        isFirstRow = true;
        allSubjects = new HashMap<>();
        getAllSubjects();
    }

    private void getDateAndTime() {
        long currentDate = System.currentTimeMillis();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy/HH/mm/ss/MMMM", Locale.getDefault());
        String dateStr = dateFormat.format(currentDate);
        String[] dateArr = dateStr.split("/");
        date = Integer.parseInt(dateArr[0]);
        year = Integer.parseInt(dateArr[2]);
        monthStr = dateArr[6];
    }

    private void getAllSubjects() {
        FirebaseDatabase.getInstance().getReference("students_data/" + user.getUid() + "/subjects/")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dsp: snapshot.getChildren()) {
                            allSubjects.put(dsp.getKey(), new Subject(dsp.child("subjectName").getValue(String.class), dsp.child("subjectShortName").getValue(String.class)));
                        }
                        getAllStudentData();
                        getDateAndTime();
                        drawTableHeader();
                        createTableRowsOfAttendance(completeDivisionName, studentDivision);
                        createTableRowsOfAttendance(completeBatchName, studentDivision + studentBatch);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(TodayAttendanceActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void getAllStudentData() {
        SharedPreferences sh = getSharedPreferences("allDataPref", MODE_PRIVATE);
        studentRollNo = sh.getInt("rollNo", 0);
        studentDivision = sh.getString("division", "");
        studentBatch = sh.getInt("batch", 0);
        completeDivisionName = sh.getString("completeDivisionName", "");
        completeBatchName = sh.getString("completeBatchName", "");
    }

    private void createTableRowsOfAttendance(String classPath, String attendanceOf) {
        FirebaseDatabase.getInstance().getReference("attendance")
                .child(classPath)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists()) {
                            return;
                        }
                        //All Subjects code
                        for (DataSnapshot dsp: snapshot.getChildren()) {
                            String subjectCode = dsp.getKey();
                            dsp.getRef().child(String.valueOf(year)).child(monthStr).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    String dateStr;
                                    final String[] lectureCount = new String[1];
                                    //All days
                                    for (DataSnapshot dsp: snapshot.getChildren()) {
                                        dateStr = dsp.getKey().split("-")[0];

                                        //If current date is equal to database date
                                        if (dateStr.equals(String.valueOf(date))) {
                                            dsp.getRef().orderByChild("rollNo").equalTo(studentRollNo).addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    lectureCount[0] = dsp.getKey().split("-")[1];

                                                    //Student is not present
                                                    if (!snapshot.exists()) {
                                                        switch (attendanceOf) {
                                                            case "A":
                                                            case "B":
                                                            case "C":
                                                                createTableRow(allSubjects.get(subjectCode).getShortName(), "Lecture", lectureCount[0], "❌");
                                                                break;

                                                            case "A1":
                                                            case "A2":
                                                            case "A3":
                                                            case "B1":
                                                            case "B2":
                                                            case "B3":
                                                            case "C1":
                                                            case "C2":
                                                            case "C3":
                                                                createTableRow(allSubjects.get(subjectCode).getShortName(), "Practical", lectureCount[0], "❌");
                                                                break;
                                                        }
                                                    } else {
                                                        switch (attendanceOf) {
                                                            case "A":
                                                            case "B":
                                                            case "C":
                                                                createTableRow(allSubjects.get(subjectCode).getShortName(), "Lecture", lectureCount[0], "✅");
                                                                break;

                                                            case "A1":
                                                            case "A2":
                                                            case "A3":
                                                            case "B1":
                                                            case "B2":
                                                            case "B3":
                                                            case "C1":
                                                            case "C2":
                                                            case "C3":
                                                                createTableRow(allSubjects.get(subjectCode).getShortName(), "Practical", lectureCount[0], "✅");
                                                                break;
                                                        }
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {
                                                    Toast.makeText(TodayAttendanceActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Toast.makeText(TodayAttendanceActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(TodayAttendanceActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void drawTableHeader() {
        TableRow tbRow = new TableRow(TodayAttendanceActivity.this);

        TextView tv0 = new TextView(TodayAttendanceActivity.this);
        TextView tv1 = new TextView(TodayAttendanceActivity.this);
        TextView tv2 = new TextView(TodayAttendanceActivity.this);
        TextView tv3 = new TextView(TodayAttendanceActivity.this);

        tv0.setText("Subject");
        tv1.setText("Period");
        tv2.setText("Period No.");
        tv3.setText("Attendance");

        tv0.setTypeface(Typeface.DEFAULT_BOLD);
        tv1.setTypeface(Typeface.DEFAULT_BOLD);
        tv2.setTypeface(Typeface.DEFAULT_BOLD);
        tv3.setTypeface(Typeface.DEFAULT_BOLD);

        tv0.setTextSize(18);
        tv1.setTextSize(18);
        tv2.setTextSize(18);
        tv3.setTextSize(18);

        tv0.setPadding(30, 30, 15, 30);
        tv1.setPadding(30, 30, 15, 30);
        tv2.setPadding(30, 30, 15, 30);
        tv3.setPadding(30, 30, 15, 30);

        tv0.setGravity(Gravity.CENTER);
        tv1.setGravity(Gravity.CENTER);
        tv2.setGravity(Gravity.CENTER);
        tv3.setGravity(Gravity.CENTER);

        tv0.setTextColor(Color.BLACK);
        tv1.setTextColor(Color.BLACK);
        tv2.setTextColor(Color.BLACK);
        tv3.setTextColor(Color.BLACK);

        tv0.setBackgroundColor(getResources().getColor(R.color.table_header));
        tv1.setBackgroundColor(getResources().getColor(R.color.table_header));
        tv2.setBackgroundColor(getResources().getColor(R.color.table_header));
        tv3.setBackgroundColor(getResources().getColor(R.color.table_header));

        tbRow.addView(tv0);
        tbRow.addView(tv1);
        tbRow.addView(tv2);
        tbRow.addView(tv3);

        table.addView(tbRow);
    }

    private void createTableRow(String subjectName, String period, String periodNo, String attendance) {
        TableRow tbRow = new TableRow(TodayAttendanceActivity.this);

        TextView tv0 = new TextView(TodayAttendanceActivity.this);
        TextView tv1 = new TextView(TodayAttendanceActivity.this);
        TextView tv2 = new TextView(TodayAttendanceActivity.this);
        TextView tv3 = new TextView(TodayAttendanceActivity.this);

        tv0.setText(subjectName);
        tv1.setText(period);
        tv2.setText(periodNo);
        tv3.setText(attendance);

        tv0.setTextSize(16);
        tv1.setTextSize(16);
        tv2.setTextSize(16);
        tv3.setTextSize(16);

        tv0.setPadding(30, 30, 15, 30);
        tv1.setPadding(30, 30, 15, 30);
        tv2.setPadding(30, 30, 15, 30);
        tv3.setPadding(30, 30, 15, 30);

        tv0.setGravity(Gravity.CENTER);
        tv1.setGravity(Gravity.CENTER);
        tv2.setGravity(Gravity.CENTER);
        tv3.setGravity(Gravity.CENTER);

        tv0.setBackgroundResource(R.drawable.borders);
        tv1.setBackgroundResource(R.drawable.borders);
        tv2.setBackgroundResource(R.drawable.borders);
        tv3.setBackgroundResource(R.drawable.borders);

        tv0.setTextColor(Color.BLACK);
        tv1.setTextColor(Color.BLACK);
        tv2.setTextColor(Color.BLACK);
        tv3.setTextColor(Color.BLACK);

        if (isFirstRow) {
            tv0.setBackgroundColor(getResources().getColor(R.color.white));
            tv1.setBackgroundColor(getResources().getColor(R.color.white));
            tv2.setBackgroundColor(getResources().getColor(R.color.white));
            tv3.setBackgroundColor(getResources().getColor(R.color.white));
            isFirstRow = false;
        } else {
            tv0.setBackgroundColor(getResources().getColor(R.color.light_gray));
            tv1.setBackgroundColor(getResources().getColor(R.color.light_gray));
            tv2.setBackgroundColor(getResources().getColor(R.color.light_gray));
            tv3.setBackgroundColor(getResources().getColor(R.color.light_gray));
            isFirstRow = true;
        }

        tbRow.addView(tv0);
        tbRow.addView(tv1);
        tbRow.addView(tv2);
        tbRow.addView(tv3);

        table.addView(tbRow);
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(TodayAttendanceActivity.this, HomeActivity.class));
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}