package com.saeefmd.alarmnotificationdemo;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.saeefmd.alarmnotificationdemo.Receiver.AlarmReceiver;
import com.saeefmd.alarmnotificationdemo.Receiver.NotificationReceiver;

import java.util.Calendar;


public class MainActivity extends AppCompatActivity {

    public static String CHANNEL_ID = "com.saeefmd.alarmnotificationdemo";

    private TimePicker alarmTimePicker;
    private TextView alarmTimeTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        alarmTimePicker = findViewById(R.id.alarm_time_picker);
        alarmTimeTv = findViewById(R.id.alarm_time_tv);
        Button setAlarmBt = findViewById(R.id.alarm_set_bt);

        SharedPreferences mSharedPref = getSharedPreferences("alarmnotificationdemo", MODE_PRIVATE);
        boolean scheduledNotificationIsOn = mSharedPref.getBoolean("notificationFlag", false);

        int alarmHour = mSharedPref.getInt("hour", -1);
        int alarmMinute = mSharedPref.getInt("minute", -1);

        if (alarmHour != -1 && alarmMinute != -1) {
            alarmTimeTv.setText("Alarm Time - " + alarmHour + ":" + alarmMinute);
        }

        createNotificationChannel();

        if (!scheduledNotificationIsOn) {
            setScheduledNotification();
        }

        setAlarmBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(System.currentTimeMillis());

                int hour, minute;

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    hour = alarmTimePicker.getHour();
                    minute = alarmTimePicker.getMinute();
                } else {
                    hour = alarmTimePicker.getCurrentHour();
                    minute = alarmTimePicker.getCurrentMinute();
                }

                calendar.setTimeInMillis(System.currentTimeMillis());
                calendar.set(
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH),
                        hour,
                        minute,
                        0
                );

                setAlarm(calendar.getTimeInMillis());

                alarmTimeTv.setText("Alarm Time - " + hour + ":" + minute);

                SharedPreferences.Editor editor = getSharedPreferences("alarmnotificationdemo", MODE_PRIVATE).edit();
                editor.putInt("hour", hour);
                editor.putInt("minute", minute);
                editor.apply();
            }

        });

    }

    private void setAlarm(long timeInMillis) {

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1001, intent, 0);

        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, timeInMillis, AlarmManager.INTERVAL_DAY, pendingIntent);

        Toast.makeText(this, "Alarm Set", Toast.LENGTH_SHORT).show();
    }

    private void setScheduledNotification() {

        AlarmManager notificationAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(this, NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1002, intent, 0);

        // Set the alarm to start at approximately 11:00 a.m.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH),
                11,
                0,
                0
        );

        // With setInexactRepeating(), you have to use one of the AlarmManager interval
        // constants--in this case, AlarmManager.INTERVAL_DAY.
        notificationAlarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_HALF_DAY, pendingIntent);

        SharedPreferences.Editor editor = getSharedPreferences("alarmnotificationdemo", MODE_PRIVATE).edit();
        editor.putBoolean("notificationFlag", true);
        editor.apply();

    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
