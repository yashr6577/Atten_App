package com.suyogbauskar.atten;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.suyogbauskar.atten.utils.ProgressDialog;

import java.util.HashMap;
import java.util.Map;

public class FillDetailsActivity extends AppCompatActivity {

    private EditText firstname, lastname, enrollNo, rollNo, semester, batch, division, department;
    private Button submitBtn;
    private String firstnameStr, lastnameStr, enrollNoStr, semesterStr = "", batchStr = "", divisionStr = "", departmentStr = "";
    private long enrollNoLong;
    private int rollNoInt;
    private boolean hasSubmitButtonPressed;
    private final ProgressDialog progressDialog = new ProgressDialog();
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fill_details);

        init();
    }

    private void init() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        user = FirebaseAuth.getInstance().getCurrentUser();
        findAllViews();
        hasSubmitButtonPressed = false;
        addListeners();
    }

    private void findAllViews() {
        firstname = findViewById(R.id.firstname);
        lastname = findViewById(R.id.lastname);
        enrollNo = findViewById(R.id.enrollNo);
        rollNo = findViewById(R.id.rollNo);
        submitBtn = findViewById(R.id.submitBtn);
        semester = findViewById(R.id.semester);
        batch = findViewById(R.id.batch);
        division = findViewById(R.id.division);
        department = findViewById(R.id.department);
    }

    private void addListeners() {
        semester.setOnClickListener(v -> {
            AlertDialog.Builder semesterDialog = new AlertDialog.Builder(FillDetailsActivity.this);
            semesterDialog.setTitle("Semester");
            String[] items = {"Semester 1", "Semester 2", "Semester 3", "Semester 4", "Semester 5", "Semester 6"};
            semesterDialog.setSingleChoiceItems(items, -1, (dialog, which) -> {
                semesterStr = items[which];
                semester.setText(semesterStr);
                semesterStr = semesterStr.charAt(semesterStr.length() - 1) + "";
                dialog.dismiss();
            });
            semesterDialog.create().show();
        });

        division.setOnClickListener(v -> {
            AlertDialog.Builder divisionDialog = new AlertDialog.Builder(FillDetailsActivity.this);
            divisionDialog.setTitle("Division");
            String[] items = {"Division A", "Division B", "Division C"};
            divisionDialog.setSingleChoiceItems(items, -1, (dialog, which) -> {
                divisionStr = items[which];
                division.setText(divisionStr);
                divisionStr = divisionStr.charAt(divisionStr.length() - 1) + "";
                dialog.dismiss();
            });
            divisionDialog.create().show();
        });

        batch.setOnClickListener(v -> {
            AlertDialog.Builder batchDialog = new AlertDialog.Builder(FillDetailsActivity.this);
            batchDialog.setTitle("Batch");
            String[] items = {"Batch 1", "Batch 2", "Batch 3"};
            batchDialog.setSingleChoiceItems(items, -1, (dialog, which) -> {
                batchStr = items[which];
                batch.setText(batchStr);
                batchStr = batchStr.charAt(batchStr.length() - 1) + "";
                dialog.dismiss();
            });
            batchDialog.create().show();
        });

        department.setOnClickListener(v -> {
            AlertDialog.Builder departmentDialog = new AlertDialog.Builder(FillDetailsActivity.this);
            departmentDialog.setTitle("Department");
            String[] items = {"Department CE", "Department CO", "Department IF", "Department EE", "Department EJ", "Department ME"};
            departmentDialog.setSingleChoiceItems(items, -1, (dialog, which) -> {
                departmentStr = items[which];
                department.setText(departmentStr);
                departmentStr = departmentStr.substring(departmentStr.length() - 2);
                dialog.dismiss();
            });
            departmentDialog.create().show();
        });

        submitBtn.setOnClickListener(view -> {
            firstnameStr = firstname.getText().toString().trim().toLowerCase();
            lastnameStr = lastname.getText().toString().trim().toLowerCase();
            enrollNoStr = enrollNo.getText().toString().trim();

            if (firstnameStr.isEmpty()) {
                Toast.makeText(getApplicationContext(), "Enter Firstname", Toast.LENGTH_LONG).show();
            } else if (lastnameStr.isEmpty()) {
                Toast.makeText(getApplicationContext(), "Enter Lastname", Toast.LENGTH_LONG).show();
            } else if (departmentStr.equals("")) {
                Toast.makeText(getApplicationContext(), "Select Department", Toast.LENGTH_LONG).show();
            } else if (semesterStr.equals("")) {
                Toast.makeText(getApplicationContext(), "Select Semester", Toast.LENGTH_LONG).show();
            } else if (divisionStr.equals("")) {
                Toast.makeText(getApplicationContext(), "Select Division", Toast.LENGTH_LONG).show();
            } else if (batchStr.equals("")) {
                Toast.makeText(getApplicationContext(), "Select Batch", Toast.LENGTH_LONG).show();
            } else if ((enrollNoStr.length() != 10) || (Long.parseLong(enrollNoStr) == 0)) {
                Toast.makeText(getApplicationContext(), "Invalid Enrollment No", Toast.LENGTH_LONG).show();
            } else if (rollNo.getText().toString().trim().isEmpty()) {
                Toast.makeText(getApplicationContext(), "Invalid Roll No", Toast.LENGTH_LONG).show();
            } else if (rollNo.getText().toString().length() >= 4 || rollNo.getText().toString().equals("0")) {
                Toast.makeText(getApplicationContext(), "Invalid Roll No", Toast.LENGTH_LONG).show();
            } else {
                progressDialog.show(FillDetailsActivity.this);
                hasSubmitButtonPressed = true;
                submitBtn.setClickable(false);
                rollNoInt = Integer.parseInt(rollNo.getText().toString());
                enrollNoLong = Long.parseLong(enrollNoStr);

                firstnameStr = firstnameStr.substring(0, 1).toUpperCase() + firstnameStr.substring(1);
                lastnameStr = lastnameStr.substring(0, 1).toUpperCase() + lastnameStr.substring(1);

                FirebaseDatabase.getInstance().getReference("students_data")
                        .orderByChild("enrollNo")
                        .equalTo(enrollNoLong)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.getChildrenCount() > 0) {
                                    Toast.makeText(FillDetailsActivity.this, "Enrollment no. already exists", Toast.LENGTH_LONG).show();
                                    progressDialog.hide();
                                    hasSubmitButtonPressed = false;
                                    submitBtn.setClickable(true);
                                } else {
                                    int batchInt = Integer.parseInt(batchStr);

                                    Map<String, Object> data = new HashMap<>();
                                    data.put("firstname", firstnameStr);
                                    data.put("lastname", lastnameStr);
                                    data.put("enrollNo", enrollNoLong);
                                    data.put("rollNo", rollNoInt);
                                    data.put("isVerified", false);
                                    data.put("semester", Integer.parseInt(semesterStr));
                                    data.put("division", divisionStr);
                                    data.put("batch", batchInt);
                                    data.put("department", departmentStr);
                                    data.put("queryStringSemester", departmentStr + semesterStr);
                                    data.put("queryStringDivision", departmentStr + semesterStr + divisionStr);
                                    data.put("queryStringIsVerified", departmentStr + "false");
                                    data.put("queryStringRollNo", departmentStr + semesterStr + divisionStr + rollNoInt);

                                    SharedPreferences sharedPref = getSharedPreferences("allDataPref", MODE_PRIVATE);
                                    SharedPreferences.Editor myEdit = sharedPref.edit();
                                    myEdit.putInt("semester", Integer.parseInt(semesterStr));
                                    myEdit.putInt("rollNo", rollNoInt);
                                    myEdit.putInt("batch", Integer.parseInt(batchStr));
                                    myEdit.putLong("enrollNo", enrollNoLong);
                                    myEdit.putString("firstname", firstnameStr);
                                    myEdit.putString("lastname", lastnameStr);
                                    myEdit.putString("division", divisionStr);
                                    myEdit.putString("department", departmentStr);
                                    myEdit.putString("completeDivisionName", departmentStr + Integer.parseInt(semesterStr) + "-" + divisionStr);
                                    myEdit.putString("completeBatchName", departmentStr + Integer.parseInt(semesterStr) + "-" + divisionStr + batchInt);
                                    myEdit.putBoolean("studentTimetableUpdated", true);
                                    myEdit.commit();

                                    FirebaseDatabase.getInstance().getReference("students_data").child(user.getUid())
                                            .setValue(data)
                                            .addOnSuccessListener(unused -> {
                                                progressDialog.hide();
                                                startActivity(new Intent(FillDetailsActivity.this, HomeActivity.class));
                                            })
                                            .addOnFailureListener(e -> {
                                                progressDialog.hide();
                                                Toast.makeText(FillDetailsActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                                                startActivity(new Intent(FillDetailsActivity.this, MainActivity.class));
                                                user.delete();
                                            });
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(FillDetailsActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });

            }
        });
    }

    @Override
    public void onBackPressed() {
        FirebaseAuth.getInstance().signOut();
        user.delete();
        startActivity(new Intent(FillDetailsActivity.this, MainActivity.class));
    }

    @Override
    protected void onPause() {
        if (!hasSubmitButtonPressed) {
            FirebaseAuth.getInstance().signOut();
            user.delete();
        }
        super.onPause();
    }
}