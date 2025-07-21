package com.suyogbauskar.atten;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SubmissionActivity extends AppCompatActivity {

    private FirebaseUser user;
    private TableLayout table;
    private boolean isFirstRow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submission);
        setTitle("Submission");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        user = FirebaseAuth.getInstance().getCurrentUser();
        table = findViewById(R.id.table);

        mainCode();
    }

    private void mainCode() {
        FirebaseDatabase.getInstance().getReference("students_data/" + user.getUid() + "/subjects/")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        boolean isManualSubmitted, isMicroProjectSubmitted;
                        String subjectShortName;
                        isFirstRow = true;
                        table.removeAllViews();
                        drawTableHeader();

                        for (DataSnapshot dsp: snapshot.getChildren()) {
                            isManualSubmitted = dsp.child("isManualSubmitted").getValue(Boolean.class);
                            isMicroProjectSubmitted = dsp.child("isMicroProjectSubmitted").getValue(Boolean.class);
                            subjectShortName = dsp.child("subjectShortName").getValue(String.class);

                            if (isManualSubmitted && isMicroProjectSubmitted) {
                                createTableRow(subjectShortName, "✅", "✅");
                            } else if (isManualSubmitted) {
                                createTableRow(subjectShortName, "✅", "❌");
                            } else if (isMicroProjectSubmitted) {
                                createTableRow(subjectShortName, "❌", "✅");
                            } else {
                                createTableRow(subjectShortName, "❌", "❌");
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.d(TAG, error.getMessage());
                    }
                });
    }

    private void drawTableHeader() {
        TableRow tbRow = new TableRow(SubmissionActivity.this);

        TextView tv0 = new TextView(SubmissionActivity.this);
        TextView tv1 = new TextView(SubmissionActivity.this);
        TextView tv2 = new TextView(SubmissionActivity.this);

        tv0.setText("Subject");
        tv1.setText("Manual");
        tv2.setText("Micro Project");

        tv0.setTypeface(Typeface.DEFAULT_BOLD);
        tv1.setTypeface(Typeface.DEFAULT_BOLD);
        tv2.setTypeface(Typeface.DEFAULT_BOLD);

        tv0.setTextSize(18);
        tv1.setTextSize(18);
        tv2.setTextSize(18);

        tv0.setPadding(30, 30, 15, 30);
        tv1.setPadding(30, 30, 15, 30);
        tv2.setPadding(30, 30, 15, 30);

        tv0.setGravity(Gravity.CENTER);
        tv1.setGravity(Gravity.CENTER);
        tv2.setGravity(Gravity.CENTER);

        tv0.setTextColor(Color.BLACK);
        tv1.setTextColor(Color.BLACK);
        tv2.setTextColor(Color.BLACK);

        tv0.setBackgroundColor(getResources().getColor(R.color.table_header));
        tv1.setBackgroundColor(getResources().getColor(R.color.table_header));
        tv2.setBackgroundColor(getResources().getColor(R.color.table_header));

        tbRow.addView(tv0);
        tbRow.addView(tv1);
        tbRow.addView(tv2);

        table.addView(tbRow);
    }

    private void createTableRow(String subjectName, String manual, String microProject) {
        TableRow tbRow = new TableRow(SubmissionActivity.this);

        TextView tv0 = new TextView(SubmissionActivity.this);
        TextView tv1 = new TextView(SubmissionActivity.this);
        TextView tv2 = new TextView(SubmissionActivity.this);

        tv0.setText(subjectName);
        tv1.setText(manual);
        tv2.setText(microProject);

        tv0.setTextSize(16);
        tv1.setTextSize(16);
        tv2.setTextSize(16);

        tv0.setPadding(30, 30, 15, 30);
        tv1.setPadding(30, 30, 15, 30);
        tv2.setPadding(30, 30, 15, 30);

        tv0.setGravity(Gravity.CENTER);
        tv1.setGravity(Gravity.CENTER);
        tv2.setGravity(Gravity.CENTER);

        tv0.setBackgroundResource(R.drawable.borders);
        tv1.setBackgroundResource(R.drawable.borders);
        tv2.setBackgroundResource(R.drawable.borders);

        tv0.setTextColor(Color.BLACK);
        tv1.setTextColor(Color.BLACK);
        tv2.setTextColor(Color.BLACK);

        if (isFirstRow) {
            tv0.setBackgroundColor(getResources().getColor(R.color.white));
            tv1.setBackgroundColor(getResources().getColor(R.color.white));
            tv2.setBackgroundColor(getResources().getColor(R.color.white));
            isFirstRow = false;
        } else {
            tv0.setBackgroundColor(getResources().getColor(R.color.light_gray));
            tv1.setBackgroundColor(getResources().getColor(R.color.light_gray));
            tv2.setBackgroundColor(getResources().getColor(R.color.light_gray));
            isFirstRow = true;
        }

        tbRow.addView(tv0);
        tbRow.addView(tv1);
        tbRow.addView(tv2);

        table.addView(tbRow);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(SubmissionActivity.this, HomeActivity.class));
    }
}