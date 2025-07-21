package com.suyogbauskar.atten;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.suyogbauskar.atten.fragments.HomeFragment;
import com.suyogbauskar.atten.fragments.SettingsFragment;
import com.suyogbauskar.atten.pojos.Subject;
import com.suyogbauskar.atten.utils.ProgressDialog;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private boolean fromDialog, isAttendanceRunning;
    private BottomNavigationView bottomNav;
    private String subjectCodeDB, attendanceOf, studentFirstname, studentLastname, studentDivision, studentDepartment, completeDivisionName, completeBatchName;
    private int lectureOrPracticalCount, studentSemester, studentRollNo, studentBatch;
    private long studentEnrollNo;
    private BroadcastReceiver callReceiver;
    private FirebaseUser user;
    private final ProgressDialog progressDialog = new ProgressDialog();
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private NavigationView navigationView;
    private TextView nameView, emailView;
    private ImageView profilePictureImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        init();
    }

    private void init() {
        user = FirebaseAuth.getInstance().getCurrentUser();
        fromDialog = false;

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigation_view);

        setLeftNavigationDrawer();
        requestCallPermission();
        requestStoragePermission();
        progressDialog.show(HomeActivity.this);
        getAllStudentData();
        createNotificationChannelForAttendance();
        createNotificationChannelForError();
        setBottomNav();
    }

    private void setLeftNavigationDrawer() {
        View header = navigationView.getHeaderView(0);
        profilePictureImg = header.findViewById(R.id.profilePicture);
        nameView = header.findViewById(R.id.nameView);
        emailView = header.findViewById(R.id.emailView);

        FirebaseDatabase.getInstance().getReference("students_data/" + user.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        nameView.setText(snapshot.child("firstname").getValue(String.class) + " " + snapshot.child("lastname").getValue(String.class));
                        emailView.setText(user.getEmail());
                        Glide.with(HomeActivity.this).load(user.getPhotoUrl()).into(profilePictureImg);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(HomeActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.start, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void getCurrentAllSubjects() {
        FirebaseDatabase.getInstance().getReference("students_data/" + user.getUid() + "/subjects/")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<String> currentSubjectCodes = new ArrayList<>();
                        for (DataSnapshot dsp : snapshot.getChildren()) {
                            currentSubjectCodes.add(dsp.getKey());
                        }
                        getAllSubjectsFromTeachers(currentSubjectCodes);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(HomeActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void getAllSubjectsFromTeachers(List<String> currentSubjectCodes) {
        FirebaseDatabase.getInstance().getReference("teachers_data")
                .orderByChild("department")
                .equalTo(studentDepartment)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Map<String, Subject> subjectCodesAndNames = new HashMap<>();
                        final long[] counter = {0};
                        long totalTeachers = snapshot.getChildrenCount();

                        for (DataSnapshot dsp : snapshot.getChildren()) {
                            //Get every teacher
                            FirebaseDatabase.getInstance().getReference("teachers_data/" + dsp.getKey() + "/subjects")
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            counter[0]++;
                                            //Get all subjects of that teacher
                                            for (DataSnapshot dsp : snapshot.getChildren()) {
                                                if (dsp.child("semester").getValue(Integer.class) == studentSemester) {
                                                    subjectCodesAndNames.put(dsp.getKey(), new Subject(dsp.child("subject_name").getValue(String.class), dsp.child("subject_short_name").getValue(String.class)));
                                                }
                                            }

                                            if (counter[0] == totalTeachers) {
                                                for (Map.Entry<String, Subject> entry1 : subjectCodesAndNames.entrySet()) {
                                                    currentSubjectCodes.remove(entry1.getKey());

                                                    FirebaseDatabase.getInstance().getReference("students_data/" + user.getUid() + "/subjects/" + entry1.getKey())
                                                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                    if (!snapshot.exists()) {
                                                                        snapshot.getRef().child("isManualSubmitted").setValue(false);
                                                                        snapshot.getRef().child("isMicroProjectSubmitted").setValue(false);
                                                                        snapshot.getRef().child("unitTest1Marks").setValue(-1);
                                                                        snapshot.getRef().child("unitTest2Marks").setValue(-1);
                                                                        snapshot.getRef().child("subjectName").setValue(entry1.getValue().getName());
                                                                        snapshot.getRef().child("subjectShortName").setValue(entry1.getValue().getShortName());
                                                                    }
                                                                }

                                                                @Override
                                                                public void onCancelled(@NonNull DatabaseError error) {
                                                                    Toast.makeText(HomeActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                                                                }
                                                            });
                                                }

                                                for (String code: currentSubjectCodes) {
                                                    FirebaseDatabase.getInstance().getReference("students_data/" + user.getUid() + "/subjects/" + code).removeValue();
                                                }
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            Toast.makeText(HomeActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(HomeActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void getAllStudentData() {
        FirebaseDatabase.getInstance().getReference("students_data/" + user.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        progressDialog.hide();

                        if (!snapshot.exists()) {
                            showDialogOfNotAuthenticated();
                            return;
                        }

                        studentSemester = snapshot.child("semester").getValue(Integer.class);
                        studentFirstname = snapshot.child("firstname").getValue(String.class);
                        studentLastname = snapshot.child("lastname").getValue(String.class);
                        studentDivision = snapshot.child("division").getValue(String.class);
                        studentDepartment = snapshot.child("department").getValue(String.class);
                        studentEnrollNo = snapshot.child("enrollNo").getValue(Long.class);
                        studentRollNo = snapshot.child("rollNo").getValue(Integer.class);
                        studentBatch = snapshot.child("batch").getValue(Integer.class);

                        completeDivisionName = studentDepartment + studentSemester + "-" + studentDivision;
                        completeBatchName = studentDepartment + studentSemester + "-" + studentDivision + studentBatch;

                        SharedPreferences sharedPref = getSharedPreferences("allDataPref", MODE_PRIVATE);
                        SharedPreferences.Editor myEdit = sharedPref.edit();
                        myEdit.putInt("semester", studentSemester);
                        myEdit.putInt("rollNo", studentRollNo);
                        myEdit.putInt("batch", studentBatch);
                        myEdit.putLong("enrollNo", studentEnrollNo);
                        myEdit.putString("firstname", studentFirstname);
                        myEdit.putString("lastname", studentLastname);
                        myEdit.putString("division", studentDivision);
                        myEdit.putString("department", studentDepartment);
                        myEdit.putString("completeDivisionName", completeDivisionName);
                        myEdit.putString("completeBatchName", completeBatchName);
                        myEdit.putBoolean("studentTimetableUpdated", true);
                        myEdit.commit();

                        getCurrentAllSubjects();
                        checkIfAccountIsVerified();
                        setBroadcastReceiverForCall();
                        checkIfAttendanceIsRunning();
                        listenIfAttendanceStarts();
                        fetchNewTimetable();
                        subscribeToTopicsOfNotification();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        progressDialog.hide();
                        Toast.makeText(HomeActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void requestCallPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, 3);
        }
    }

    private void requestStoragePermission() {
        if (Build.VERSION.SDK_INT < 33) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 2);
            }
        }
    }

    private void checkIfAccountIsVerified() {
        FirebaseDatabase.getInstance().getReference("students_data/" + user.getUid() + "/isVerified")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.getValue(Boolean.class)) {
                            fromDialog = true;
                            SweetAlertDialog pDialog = new SweetAlertDialog(HomeActivity.this, SweetAlertDialog.ERROR_TYPE);
                            pDialog.setTitle("Verification needed");
                            pDialog.setContentText("Please verify your details from teacher");
                            pDialog.setConfirmText("Ok");
                            pDialog.setConfirmClickListener(sweetAlertDialog -> {
                                pDialog.dismissWithAnimation();
                                finishAffinity();
                            });
                            pDialog.setCancelable(false);
                            pDialog.show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(HomeActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setBottomNav() {
        bottomNav = findViewById(R.id.bottomNav_view);
        bottomNav.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.home:
                    getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new HomeFragment()).commit();
                    break;

                case R.id.schedule:
                    startActivity(new Intent(HomeActivity.this, ScheduleActivity.class));
                    break;

                case R.id.today:
                    startActivity(new Intent(HomeActivity.this, TodayAttendanceActivity.class));
                    break;
            }
            return true;
        });
        getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new HomeFragment()).commit();
    }

    private void listenIfAttendanceStarts() {
        FirebaseDatabase.getInstance().getReference("/attendance/active_attendance/" + completeDivisionName)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.child("isAttendanceRunning").getValue(Boolean.class)) {
                            attendanceOf = completeDivisionName;
                            ifAttendanceStarts(snapshot);
                        } else {
                            ifAttendanceEnds();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.d(TAG, "Error: " + error.getMessage());
                    }
                });

        FirebaseDatabase.getInstance().getReference("/attendance/active_attendance/" + completeBatchName)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.child("isAttendanceRunning").getValue(Boolean.class)) {
                            attendanceOf = completeBatchName;
                            ifAttendanceStarts(snapshot);
                        } else {
                            ifAttendanceEnds();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.d(TAG, "Error: " + error.getMessage());
                    }
                });

    }

    private void ifAttendanceStarts(DataSnapshot snapshot) {
        enableBottomBar(false);
        isAttendanceRunning = true;
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(false);
        subjectCodeDB = snapshot.child("subject_code").getValue(String.class);
        lectureOrPracticalCount = snapshot.child("count").getValue(Integer.class);
        registerReceiver(callReceiver, new IntentFilter(TelephonyManager.ACTION_PHONE_STATE_CHANGED));

        if (((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE)).getCallState() == TelephonyManager.CALL_STATE_OFFHOOK) {
            //Active call
            removeAttendance("You were mark absent due to phone call");
            finishAffinity();
        }
    }

    private void ifAttendanceEnds() {
        enableBottomBar(true);
        isAttendanceRunning = false;
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        try {
            unregisterReceiver(callReceiver);
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    private void checkIfAttendanceIsRunning() {
        FirebaseDatabase.getInstance().getReference("/attendance/active_attendance/" + completeDivisionName)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.child("isAttendanceRunning").getValue(Boolean.class)) {
                            showDialogOfAttendanceStarted();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(HomeActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        FirebaseDatabase.getInstance().getReference("/attendance/active_attendance/" + completeBatchName)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.child("isAttendanceRunning").getValue(Boolean.class)) {
                            showDialogOfAttendanceStarted();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(HomeActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showDialogOfNotAuthenticated() {
        fromDialog = true;
        SweetAlertDialog pDialog = new SweetAlertDialog(HomeActivity.this, SweetAlertDialog.ERROR_TYPE);
        pDialog.setTitleText("Authentication Failed");
        pDialog.setContentText("Your account doesn't exist, Try contacting your teacher");
        pDialog.setConfirmText("Ok");
        pDialog.setConfirmClickListener(sweetAlertDialog -> {
            pDialog.dismissWithAnimation();
            finishAffinity();
        });
        pDialog.setCancelable(false);
        pDialog.show();
    }

    private void showDialogOfAttendanceStarted() {
        fromDialog = true;
        SweetAlertDialog pDialog = new SweetAlertDialog(HomeActivity.this, SweetAlertDialog.ERROR_TYPE);
        pDialog.setContentText("Can't start app if attendance has started");
        pDialog.setConfirmText("Ok");
        pDialog.setConfirmClickListener(sweetAlertDialog -> {
            pDialog.dismissWithAnimation();
            finishAffinity();
        });
        pDialog.setCancelable(false);
        pDialog.show();
    }

    private void setBroadcastReceiverForCall() {
        callReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);

                if (state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                    //Incoming call answered
                    removeAttendance("You were mark absent due to phone call");
                    finishAffinity();
                }
            }
        };
    }

    private void fetchNewTimetable() {
        FirebaseStorage.getInstance().getReference().child("Students_Timetables/" + studentDepartment + studentSemester + "_Timetable.csv")
                .getBytes(Long.MAX_VALUE)
                .addOnSuccessListener(bytes -> {
                    String filename = studentDepartment + studentSemester + "_Timetable.csv";

                    File file = new File(getApplicationContext().getExternalFilesDir(null), filename);
                    try {
                        file.createNewFile();

                        FileOutputStream outputStream = new FileOutputStream(file);
                        outputStream.write(bytes);
                        outputStream.flush();
                        outputStream.close();

                    } catch (Exception e) {
                        Log.d(TAG, "Error: " + e.getMessage());
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(HomeActivity.this, e.getMessage(), Toast.LENGTH_LONG).show());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 3) {
            if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                sendErrorNotification("Call permission is required to prevent sharing of code through call");
                finishAffinity();
            }
        }
        if (requestCode == 1 || requestCode == 2) {
            if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                sendErrorNotification("Storage permission is required to save excel file");
                finishAffinity();
            }
        }
    }

    @Override
    protected void onPause() {
        try {
            unregisterReceiver(callReceiver);
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
        super.onPause();
    }

    private void removeAttendance(String message) {
        if (fromDialog) {
            return;
        }
        MediaPlayer.create(getApplicationContext(), R.raw.error).start();
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

        sendNotificationOfAbsentMarking(message);
    }

    private void enableBottomBar(boolean enable) {
        for (int i = 0; i < bottomNav.getMenu().size(); i++) {
            bottomNav.getMenu().getItem(i).setEnabled(enable);
        }
    }

    private void subscribeToTopicsOfNotification() {
        FirebaseMessaging.getInstance().subscribeToTopic(studentDepartment + studentSemester + "-All");
        FirebaseMessaging.getInstance().subscribeToTopic("everyone");
        FirebaseMessaging.getInstance().subscribeToTopic(completeDivisionName);
        FirebaseMessaging.getInstance().subscribeToTopic(completeBatchName);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            finishAffinity();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if ((item.getItemId() == R.id.notification) && (!isAttendanceRunning)) {
            startActivity(new Intent(HomeActivity.this, NotificationActivity.class));
        }
        if (toggle.onOptionsItemSelected(item)) {
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.unitTestMarks:
                startActivity(new Intent(HomeActivity.this, UnitTestMarksActivity.class));
                break;

            case R.id.submission:
                startActivity(new Intent(HomeActivity.this, SubmissionActivity.class));
                break;

            case R.id.settings:
                getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new SettingsFragment()).commit();
                break;
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu, menu);
        return true;
    }

    private void createNotificationChannelForAttendance() {
        String name = "Attendance";
        String description = "Attendance Notifications";
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel("Attendance", name, importance);
        channel.setDescription(description);
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    private void createNotificationChannelForError() {
        String name = "Error";
        String description = "Error Notifications";
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel("Error", name, importance);
        channel.setDescription(description);
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    private void sendNotificationOfAbsentMarking(String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "Attendance")
                .setSmallIcon(R.drawable.raw_logo)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(message))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat.from(this).notify(0, builder.build());
    }

    private void sendErrorNotification(String error) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "Error")
                .setSmallIcon(R.drawable.raw_logo)
                .setContentText(error)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(error))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat.from(this).notify(0, builder.build());
    }
}