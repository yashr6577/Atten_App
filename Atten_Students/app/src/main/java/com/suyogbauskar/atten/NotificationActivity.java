package com.suyogbauskar.atten;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.suyogbauskar.atten.pojos.NotificationData;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class NotificationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        setTitle("Notification");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        showNotification();
    }

    private void showNotification() {
        long currentDate = System.currentTimeMillis();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy/HH/mm/ss/MMMM", Locale.getDefault());
        String dateStr = dateFormat.format(currentDate);
        String[] dateArr = dateStr.split("/");
        int date = Integer.parseInt(dateArr[0]);
        int year = Integer.parseInt(dateArr[2]);
        String monthStr = dateArr[6];

        SharedPreferences sh = getSharedPreferences("allDataPref", MODE_PRIVATE);
        String completeDivisionName = sh.getString("completeDivisionName", "");
        String completeBatchName = sh.getString("completeBatchName", "");
        String studentDepartment = sh.getString("department", "");
        int semester = sh.getInt("semester", 0);

        FirebaseDatabase.getInstance().getReference("notifications")
                .child(String.valueOf(year))
                .child(monthStr)
                .child(String.valueOf(date))
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<NotificationData> allNotifications = new ArrayList<>();
                        DatabaseReference mainRef = snapshot.getRef();
                        mainRef.child(completeDivisionName)
                                .orderByChild("timestamp")
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot dsp: snapshot.getChildren()) {
                                    allNotifications.add(new NotificationData(dsp.child("timestamp").getValue(Long.class), dsp.child("title").getValue(String.class), dsp.child("body").getValue(String.class), dsp.child("time").getValue(String.class)));
                                }
                                mainRef.child(completeBatchName)
                                        .orderByChild("timestamp")
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                for (DataSnapshot dsp: snapshot.getChildren()) {
                                                    allNotifications.add(new NotificationData(dsp.child("timestamp").getValue(Long.class), dsp.child("title").getValue(String.class), dsp.child("body").getValue(String.class), dsp.child("time").getValue(String.class)));
                                                }
                                                mainRef.child(studentDepartment + semester + "-All")
                                                        .orderByChild("timestamp")
                                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                for (DataSnapshot dsp: snapshot.getChildren()) {
                                                                    allNotifications.add(new NotificationData(dsp.child("timestamp").getValue(Long.class), dsp.child("title").getValue(String.class), dsp.child("body").getValue(String.class), dsp.child("time").getValue(String.class)));
                                                                }
                                                                allNotifications.sort(Comparator.comparing(NotificationData::getTimestamp));
                                                                Collections.reverse(allNotifications);

                                                                String[] titleArr = new String[allNotifications.size()];
                                                                String[] bodyArr = new String[allNotifications.size()];
                                                                String[] timesArr = new String[allNotifications.size()];

                                                                for (int i = 0; i < allNotifications.size(); i++) {
                                                                    titleArr[i] = allNotifications.get(i).getTitle();
                                                                    bodyArr[i] = allNotifications.get(i).getBody();
                                                                    timesArr[i] = allNotifications.get(i).getTime();
                                                                }

                                                                ListView notificationList = (ListView) findViewById(R.id.notificationListView);
                                                                NotificationAdapter notificationAdapter = new NotificationAdapter(getApplicationContext(), titleArr, bodyArr, timesArr);
                                                                notificationList.setAdapter(notificationAdapter);
                                                            }

                                                            @Override
                                                            public void onCancelled(@NonNull DatabaseError error) {
                                                                Toast.makeText(NotificationActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {
                                                Toast.makeText(NotificationActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(NotificationActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.d(TAG, error.getMessage());
                    }
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(NotificationActivity.this, HomeActivity.class));
    }
}