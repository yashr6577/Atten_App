package com.suyogbauskar.atten.excelfiles;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.suyogbauskar.atten.R;
import com.suyogbauskar.atten.pojos.Student;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class CreateExcelFileOfAttendance extends Service {

    private String year, completeDivisionName = "", completeBatchName = "";
    private FirebaseUser user;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        user = FirebaseAuth.getInstance().getCurrentUser();
        SharedPreferences sharedPreferences = getSharedPreferences("yearPref", MODE_PRIVATE);
        year = sharedPreferences.getString("year", "");

        createNotificationChannelForAttendance();
        createNotificationChannelForError();
        getAllStudentData();
        getSubjects();

        return super.onStartCommand(intent, flags, startId);
    }

    private void getAllStudentData() {
        SharedPreferences sh = getSharedPreferences("allDataPref", MODE_PRIVATE);
        completeDivisionName = sh.getString("completeDivisionName", "");
        completeBatchName = sh.getString("completeBatchName", "");
    }

    private void getSubjects() {
        FirebaseDatabase.getInstance().getReference("students_data/" + user.getUid() + "/subjects")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dsp : snapshot.getChildren()) {
                            createExcelFiles(dsp.getKey(), dsp.child("subjectShortName").getValue(String.class), completeDivisionName, new XSSFWorkbook(), "Lecture", 0);
                            createExcelFiles(dsp.getKey(), dsp.child("subjectShortName").getValue(String.class), completeBatchName, new XSSFWorkbook(), "Practical", 1);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void createExcelFiles(String subjectCode, String subjectName, String attendanceOf, XSSFWorkbook xssfWorkbook, String period, int notificationValue) {
        final Map<String, Map<String, Map<String, Map<String, Object>>>>[] allMonthsAndChildren = new Map[]{new HashMap<>()};
        Map<String, Map<String, Student>> requiredPresentData = new HashMap<>();
        Map<String, Integer> totalLecturesInMonth = new HashMap<>();
        final int[] totalLectures = new int[1];

        FirebaseDatabase.getInstance().getReference("attendance/" + attendanceOf + "/" + subjectCode + "/" + year)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists()) {
                            sendErrorNotification("No attendance found of " + subjectName + " " + period + " " + year, Integer.parseInt(subjectCode + notificationValue + "1"));
                            return;
                        }
                        int counter = 0;
                        Map<String, Student> tempMap = new HashMap<>();
                        String monthName, firstname = "", lastname = "", dayName = "";
                        int rollNo = 0;
                        Student tempStudent = null;

                        allMonthsAndChildren[0] = (Map<String, Map<String, Map<String, Map<String, Object>>>>) snapshot.getValue();

                        for (Map.Entry<String, Map<String, Map<String, Map<String, Object>>>> entry1 : allMonthsAndChildren[0].entrySet()) {
                            //Month
                            monthName = entry1.getKey();
                            totalLectures[0] = 0;

                            for (Map.Entry<String, Map<String, Map<String, Object>>> entry2 : entry1.getValue().entrySet()) {
                                //Day
                                dayName = entry2.getKey();
                                totalLectures[0]++;

                                for (Map.Entry<String, Map<String, Object>> entry3 : entry2.getValue().entrySet()) {
                                    //UID
                                    if (user.getUid().equals(entry3.getKey())) {
                                        for (Map.Entry<String, Object> entry4 : entry3.getValue().entrySet()) {
                                            if (entry4.getKey().equals("firstname")) {
                                                firstname = entry4.getValue().toString();
                                                counter++;
                                            } else if (entry4.getKey().equals("lastname")) {
                                                lastname = entry4.getValue().toString();
                                                counter++;
                                            } else if (entry4.getKey().equals("rollNo")) {
                                                rollNo = Integer.parseInt(entry4.getValue().toString());
                                                counter++;
                                            }

                                            if (counter % 3 == 0) {
                                                tempStudent = new Student(firstname, lastname, rollNo);
                                            }
                                        }
                                    }
                                }
                                if (tempStudent != null) {
                                    tempMap.put(dayName, tempStudent);
                                }
                            }
                            totalLecturesInMonth.put(monthName, totalLectures[0]);
                            requiredPresentData.put(monthName, tempMap);

                            tempMap = new HashMap<>();
                        }
                        fillStaticData(requiredPresentData, xssfWorkbook, subjectCode, notificationValue);
                        fillAttendance(requiredPresentData, xssfWorkbook);
                        calculatePercentage(requiredPresentData, xssfWorkbook, totalLecturesInMonth);
                        autoSizeAllColumns(xssfWorkbook);
                        writeExcelDataToFile(subjectName, attendanceOf, xssfWorkbook, subjectCode,period, notificationValue);
                        sendNotificationOfExcelFileCreated(subjectName, attendanceOf, Integer.parseInt(subjectCode + notificationValue + "1"));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        sendErrorNotification(error.getMessage(), Integer.parseInt(subjectCode + notificationValue + "2"));
                    }
                });
    }

    private void fillAttendance(Map<String, Map<String, Student>> requiredPresentData, XSSFWorkbook xssfWorkbook) {
        XSSFSheet xssfSheet;
        XSSFRow xssfRow;
        XSSFCell xssfCell;
        int totalColumns;

        for (Map.Entry<String, Map<String, Student>> entry1 : requiredPresentData.entrySet()) {
            //Month
            xssfSheet = xssfWorkbook.getSheet(entry1.getKey());
            totalColumns = xssfSheet.getRow(0).getLastCellNum() - 1;
            for (int i = 2; i < totalColumns; i++) {
                xssfRow = xssfSheet.getRow(1);
                xssfCell = xssfRow.createCell(i);
                xssfCell.setCellValue("P");
            }
        }
    }

    private void calculatePercentage(Map<String, Map<String, Student>> requiredPresentData, XSSFWorkbook xssfWorkbook, Map<String, Integer> totalLecturesInMonth) {
        int totalLectures, studentAttendance;
        float percentage;
        XSSFSheet xssfSheet;
        XSSFRow xssfRow;
        XSSFCell xssfCell;

        for (Map.Entry<String, Map<String, Student>> entry1 : requiredPresentData.entrySet()) {
            //Month

            totalLectures = totalLecturesInMonth.get(entry1.getKey());
            xssfSheet = xssfWorkbook.getSheet(entry1.getKey());
            xssfRow = xssfSheet.getRow(1);
            studentAttendance = xssfSheet.getRow(0).getLastCellNum() - 3;

            percentage = studentAttendance / (float) totalLectures;
            percentage *= 100;

            xssfCell = xssfRow.createCell(xssfSheet.getRow(0).getLastCellNum() - 1);
            xssfCell.setCellValue(percentage);
        }
    }

    private void writeExcelDataToFile(String subjectName, String attendanceOf, XSSFWorkbook xssfWorkbook, String subjectCode, String period, int notificationValue) {
        String filename = subjectName + " " + period + " " + year;

        try {
            File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/Atten");
            dir.mkdir();
            dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/Atten/" + attendanceOf);
            dir.mkdir();

            File filePath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/Atten/" + attendanceOf + "/" + filename + ".xlsx");

            filePath.createNewFile();

            FileOutputStream outputStream = new FileOutputStream(filePath);
            xssfWorkbook.write(outputStream);
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            sendErrorNotification(e.getMessage(), Integer.parseInt(subjectCode + notificationValue + "3"));
        }
    }

    private void fillStaticData(Map<String, Map<String, Student>> requiredPresentData, XSSFWorkbook xssfWorkbook, String subjectCode, int notificationValue) {
        int rowNo, columnNo;
        XSSFSheet xssfSheet;
        XSSFRow xssfRow;
        XSSFCell xssfCell;

        for (Map.Entry<String, Map<String, Student>> entry1 : requiredPresentData.entrySet()) {
            //Month
            rowNo = 0;
            columnNo = 0;

            try {
                xssfSheet = xssfWorkbook.createSheet(entry1.getKey());
            } catch (IllegalArgumentException e) {
                sendErrorNotification(e.getMessage(), Integer.parseInt(subjectCode + notificationValue + "4"));
                return;
            }

            xssfRow = xssfSheet.createRow(rowNo);

            xssfCell = xssfRow.createCell(columnNo);
            xssfCell.setCellValue("Roll No");

            columnNo++;

            xssfCell = xssfRow.createCell(columnNo);
            xssfCell.setCellValue("Name");

            columnNo++;

            List<Float> dayNameFloatList = new ArrayList<>();
            List<String> lastCharacters = new ArrayList<>();

            for (Map.Entry<String, Student> entry2 : entry1.getValue().entrySet()) {
                if (entry2.getKey().charAt(entry2.getKey().length() - 1) == '0') {
                    lastCharacters.add("0");
                } else {
                    lastCharacters.add("");
                }
                dayNameFloatList.add(Float.parseFloat(entry2.getKey().replace("-", ".")));
            }

            Collections.sort(dayNameFloatList);

            String dayNameInStr;
            int i = 0;
            for (Float f : dayNameFloatList) {
                dayNameInStr = f.toString().replace(".", "-");
                dayNameInStr += lastCharacters.get(i);
                i++;
                xssfCell = xssfRow.createCell(columnNo);
                xssfCell.setCellValue(dayNameInStr);

                columnNo++;
            }

            xssfCell = xssfRow.createCell(columnNo);
            xssfCell.setCellValue("Percentage");

            //Filling Student Data
            for (Map.Entry<String, Student> entry2 : entry1.getValue().entrySet()) {
                rowNo++;
                columnNo = 0;

                xssfRow = xssfSheet.createRow(1);

                xssfCell = xssfRow.createCell(columnNo);
                xssfCell.setCellValue(entry2.getValue().getRollNo());
                columnNo++;

                xssfCell = xssfRow.createCell(columnNo);
                xssfCell.setCellValue(entry2.getValue().getFirstname() + " " + entry2.getValue().getLastname());
            }

        }
    }

    private void autoSizeAllColumns(Workbook workbook) {
        int numberOfSheets = workbook.getNumberOfSheets();
        for (int i = 0; i < numberOfSheets; i++) {
            Sheet sheet = workbook.getSheetAt(i);
            if (sheet.getPhysicalNumberOfRows() > 0) {
                Row row = sheet.getRow(sheet.getFirstRowNum());
                Iterator<Cell> cellIterator = row.cellIterator();
                while (cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();
                    int columnIndex = cell.getColumnIndex();
                    if (columnIndex == 0) {
                        sheet.setColumnWidth(columnIndex, 2000);
                    } else if (columnIndex == 1) {
                        sheet.setColumnWidth(columnIndex, 5000);
                    } else {
                        sheet.setColumnWidth(columnIndex, 3000);
                    }
                }
            }
        }
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

    private void sendNotificationOfExcelFileCreated(String subjectName, String attendanceOf, int id) {
        Uri selectedUri = Uri.parse(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/Atten/" + attendanceOf);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(selectedUri, "resource/folder");

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "Attendance")
                .setSmallIcon(R.drawable.raw_logo)
                .setContentText("Excel file of " + subjectName + " " + year + " saved in downloads folder")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Excel file of " + subjectName + " " + year + " saved in downloads folder"))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat.from(this).notify(id, builder.build());
    }

    private void sendErrorNotification(String error, int id) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "Error")
                .setSmallIcon(R.drawable.raw_logo)
                .setContentText(error)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(error))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat.from(this).notify(id, builder.build());
    }
}
