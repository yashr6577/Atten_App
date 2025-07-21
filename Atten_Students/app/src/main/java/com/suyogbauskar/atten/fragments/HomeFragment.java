package com.suyogbauskar.atten.fragments;

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.suyogbauskar.atten.R;
import com.suyogbauskar.atten.excelfiles.CreateExcelFileOfAttendance;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class HomeFragment extends Fragment implements ViewTreeObserver.OnWindowFocusChangeListener {

    private EditText codeView;
    private Button excelBtn;
    private TextView noteView, statusView, statusOfAttendanceTextView;
    private boolean isAttendanceMarked, listenForWindowFocusChange;
    private int attendanceCodeDB, lectureOrPracticalCount, studentSemester, studentRollNo, studentBatch;
    private String subjectShortNameDB, subjectCodeDB, teacherUIDDB, attendanceOf = "", studentFirstname, studentLastname, studentDivision, completeDivisionName = "", completeBatchName = "";
    private long studentEnrollNo;
    private FirebaseUser user;
    private ValueEventListener divisionListener, batchListener;

    public HomeFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        getActivity().setTitle("Attendance");

        init(view);
        getAllStudentData();
        listenIfAttendanceStarts();

        return view;
    }

    private void init(View view) {
        user = FirebaseAuth.getInstance().getCurrentUser();
        isAttendanceMarked = false;
        findAllViews(view);
        addListeners();
    }

    private void findAllViews(View view) {
        codeView = view.findViewById(R.id.code);
        noteView = view.findViewById(R.id.noteView);
        statusOfAttendanceTextView = view.findViewById(R.id.statusOfAttendanceTextView);
        statusView = view.findViewById(R.id.statusTextView);
        excelBtn = view.findViewById(R.id.excelBtn);
    }

    private void addListeners() {
        codeView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (codeView.getText().toString().length() == 5) {
                    checkAttendanceCode();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        excelBtn.setOnClickListener(view -> createExcelFiles());
    }

    private void getAllStudentData() {
        SharedPreferences sh = getActivity().getSharedPreferences("allDataPref", MODE_PRIVATE);
        studentSemester = sh.getInt("semester", 0);
        studentRollNo = sh.getInt("rollNo", 0);
        studentBatch = sh.getInt("batch", 0);
        studentEnrollNo = sh.getLong("enrollNo", 0);
        studentFirstname = sh.getString("firstname", "");
        studentLastname = sh.getString("lastname", "");
        studentDivision = sh.getString("division", "");
        completeDivisionName = sh.getString("completeDivisionName", "");
        completeBatchName = sh.getString("completeBatchName", "");
    }

    private void createExcelFiles() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
        alertDialog.setTitle("Year");
        String[] items = {"2023", "2024", "2025", "2026", "2027"};
        alertDialog.setSingleChoiceItems(items, -1, (dialog, which) -> {
            SharedPreferences sharedPreferences = getActivity().getSharedPreferences("yearPref", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            switch (which) {
                case 0:
                    editor.putString("year", items[0]);
                    break;
                case 1:
                    editor.putString("year", items[1]);
                    break;
                case 2:
                    editor.putString("year", items[2]);
                    break;
                case 3:
                    editor.putString("year", items[3]);
                    break;
                case 4:
                    editor.putString("year", items[4]);
                    break;
            }
            Toast.makeText(getContext(), "Creating Excel File...", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
            editor.commit();
            getActivity().startService(new Intent(getContext(), CreateExcelFileOfAttendance.class));
        });
        alertDialog.create().show();
    }

    private void ifAttendanceStarts(DataSnapshot snapshot, String message) {
        attendanceCodeDB = snapshot.child("code").getValue(Integer.class);
        subjectShortNameDB = snapshot.child("subject_short_name").getValue(String.class);
        subjectCodeDB = snapshot.child("subject_code").getValue(String.class);
        teacherUIDDB = snapshot.child("uid").getValue(String.class);
        noteView.setVisibility(View.VISIBLE);
        statusView.setText(subjectShortNameDB + " " + message + "\nAttendance started");
        excelBtn.setVisibility(View.GONE);
        listenForWindowFocusChange = true;
        codeView.requestFocus();
        codeView.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                checkIfAttendanceIsRunningAndRemoveAttendance();
            }
        });

    }

    private void ifAttendanceEnds() {
        codeView.setText("");
        statusOfAttendanceTextView.setText("");
        noteView.setVisibility(View.INVISIBLE);
        statusView.setText("");
        isAttendanceMarked = false;
        listenForWindowFocusChange = false;
        excelBtn.setVisibility(View.VISIBLE);
        codeView.setOnFocusChangeListener(null);
    }

    private void listenIfAttendanceStarts() {
        divisionListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child("isAttendanceRunning").getValue(Boolean.class)) {
                    attendanceOf = completeDivisionName;
                    ifAttendanceStarts(snapshot, "Lecture");
                } else {
                    ifAttendanceEnds();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_LONG).show();
            }
        };
        FirebaseDatabase.getInstance().getReference("/attendance/active_attendance/" + completeDivisionName)
                .addValueEventListener(divisionListener);

        batchListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child("isAttendanceRunning").getValue(Boolean.class)) {
                    attendanceOf = completeBatchName;
                    ifAttendanceStarts(snapshot, "Practical");
                } else {
                    ifAttendanceEnds();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_LONG).show();
            }
        };
        FirebaseDatabase.getInstance().getReference("/attendance/active_attendance/" + completeBatchName)
                .addValueEventListener(batchListener);
    }

    private void onSuccessfulAttendance() {
        statusOfAttendanceTextView.setText("Attendance marked");
        statusOfAttendanceTextView.setTextColor(Color.GREEN);

        MediaPlayer.create(getContext(), R.raw.success).start();

        isAttendanceMarked = true;
    }

    private void checkAttendanceCode() {
        try {
            int codeInt = Integer.parseInt(codeView.getText().toString());

            if (attendanceCodeDB == codeInt && !isAttendanceMarked && codeInt != 0) {
                long currentDate = System.currentTimeMillis();
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy/HH/mm/ss/MMMM", Locale.getDefault());
                String dateStr = dateFormat.format(currentDate);
                String[] dateArr = dateStr.split("/");
                int date = Integer.parseInt(dateArr[0]);
                int year = Integer.parseInt(dateArr[2]);
                String monthStr = dateArr[6];

                Map<String, Object> data = new HashMap<>();
                data.put("firstname", studentFirstname);
                data.put("lastname", studentLastname);
                data.put("rollNo", studentRollNo);

                if (attendanceOf.equals(completeDivisionName)) {
                    FirebaseDatabase.getInstance().getReference("teachers_data/" + teacherUIDDB + "/subjects/" + subjectCodeDB + "/" + attendanceOf.substring(attendanceOf.length() - 1) + "_count")
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    lectureOrPracticalCount = snapshot.getValue(Integer.class);
                                    FirebaseDatabase.getInstance().getReference("/attendance/" + completeDivisionName + "/" + subjectCodeDB + "/" + year + "/" + monthStr)
                                            .child(date + "-" + snapshot.getValue(Integer.class))
                                            .child(user.getUid())
                                            .setValue(data).addOnSuccessListener(unused -> onSuccessfulAttendance());
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
                } else if (attendanceOf.equals(completeBatchName)) {
                    FirebaseDatabase.getInstance().getReference("teachers_data/" + teacherUIDDB + "/subjects/" + subjectCodeDB + "/" + attendanceOf.substring(attendanceOf.length() - 2) + "_count")
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    lectureOrPracticalCount = snapshot.getValue(Integer.class);
                                    FirebaseDatabase.getInstance().getReference("/attendance/" + completeBatchName + "/" + subjectCodeDB + "/" + year + "/" + monthStr)
                                            .child(date + "-" + snapshot.getValue(Integer.class))
                                            .child(user.getUid())
                                            .setValue(data).addOnSuccessListener(unused -> onSuccessfulAttendance());
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
                }
            } else if (isAttendanceMarked) {
                statusOfAttendanceTextView.setText("Attendance already marked");
                statusOfAttendanceTextView.setTextColor(Color.RED);

                MediaPlayer.create(getContext(), R.raw.error).start();
            } else {
                statusOfAttendanceTextView.setText("Invalid code");
                statusOfAttendanceTextView.setTextColor(Color.RED);

                MediaPlayer.create(getContext(), R.raw.error).start();
            }
        } catch (NumberFormatException e) {
            statusOfAttendanceTextView.setText("Enter code");
            statusOfAttendanceTextView.setTextColor(Color.RED);

            MediaPlayer.create(getContext(), R.raw.error).start();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (!hasFocus) {
            if (listenForWindowFocusChange) {
                checkIfAttendanceIsRunningAndRemoveAttendance();
            }
        }
    }

    private void checkIfAttendanceIsRunningAndRemoveAttendance() {
        FirebaseDatabase.getInstance().getReference("/attendance/active_attendance/" + completeDivisionName)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.child("isAttendanceRunning").getValue(Boolean.class)) {
                            lectureOrPracticalCount = snapshot.child("count").getValue(Integer.class);
                            removeAttendance("You were marked absent");
                        }
                        if (getActivity() != null) {
                            getActivity().finishAffinity();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });

        FirebaseDatabase.getInstance().getReference("/attendance/active_attendance/" + completeBatchName)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.child("isAttendanceRunning").getValue(Boolean.class)) {
                            lectureOrPracticalCount = snapshot.child("count").getValue(Integer.class);
                            removeAttendance("You were marked absent");
                        }
                        if (getActivity() != null) {
                            getActivity().finishAffinity();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void removeAttendance(String message) {
        if (getActivity() == null) {
            return;
        }
        MediaPlayer.create(getContext(), R.raw.error).start();
        long currentDate = System.currentTimeMillis();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy/HH/mm/ss/MMMM", Locale.getDefault());
        String dateStr = dateFormat.format(currentDate);
        String[] dateArr = dateStr.split("/");
        int date = Integer.parseInt(dateArr[0]);
        int year = Integer.parseInt(dateArr[2]);
        String monthStr = dateArr[6];

        FirebaseDatabase.getInstance().getReference("/attendance/" + attendanceOf + "/" + subjectCodeDB + "/" +
                        year + "/" + monthStr + "/" + date + "-" + lectureOrPracticalCount + "/" + user.getUid())
                .removeValue();

        sendNotificationOfAbsentMarking(message, 81);
    }

    private void sendNotificationOfAbsentMarking(String message, int id) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getContext(), "Attendance")
                .setSmallIcon(R.drawable.raw_logo)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(message))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat.from(getContext()).notify(id, builder.build());
    }

    @Override
    public void onPause() {
        super.onPause();
        if (listenForWindowFocusChange) {
            checkIfAttendanceIsRunningAndRemoveAttendance();
        }
    }

    @Override
    public void onDestroy() {
        if (divisionListener != null) {
            FirebaseDatabase.getInstance().getReference("/attendance/active_attendance/" + completeDivisionName).removeEventListener(divisionListener);
        }
        if (batchListener != null) {
            FirebaseDatabase.getInstance().getReference("/attendance/active_attendance/" + completeBatchName).removeEventListener(batchListener);
        }
        super.onDestroy();
    }
}