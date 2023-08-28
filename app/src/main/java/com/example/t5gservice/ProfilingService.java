package com.example.t5gservice;

import static java.lang.Boolean.TRUE;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class ProfilingService extends Service {

    int value = 0;
    private Timer timer;

    TaskProfiler tp = new TaskProfiler();
    Profiler pf = new Profiler();

    Process p = null;
    String foreground_pid=null;
    String[] process_info;
    String[][] foreground_thread_info;
    boolean csv_save=TRUE;
    File thread_csv_file = null;
    String battery_current_avg = null;
    String battery_voltage_now = null;
    String battery_power_avg = null;
    Profiler profiler = new Profiler();


    public ProfilingService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("tftf", "Start  profiling service");


        final String strId = "[t5g] ProfilingService running";
        final String strTitle = getString(R.string.app_name);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = notificationManager.getNotificationChannel(strId);
        if (channel == null) {
            channel = new NotificationChannel(strId, strTitle, NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }
        Notification notification = new NotificationCompat.Builder(this, strId).build();
        startForeground(1, notification);


        pf.start();

        timer = new Timer();
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String threadinfo_fileName = "threadinfodata_" + timeStamp + ".csv";

        thread_csv_file = new File(getFilesDir(), threadinfo_fileName);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try{
                    process_info=tp.getprocessinfo(p, "apprtc");
                    battery_current_avg = String.valueOf(profiler.getBatteryCurrent());
                    battery_power_avg = String.valueOf(profiler.getBatteryVoltage()*profiler.getBatteryCurrent()/1000);



                    if (process_info.length != 0 && (process_info[9].equals("/top-app") || process_info[9].equals("/foreground") || process_info[9].equals("/system"))) {
                        foreground_pid = process_info[2];
                        foreground_thread_info = tp.executeTopShellCommand(p, foreground_pid);
                        if (csv_save==TRUE) {
                            tp.tid_writeCSV(foreground_thread_info, thread_csv_file);
                        }

                    }
                }catch (NullPointerException e) {
                    e.printStackTrace();
                }

            }
        }, 0, 1000); // 1초마다 실행
    }




    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
        tp.tid_header_writeCSV(thread_csv_file);
        pf.stop();
        if (timer != null) {
            timer.cancel();
        }

        Log.i("tftf", "stop profiling service");

    }

}