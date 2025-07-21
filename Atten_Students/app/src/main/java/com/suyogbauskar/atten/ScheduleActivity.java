package com.suyogbauskar.atten;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

public class ScheduleActivity extends AppCompatActivity {

    private TableLayout table;
    private Button filterBtn;
    private boolean isFirstRow;
    private Date date;
    private int day, hour, minute, studentSemester, studentBatch;
    private String studentDepartment, studentDivision;
    private TextView noScheduleView;
    private List<List<String>> completeTimetable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);
        setTitle("Schedule");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        findAllViews();
        filterBtn.setOnClickListener(v -> showViewByDialog());
        getAllStudentData();
        setTimeTableData();
        viewBy();
    }

    private void findAllViews() {
        table = findViewById(R.id.table);
        filterBtn = findViewById(R.id.filterBtn);
        noScheduleView = findViewById(R.id.noScheduleView);
    }

    private void getAllStudentData() {
        SharedPreferences sh = getSharedPreferences("allDataPref", MODE_PRIVATE);
        studentSemester = sh.getInt("semester", 0);
        studentBatch = sh.getInt("batch", 0);
        studentDepartment = sh.getString("department", "");
        studentDivision = sh.getString("division", "");
    }

    private void setTimeTableData() {
        completeTimetable = new ArrayList<>();
        List<String> tempList;

        try {
            String filename = studentDepartment + studentSemester + "_Timetable.csv";
            File file = new File(getApplicationContext().getExternalFilesDir(null), filename);

            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] splitted = line.split(",");
                tempList = new ArrayList<>();
                for (String s : splitted) {
                    tempList.add(s);
                }
                completeTimetable.add(tempList);
            }

        } catch (Exception e) {
            Log.d(TAG, "Error: " + e.getMessage());
        }

        Calendar calendar = Calendar.getInstance();
        date = calendar.getTime();
        day = calendar.get(Calendar.DAY_OF_WEEK);
        hour = calendar.get(Calendar.HOUR_OF_DAY);
        minute = calendar.get(Calendar.MINUTE);
    }

    private void viewBy() {
        SharedPreferences sh = getSharedPreferences("viewByPref", MODE_PRIVATE);
        String viewBy = sh.getString("viewBy", "All");

        if (day == 1 && viewBy.equals("Today")) {
            noScheduleView.setVisibility(View.VISIBLE);
            return;
        }

        switch (viewBy) {
            case "All":
                showTableOfAll();
                break;

            case "A":
                showTableOfA();
                break;

            case "B":
                showTableOfB();
                break;

            case "C":
                showTableOfC();
                break;

            case "Today":
                showTableOfToday(studentDivision + studentBatch);
                break;
        }
    }

    private void showTableOfAll() {
        drawTableHeaderOfCompleteTimetable();

        for (int i = 1; i < completeTimetable.size(); i++) {
            createTableRowOfCompleteTimetable(completeTimetable.get(i).get(0), completeTimetable.get(i).get(1), completeTimetable.get(i).get(2),
                    completeTimetable.get(i).get(3), completeTimetable.get(i).get(4), completeTimetable.get(i).get(5), completeTimetable.get(i).get(6),
                    completeTimetable.get(i).get(7), completeTimetable.get(i).get(8), completeTimetable.get(i).get(9), completeTimetable.get(i).get(10),
                    completeTimetable.get(i).get(11));
        }
    }

    private void showTableOfA() {
        drawTableHeaderOfCompleteTimetable();

        for (int i = 1; i < completeTimetable.size(); i++) {
            if (completeTimetable.get(i).get(1).equals("A")) {
                createTableRowOfCompleteTimetable(completeTimetable.get(i).get(0), completeTimetable.get(i).get(1), completeTimetable.get(i).get(2),
                        completeTimetable.get(i).get(3), completeTimetable.get(i).get(4), completeTimetable.get(i).get(5), completeTimetable.get(i).get(6),
                        completeTimetable.get(i).get(7), completeTimetable.get(i).get(8), completeTimetable.get(i).get(9), completeTimetable.get(i).get(10),
                        completeTimetable.get(i).get(11));
            }
        }
    }

    private void showTableOfB() {
        drawTableHeaderOfCompleteTimetable();

        for (int i = 1; i < completeTimetable.size(); i++) {
            if (completeTimetable.get(i).get(1).equals("B")) {
                createTableRowOfCompleteTimetable(completeTimetable.get(i).get(0), completeTimetable.get(i).get(1), completeTimetable.get(i).get(2),
                        completeTimetable.get(i).get(3), completeTimetable.get(i).get(4), completeTimetable.get(i).get(5), completeTimetable.get(i).get(6),
                        completeTimetable.get(i).get(7), completeTimetable.get(i).get(8), completeTimetable.get(i).get(9), completeTimetable.get(i).get(10),
                        completeTimetable.get(i).get(11));
            }
        }
    }

    private void showTableOfC() {
        drawTableHeaderOfCompleteTimetable();

        for (int i = 1; i < completeTimetable.size(); i++) {
            if (completeTimetable.get(i).get(1).equals("C")) {
                createTableRowOfCompleteTimetable(completeTimetable.get(i).get(0), completeTimetable.get(i).get(1), completeTimetable.get(i).get(2),
                        completeTimetable.get(i).get(3), completeTimetable.get(i).get(4), completeTimetable.get(i).get(5), completeTimetable.get(i).get(6),
                        completeTimetable.get(i).get(7), completeTimetable.get(i).get(8), completeTimetable.get(i).get(9), completeTimetable.get(i).get(10),
                        completeTimetable.get(i).get(11));
            }
        }
    }

    private void creatorOfTodayRows(int index) {
        if ((hour == 10 && minute >= 30) || (hour == 11 && minute < 30)) {
            createTableRowOfTodayTimetable(completeTimetable.get(0).get(3), completeTimetable.get(index).get(3), getResources().getColor(R.color.light_blue));
            createTableRowOfTodayTimetable(completeTimetable.get(0).get(4), completeTimetable.get(index).get(4), getResources().getColor(R.color.white));
            createTableRowOfTodayTimetable(completeTimetable.get(0).get(5), completeTimetable.get(index).get(5), getResources().getColor(R.color.light_gray));
            createTableRowOfTodayTimetable(completeTimetable.get(0).get(6), completeTimetable.get(index).get(6), getResources().getColor(R.color.white));
            createTableRowOfTodayTimetable(completeTimetable.get(0).get(7), completeTimetable.get(index).get(7), getResources().getColor(R.color.light_gray));
            createTableRowOfTodayTimetable(completeTimetable.get(0).get(8), completeTimetable.get(index).get(8), getResources().getColor(R.color.white));
            createTableRowOfTodayTimetable(completeTimetable.get(0).get(9), completeTimetable.get(index).get(9), getResources().getColor(R.color.light_gray));
            createTableRowOfTodayTimetable(completeTimetable.get(0).get(10), completeTimetable.get(index).get(10), getResources().getColor(R.color.white));
            createTableRowOfTodayTimetable(completeTimetable.get(0).get(11), completeTimetable.get(index).get(11), getResources().getColor(R.color.light_gray));
        } else if (hour == 11 || hour == 12 && minute < 30) {
            createTableRowOfTodayTimetable(completeTimetable.get(0).get(3), completeTimetable.get(index).get(3), getResources().getColor(R.color.white));
            createTableRowOfTodayTimetable(completeTimetable.get(0).get(4), completeTimetable.get(index).get(4), getResources().getColor(R.color.light_blue));
            createTableRowOfTodayTimetable(completeTimetable.get(0).get(5), completeTimetable.get(index).get(5), getResources().getColor(R.color.light_gray));
            createTableRowOfTodayTimetable(completeTimetable.get(0).get(6), completeTimetable.get(index).get(6), getResources().getColor(R.color.white));
            createTableRowOfTodayTimetable(completeTimetable.get(0).get(7), completeTimetable.get(index).get(7), getResources().getColor(R.color.light_gray));
            createTableRowOfTodayTimetable(completeTimetable.get(0).get(8), completeTimetable.get(index).get(8), getResources().getColor(R.color.white));
            createTableRowOfTodayTimetable(completeTimetable.get(0).get(9), completeTimetable.get(index).get(9), getResources().getColor(R.color.light_gray));
            createTableRowOfTodayTimetable(completeTimetable.get(0).get(10), completeTimetable.get(index).get(10), getResources().getColor(R.color.white));
            createTableRowOfTodayTimetable(completeTimetable.get(0).get(11), completeTimetable.get(index).get(11), getResources().getColor(R.color.light_gray));
        } else if (hour == 12 || hour == 1 && minute < 30) {
            createTableRowOfTodayTimetable(completeTimetable.get(0).get(3), completeTimetable.get(index).get(3), getResources().getColor(R.color.white));
            createTableRowOfTodayTimetable(completeTimetable.get(0).get(4), completeTimetable.get(index).get(4), getResources().getColor(R.color.light_gray));
            createTableRowOfTodayTimetable(completeTimetable.get(0).get(5), completeTimetable.get(index).get(5), getResources().getColor(R.color.light_blue));
            createTableRowOfTodayTimetable(completeTimetable.get(0).get(6), completeTimetable.get(index).get(6), getResources().getColor(R.color.white));
            createTableRowOfTodayTimetable(completeTimetable.get(0).get(7), completeTimetable.get(index).get(7), getResources().getColor(R.color.light_gray));
            createTableRowOfTodayTimetable(completeTimetable.get(0).get(8), completeTimetable.get(index).get(8), getResources().getColor(R.color.white));
            createTableRowOfTodayTimetable(completeTimetable.get(0).get(9), completeTimetable.get(index).get(9), getResources().getColor(R.color.light_gray));
            createTableRowOfTodayTimetable(completeTimetable.get(0).get(10), completeTimetable.get(index).get(10), getResources().getColor(R.color.white));
            createTableRowOfTodayTimetable(completeTimetable.get(0).get(11), completeTimetable.get(index).get(11), getResources().getColor(R.color.light_gray));
        } else if (hour == 13 && minute < 50) {
            createTableRowOfTodayTimetable(completeTimetable.get(0).get(3), completeTimetable.get(index).get(3), getResources().getColor(R.color.white));
            createTableRowOfTodayTimetable(completeTimetable.get(0).get(4), completeTimetable.get(index).get(4), getResources().getColor(R.color.light_gray));
            createTableRowOfTodayTimetable(completeTimetable.get(0).get(5), completeTimetable.get(index).get(5), getResources().getColor(R.color.white));
            createTableRowOfTodayTimetable(completeTimetable.get(0).get(6), completeTimetable.get(index).get(6), getResources().getColor(R.color.light_blue));
            createTableRowOfTodayTimetable(completeTimetable.get(0).get(7), completeTimetable.get(index).get(7), getResources().getColor(R.color.light_gray));
            createTableRowOfTodayTimetable(completeTimetable.get(0).get(8), completeTimetable.get(index).get(8), getResources().getColor(R.color.white));
            createTableRowOfTodayTimetable(completeTimetable.get(0).get(9), completeTimetable.get(index).get(9), getResources().getColor(R.color.light_gray));
            createTableRowOfTodayTimetable(completeTimetable.get(0).get(10), completeTimetable.get(index).get(10), getResources().getColor(R.color.white));
            createTableRowOfTodayTimetable(completeTimetable.get(0).get(11), completeTimetable.get(index).get(11), getResources().getColor(R.color.light_gray));
        } else if (hour == 13 || hour == 14 && minute < 50) {
            createTableRowOfTodayTimetable(completeTimetable.get(0).get(3), completeTimetable.get(index).get(3), getResources().getColor(R.color.white));
            createTableRowOfTodayTimetable(completeTimetable.get(0).get(4), completeTimetable.get(index).get(4), getResources().getColor(R.color.light_gray));
            createTableRowOfTodayTimetable(completeTimetable.get(0).get(5), completeTimetable.get(index).get(5), getResources().getColor(R.color.white));
            createTableRowOfTodayTimetable(completeTimetable.get(0).get(6), completeTimetable.get(index).get(6), getResources().getColor(R.color.light_gray));
            createTableRowOfTodayTimetable(completeTimetable.get(0).get(7), completeTimetable.get(index).get(7), getResources().getColor(R.color.light_blue));
            createTableRowOfTodayTimetable(completeTimetable.get(0).get(8), completeTimetable.get(index).get(8), getResources().getColor(R.color.white));
            createTableRowOfTodayTimetable(completeTimetable.get(0).get(9), completeTimetable.get(index).get(9), getResources().getColor(R.color.light_gray));
            createTableRowOfTodayTimetable(completeTimetable.get(0).get(10), completeTimetable.get(index).get(10), getResources().getColor(R.color.white));
            createTableRowOfTodayTimetable(completeTimetable.get(0).get(11), completeTimetable.get(index).get(11), getResources().getColor(R.color.light_gray));
        } else if (hour == 14 || hour == 15 && minute < 50) {
            createTableRowOfTodayTimetable(completeTimetable.get(0).get(3), completeTimetable.get(index).get(3), getResources().getColor(R.color.white));
            createTableRowOfTodayTimetable(completeTimetable.get(0).get(4), completeTimetable.get(index).get(4), getResources().getColor(R.color.light_gray));
            createTableRowOfTodayTimetable(completeTimetable.get(0).get(5), completeTimetable.get(index).get(5), getResources().getColor(R.color.white));
            createTableRowOfTodayTimetable(completeTimetable.get(0).get(6), completeTimetable.get(index).get(6), getResources().getColor(R.color.light_gray));
            createTableRowOfTodayTimetable(completeTimetable.get(0).get(7), completeTimetable.get(index).get(7), getResources().getColor(R.color.white));
            createTableRowOfTodayTimetable(completeTimetable.get(0).get(8), completeTimetable.get(index).get(8), getResources().getColor(R.color.light_blue));
            createTableRowOfTodayTimetable(completeTimetable.get(0).get(9), completeTimetable.get(index).get(9), getResources().getColor(R.color.light_gray));
            createTableRowOfTodayTimetable(completeTimetable.get(0).get(10), completeTimetable.get(index).get(10), getResources().getColor(R.color.white));
            createTableRowOfTodayTimetable(completeTimetable.get(0).get(11), completeTimetable.get(index).get(11), getResources().getColor(R.color.light_gray));
        } else if (hour == 15 || hour == 16 && minute < 0) {
            createTableRowOfTodayTimetable(completeTimetable.get(0).get(3), completeTimetable.get(index).get(3), getResources().getColor(R.color.white));
            createTableRowOfTodayTimetable(completeTimetable.get(0).get(4), completeTimetable.get(index).get(4), getResources().getColor(R.color.light_gray));
            createTableRowOfTodayTimetable(completeTimetable.get(0).get(5), completeTimetable.get(index).get(5), getResources().getColor(R.color.white));
            createTableRowOfTodayTimetable(completeTimetable.get(0).get(6), completeTimetable.get(index).get(6), getResources().getColor(R.color.light_gray));
            createTableRowOfTodayTimetable(completeTimetable.get(0).get(7), completeTimetable.get(index).get(7), getResources().getColor(R.color.white));
            createTableRowOfTodayTimetable(completeTimetable.get(0).get(8), completeTimetable.get(index).get(8), getResources().getColor(R.color.light_gray));
            createTableRowOfTodayTimetable(completeTimetable.get(0).get(9), completeTimetable.get(index).get(9), getResources().getColor(R.color.light_blue));
            createTableRowOfTodayTimetable(completeTimetable.get(0).get(10), completeTimetable.get(index).get(10), getResources().getColor(R.color.white));
            createTableRowOfTodayTimetable(completeTimetable.get(0).get(11), completeTimetable.get(index).get(11), getResources().getColor(R.color.light_gray));
        } else if (hour == 16 || hour == 17 && minute < 0) {
            createTableRowOfTodayTimetable(completeTimetable.get(0).get(3), completeTimetable.get(index).get(3), getResources().getColor(R.color.white));
            createTableRowOfTodayTimetable(completeTimetable.get(0).get(4), completeTimetable.get(index).get(4), getResources().getColor(R.color.light_gray));
            createTableRowOfTodayTimetable(completeTimetable.get(0).get(5), completeTimetable.get(index).get(5), getResources().getColor(R.color.white));
            createTableRowOfTodayTimetable(completeTimetable.get(0).get(6), completeTimetable.get(index).get(6), getResources().getColor(R.color.light_gray));
            createTableRowOfTodayTimetable(completeTimetable.get(0).get(7), completeTimetable.get(index).get(7), getResources().getColor(R.color.white));
            createTableRowOfTodayTimetable(completeTimetable.get(0).get(8), completeTimetable.get(index).get(8), getResources().getColor(R.color.light_gray));
            createTableRowOfTodayTimetable(completeTimetable.get(0).get(9), completeTimetable.get(index).get(9), getResources().getColor(R.color.white));
            createTableRowOfTodayTimetable(completeTimetable.get(0).get(10), completeTimetable.get(index).get(10), getResources().getColor(R.color.light_blue));
            createTableRowOfTodayTimetable(completeTimetable.get(0).get(11), completeTimetable.get(index).get(11), getResources().getColor(R.color.light_gray));
        } else if (hour == 17 || hour == 18 && minute < 0) {
            createTableRowOfTodayTimetable(completeTimetable.get(0).get(3), completeTimetable.get(index).get(3), getResources().getColor(R.color.white));
            createTableRowOfTodayTimetable(completeTimetable.get(0).get(4), completeTimetable.get(index).get(4), getResources().getColor(R.color.light_gray));
            createTableRowOfTodayTimetable(completeTimetable.get(0).get(5), completeTimetable.get(index).get(5), getResources().getColor(R.color.white));
            createTableRowOfTodayTimetable(completeTimetable.get(0).get(6), completeTimetable.get(index).get(6), getResources().getColor(R.color.light_gray));
            createTableRowOfTodayTimetable(completeTimetable.get(0).get(7), completeTimetable.get(index).get(7), getResources().getColor(R.color.white));
            createTableRowOfTodayTimetable(completeTimetable.get(0).get(8), completeTimetable.get(index).get(8), getResources().getColor(R.color.light_gray));
            createTableRowOfTodayTimetable(completeTimetable.get(0).get(9), completeTimetable.get(index).get(9), getResources().getColor(R.color.white));
            createTableRowOfTodayTimetable(completeTimetable.get(0).get(10), completeTimetable.get(index).get(10), getResources().getColor(R.color.light_gray));
            createTableRowOfTodayTimetable(completeTimetable.get(0).get(11), completeTimetable.get(index).get(11), getResources().getColor(R.color.light_blue));
        } else {
            createTableRowOfTodayTimetable(completeTimetable.get(0).get(3), completeTimetable.get(index).get(3), getResources().getColor(R.color.white));
            createTableRowOfTodayTimetable(completeTimetable.get(0).get(4), completeTimetable.get(index).get(4), getResources().getColor(R.color.light_gray));
            createTableRowOfTodayTimetable(completeTimetable.get(0).get(5), completeTimetable.get(index).get(5), getResources().getColor(R.color.white));
            createTableRowOfTodayTimetable(completeTimetable.get(0).get(6), completeTimetable.get(index).get(6), getResources().getColor(R.color.light_gray));
            createTableRowOfTodayTimetable(completeTimetable.get(0).get(7), completeTimetable.get(index).get(7), getResources().getColor(R.color.white));
            createTableRowOfTodayTimetable(completeTimetable.get(0).get(8), completeTimetable.get(index).get(8), getResources().getColor(R.color.light_gray));
            createTableRowOfTodayTimetable(completeTimetable.get(0).get(9), completeTimetable.get(index).get(9), getResources().getColor(R.color.white));
            createTableRowOfTodayTimetable(completeTimetable.get(0).get(10), completeTimetable.get(index).get(10), getResources().getColor(R.color.light_gray));
            createTableRowOfTodayTimetable(completeTimetable.get(0).get(11), completeTimetable.get(index).get(11), getResources().getColor(R.color.white));
        }
    }

    private void showTableOfToday(String batch) {
        drawTableHeaderOfTodayTimetable(String.valueOf(new SimpleDateFormat("EEEE", Locale.ENGLISH).format(date.getTime())));

        switch (batch) {
            case "A1":
                switch (day) {
                    case 2:
                        creatorOfTodayRows(1);
                        break;
                    case 3:
                        creatorOfTodayRows(6);
                        break;
                    case 4:
                        creatorOfTodayRows(11);
                        break;
                    case 5:
                        creatorOfTodayRows(16);
                        break;
                    case 6:
                        creatorOfTodayRows(21);
                        break;
                    case 7:
                        creatorOfTodayRows(26);
                        break;
                }
                break;
            case "A2":
                switch (day) {
                    case 2:
                        creatorOfTodayRows(2);
                        break;
                    case 3:
                        creatorOfTodayRows(7);
                        break;
                    case 4:
                        creatorOfTodayRows(12);
                        break;
                    case 5:
                        creatorOfTodayRows(17);
                        break;
                    case 6:
                        creatorOfTodayRows(22);
                        break;
                    case 7:
                        creatorOfTodayRows(27);
                        break;
                }
                break;
            case "A3":
                switch (day) {
                    case 2:
                        creatorOfTodayRows(3);
                        break;
                    case 3:
                        creatorOfTodayRows(8);
                        break;
                    case 4:
                        creatorOfTodayRows(13);
                        break;
                    case 5:
                        creatorOfTodayRows(18);
                        break;
                    case 6:
                        creatorOfTodayRows(23);
                        break;
                    case 7:
                        creatorOfTodayRows(28);
                        break;
                }
                break;
            case "B1":
                switch (day) {
                    case 2:
                        creatorOfTodayRows(4);
                        break;
                    case 3:
                        creatorOfTodayRows(9);
                        break;
                    case 4:
                        creatorOfTodayRows(14);
                        break;
                    case 5:
                        creatorOfTodayRows(19);
                        break;
                    case 6:
                        creatorOfTodayRows(24);
                        break;
                    case 7:
                        creatorOfTodayRows(29);
                        break;
                }
                break;
            case "B2":
                switch (day) {
                    case 2:
                        creatorOfTodayRows(5);
                        break;
                    case 3:
                        creatorOfTodayRows(10);
                        break;
                    case 4:
                        creatorOfTodayRows(15);
                        break;
                    case 5:
                        creatorOfTodayRows(20);
                        break;
                    case 6:
                        creatorOfTodayRows(25);
                        break;
                    case 7:
                        creatorOfTodayRows(30);
                        break;
                }
                break;
        }
    }

    private void drawTableHeaderOfTodayTimetable(String weekday) {
        TableRow tbRow = new TableRow(ScheduleActivity.this);

        TextView tv0 = new TextView(ScheduleActivity.this);
        TextView tv1 = new TextView(ScheduleActivity.this);

        tv0.setText("TIME");
        tv1.setText(weekday);

        tv0.setTypeface(Typeface.DEFAULT_BOLD);
        tv1.setTypeface(Typeface.DEFAULT_BOLD);

        tv0.setTextSize(18);
        tv1.setTextSize(18);

        tv0.setPadding(30, 30, 15, 30);
        tv1.setPadding(30, 30, 15, 30);

        tv0.setGravity(Gravity.CENTER);
        tv1.setGravity(Gravity.CENTER);

        tv0.setTextColor(Color.BLACK);
        tv1.setTextColor(Color.BLACK);

        tv0.setBackgroundColor(getResources().getColor(R.color.table_header));
        tv1.setBackgroundColor(getResources().getColor(R.color.table_header));

        tbRow.addView(tv0);
        tbRow.addView(tv1);

        table.addView(tbRow);
    }

    private void createTableRowOfTodayTimetable(String time, String lecture, int color) {
        TableRow tbRow = new TableRow(ScheduleActivity.this);

        TextView tv0 = new TextView(ScheduleActivity.this);
        TextView tv1 = new TextView(ScheduleActivity.this);

        tv0.setText(time);
        tv1.setText(lecture);

        tv0.setTextSize(16);
        tv1.setTextSize(16);

        tv0.setPadding(30, 30, 15, 30);
        tv1.setPadding(30, 30, 15, 30);

        tv0.setGravity(Gravity.CENTER);
        tv1.setGravity(Gravity.CENTER);

        tv0.setBackgroundResource(R.drawable.borders);
        tv1.setBackgroundResource(R.drawable.borders);

        tv0.setTextColor(Color.BLACK);
        tv1.setTextColor(Color.BLACK);

        tv0.setBackgroundColor(color);
        tv1.setBackgroundColor(color);

        tbRow.addView(tv0);
        tbRow.addView(tv1);

        table.addView(tbRow);
    }

    private void drawTableHeaderOfCompleteTimetable() {
        TableRow tbRow = new TableRow(ScheduleActivity.this);

        TextView tv0 = new TextView(ScheduleActivity.this);
        TextView tv1 = new TextView(ScheduleActivity.this);
        TextView tv2 = new TextView(ScheduleActivity.this);
        TextView tv3 = new TextView(ScheduleActivity.this);
        TextView tv4 = new TextView(ScheduleActivity.this);
        TextView tv5 = new TextView(ScheduleActivity.this);
        TextView tv6 = new TextView(ScheduleActivity.this);
        TextView tv7 = new TextView(ScheduleActivity.this);
        TextView tv8 = new TextView(ScheduleActivity.this);
        TextView tv9 = new TextView(ScheduleActivity.this);
        TextView tv10 = new TextView(ScheduleActivity.this);
        TextView tv11 = new TextView(ScheduleActivity.this);

        tv0.setText(completeTimetable.get(0).get(0));
        tv1.setText(completeTimetable.get(0).get(1));
        tv2.setText(completeTimetable.get(0).get(2));
        tv3.setText(completeTimetable.get(0).get(3));
        tv4.setText(completeTimetable.get(0).get(4));
        tv5.setText(completeTimetable.get(0).get(5));
        tv6.setText(completeTimetable.get(0).get(6));
        tv7.setText(completeTimetable.get(0).get(7));
        tv8.setText(completeTimetable.get(0).get(8));
        tv9.setText(completeTimetable.get(0).get(9));
        tv10.setText(completeTimetable.get(0).get(10));
        tv11.setText(completeTimetable.get(0).get(11));

        tv0.setTypeface(Typeface.DEFAULT_BOLD);
        tv1.setTypeface(Typeface.DEFAULT_BOLD);
        tv2.setTypeface(Typeface.DEFAULT_BOLD);
        tv3.setTypeface(Typeface.DEFAULT_BOLD);
        tv4.setTypeface(Typeface.DEFAULT_BOLD);
        tv5.setTypeface(Typeface.DEFAULT_BOLD);
        tv6.setTypeface(Typeface.DEFAULT_BOLD);
        tv7.setTypeface(Typeface.DEFAULT_BOLD);
        tv8.setTypeface(Typeface.DEFAULT_BOLD);
        tv9.setTypeface(Typeface.DEFAULT_BOLD);
        tv10.setTypeface(Typeface.DEFAULT_BOLD);
        tv11.setTypeface(Typeface.DEFAULT_BOLD);

        tv0.setTextSize(18);
        tv1.setTextSize(18);
        tv2.setTextSize(18);
        tv3.setTextSize(18);
        tv4.setTextSize(18);
        tv5.setTextSize(18);
        tv6.setTextSize(18);
        tv7.setTextSize(18);
        tv8.setTextSize(18);
        tv9.setTextSize(18);
        tv10.setTextSize(18);
        tv11.setTextSize(18);

        tv0.setPadding(30, 30, 15, 30);
        tv1.setPadding(30, 30, 15, 30);
        tv2.setPadding(30, 30, 15, 30);
        tv3.setPadding(30, 30, 15, 30);
        tv4.setPadding(30, 30, 15, 30);
        tv5.setPadding(30, 30, 15, 30);
        tv6.setPadding(30, 30, 15, 30);
        tv7.setPadding(30, 30, 15, 30);
        tv8.setPadding(30, 30, 15, 30);
        tv9.setPadding(30, 30, 15, 30);
        tv10.setPadding(30, 30, 15, 30);
        tv11.setPadding(30, 30, 15, 30);

        tv0.setGravity(Gravity.CENTER);
        tv1.setGravity(Gravity.CENTER);
        tv2.setGravity(Gravity.CENTER);
        tv3.setGravity(Gravity.CENTER);
        tv4.setGravity(Gravity.CENTER);
        tv5.setGravity(Gravity.CENTER);
        tv6.setGravity(Gravity.CENTER);
        tv7.setGravity(Gravity.CENTER);
        tv8.setGravity(Gravity.CENTER);
        tv9.setGravity(Gravity.CENTER);
        tv10.setGravity(Gravity.CENTER);
        tv11.setGravity(Gravity.CENTER);

        tv0.setTextColor(Color.BLACK);
        tv1.setTextColor(Color.BLACK);
        tv2.setTextColor(Color.BLACK);
        tv3.setTextColor(Color.BLACK);
        tv4.setTextColor(Color.BLACK);
        tv5.setTextColor(Color.BLACK);
        tv6.setTextColor(Color.BLACK);
        tv7.setTextColor(Color.BLACK);
        tv8.setTextColor(Color.BLACK);
        tv9.setTextColor(Color.BLACK);
        tv10.setTextColor(Color.BLACK);
        tv11.setTextColor(Color.BLACK);

        tv0.setBackgroundColor(getResources().getColor(R.color.table_header));
        tv1.setBackgroundColor(getResources().getColor(R.color.table_header));
        tv2.setBackgroundColor(getResources().getColor(R.color.table_header));
        tv3.setBackgroundColor(getResources().getColor(R.color.table_header));
        tv4.setBackgroundColor(getResources().getColor(R.color.table_header));
        tv5.setBackgroundColor(getResources().getColor(R.color.table_header));
        tv6.setBackgroundColor(getResources().getColor(R.color.table_header));
        tv7.setBackgroundColor(getResources().getColor(R.color.table_header));
        tv8.setBackgroundColor(getResources().getColor(R.color.table_header));
        tv9.setBackgroundColor(getResources().getColor(R.color.table_header));
        tv10.setBackgroundColor(getResources().getColor(R.color.table_header));
        tv11.setBackgroundColor(getResources().getColor(R.color.table_header));

        tbRow.addView(tv0);
        tbRow.addView(tv1);
        tbRow.addView(tv2);
        tbRow.addView(tv3);
        tbRow.addView(tv4);
        tbRow.addView(tv5);
        tbRow.addView(tv6);
        tbRow.addView(tv7);
        tbRow.addView(tv8);
        tbRow.addView(tv9);
        tbRow.addView(tv10);
        tbRow.addView(tv11);

        table.addView(tbRow);
    }

    private void createTableRowOfCompleteTimetable(String one, String two, String three, String four, String five, String six, String seven, String eight, String nine, String ten, String eleven, String twelve) {
        TableRow tbRow = new TableRow(ScheduleActivity.this);

        TextView tv0 = new TextView(ScheduleActivity.this);
        TextView tv1 = new TextView(ScheduleActivity.this);
        TextView tv2 = new TextView(ScheduleActivity.this);
        TextView tv3 = new TextView(ScheduleActivity.this);
        TextView tv4 = new TextView(ScheduleActivity.this);
        TextView tv5 = new TextView(ScheduleActivity.this);
        TextView tv6 = new TextView(ScheduleActivity.this);
        TextView tv7 = new TextView(ScheduleActivity.this);
        TextView tv8 = new TextView(ScheduleActivity.this);
        TextView tv9 = new TextView(ScheduleActivity.this);
        TextView tv10 = new TextView(ScheduleActivity.this);
        TextView tv11 = new TextView(ScheduleActivity.this);

        tv0.setText(one);
        tv1.setText(two);
        tv2.setText(three);
        tv3.setText(four);
        tv4.setText(five);
        tv5.setText(six);
        tv6.setText(seven);
        tv7.setText(eight);
        tv8.setText(nine);
        tv9.setText(ten);
        tv10.setText(eleven);
        tv11.setText(twelve);

        tv0.setTextSize(16);
        tv1.setTextSize(16);
        tv2.setTextSize(16);
        tv3.setTextSize(16);
        tv4.setTextSize(16);
        tv5.setTextSize(16);
        tv6.setTextSize(16);
        tv7.setTextSize(16);
        tv8.setTextSize(16);
        tv9.setTextSize(16);
        tv10.setTextSize(16);
        tv11.setTextSize(16);

        tv0.setPadding(30, 30, 15, 30);
        tv1.setPadding(30, 30, 15, 30);
        tv2.setPadding(30, 30, 15, 30);
        tv3.setPadding(30, 30, 15, 30);
        tv4.setPadding(30, 30, 15, 30);
        tv5.setPadding(30, 30, 15, 30);
        tv6.setPadding(30, 30, 15, 30);
        tv7.setPadding(30, 30, 15, 30);
        tv8.setPadding(30, 30, 15, 30);
        tv9.setPadding(30, 30, 15, 30);
        tv10.setPadding(30, 30, 15, 30);
        tv11.setPadding(30, 30, 15, 30);

        tv0.setGravity(Gravity.CENTER);
        tv1.setGravity(Gravity.CENTER);
        tv2.setGravity(Gravity.CENTER);
        tv3.setGravity(Gravity.CENTER);
        tv4.setGravity(Gravity.CENTER);
        tv5.setGravity(Gravity.CENTER);
        tv6.setGravity(Gravity.CENTER);
        tv7.setGravity(Gravity.CENTER);
        tv8.setGravity(Gravity.CENTER);
        tv9.setGravity(Gravity.CENTER);
        tv10.setGravity(Gravity.CENTER);
        tv11.setGravity(Gravity.CENTER);

        tv0.setBackgroundResource(R.drawable.borders);
        tv1.setBackgroundResource(R.drawable.borders);
        tv2.setBackgroundResource(R.drawable.borders);
        tv3.setBackgroundResource(R.drawable.borders);
        tv4.setBackgroundResource(R.drawable.borders);
        tv5.setBackgroundResource(R.drawable.borders);
        tv6.setBackgroundResource(R.drawable.borders);
        tv7.setBackgroundResource(R.drawable.borders);
        tv8.setBackgroundResource(R.drawable.borders);
        tv9.setBackgroundResource(R.drawable.borders);
        tv10.setBackgroundResource(R.drawable.borders);
        tv11.setBackgroundResource(R.drawable.borders);

        tv0.setTextColor(Color.BLACK);
        tv1.setTextColor(Color.BLACK);
        tv2.setTextColor(Color.BLACK);
        tv3.setTextColor(Color.BLACK);
        tv4.setTextColor(Color.BLACK);
        tv5.setTextColor(Color.BLACK);
        tv6.setTextColor(Color.BLACK);
        tv7.setTextColor(Color.BLACK);
        tv8.setTextColor(Color.BLACK);
        tv9.setTextColor(Color.BLACK);
        tv10.setTextColor(Color.BLACK);
        tv11.setTextColor(Color.BLACK);

        if (isFirstRow) {
            tv0.setBackgroundColor(getResources().getColor(R.color.white));
            tv1.setBackgroundColor(getResources().getColor(R.color.white));
            tv2.setBackgroundColor(getResources().getColor(R.color.white));
            tv3.setBackgroundColor(getResources().getColor(R.color.white));
            tv4.setBackgroundColor(getResources().getColor(R.color.white));
            tv5.setBackgroundColor(getResources().getColor(R.color.white));
            tv6.setBackgroundColor(getResources().getColor(R.color.white));
            tv7.setBackgroundColor(getResources().getColor(R.color.white));
            tv8.setBackgroundColor(getResources().getColor(R.color.white));
            tv9.setBackgroundColor(getResources().getColor(R.color.white));
            tv10.setBackgroundColor(getResources().getColor(R.color.white));
            tv11.setBackgroundColor(getResources().getColor(R.color.white));
            isFirstRow = false;
        } else {
            tv0.setBackgroundColor(getResources().getColor(R.color.light_gray));
            tv1.setBackgroundColor(getResources().getColor(R.color.light_gray));
            tv2.setBackgroundColor(getResources().getColor(R.color.light_gray));
            tv3.setBackgroundColor(getResources().getColor(R.color.light_gray));
            tv4.setBackgroundColor(getResources().getColor(R.color.light_gray));
            tv5.setBackgroundColor(getResources().getColor(R.color.light_gray));
            tv6.setBackgroundColor(getResources().getColor(R.color.light_gray));
            tv7.setBackgroundColor(getResources().getColor(R.color.light_gray));
            tv8.setBackgroundColor(getResources().getColor(R.color.light_gray));
            tv9.setBackgroundColor(getResources().getColor(R.color.light_gray));
            tv10.setBackgroundColor(getResources().getColor(R.color.light_gray));
            tv11.setBackgroundColor(getResources().getColor(R.color.light_gray));
            isFirstRow = true;
        }

        tbRow.addView(tv0);
        tbRow.addView(tv1);
        tbRow.addView(tv2);
        tbRow.addView(tv3);
        tbRow.addView(tv4);
        tbRow.addView(tv5);
        tbRow.addView(tv6);
        tbRow.addView(tv7);
        tbRow.addView(tv8);
        tbRow.addView(tv9);
        tbRow.addView(tv10);
        tbRow.addView(tv11);

        table.addView(tbRow);
    }

    private void showViewByDialog() {
        SharedPreferences sharedPreferences = getSharedPreferences("viewByPref", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(ScheduleActivity.this);
        alertDialog.setTitle("View schedule of");
        String[] items = {"Today", "All", "A Division", "B Division", "C Division"};
        int checkedItem = -1;
        switch (sharedPreferences.getString("viewBy", "All")) {
            case "Today":
                checkedItem = 0;
                break;
            case "All":
                checkedItem = 1;
                break;
            case "A":
                checkedItem = 2;
                break;
            case "B":
                checkedItem = 3;
                break;
            case "C":
                checkedItem = 4;
                break;
        }
        alertDialog.setSingleChoiceItems(items, checkedItem, (dialog, which) -> {
            switch (which) {
                case 0:
                    editor.putString("viewBy", "Today");
                    break;
                case 1:
                    editor.putString("viewBy", "All");
                    break;
                case 2:
                    editor.putString("viewBy", "A");
                    break;
                case 3:
                    editor.putString("viewBy", "B");
                    break;
                case 4:
                    editor.putString("viewBy", "C");
                    break;
            }
            dialog.dismiss();
            editor.commit();
            startActivity(new Intent(ScheduleActivity.this, ScheduleActivity.class));
        });
        alertDialog.create().show();
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(ScheduleActivity.this, HomeActivity.class));
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}