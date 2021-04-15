/*
 * Developed by Mqondisi Ndlovu Copyright (c) 2021. contact @ 0772783880
 */

package com.axis.revmaxinterface;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import java.util.Calendar;

public class Watchfi extends Service {

    private PendingIntent pedint;
    String err;
    private int NOTIFICATION_ID = 1;

    public Watchfi() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void onCreate() {
        super.onCreate();
        startService();
    }

    public void startService() {

        NotificationManager mnotifi = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        int icon = R.drawable.icon;
        Intent notifiintent = new Intent(this, DoWork.class);
        Context cont = getApplicationContext();
        PendingIntent contentIntent = PendingIntent.getActivity(cont, 0, notifiintent, 0);


        Intent myIntent = new Intent(Watchfi.this, DoWork.class);
        pedint = PendingIntent.getService(Watchfi.this, 0, myIntent, 0);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.SECOND, 2);
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pedint);

        err = "Polling";
        NotificationCompat.Builder notifil = new
                NotificationCompat.Builder(this)
                .setContentTitle(MainActivity.serial + " Status")
                .setContentText(err)
                .setTicker("Interface Alert")
                .setAutoCancel(true)
                .setSmallIcon(icon)
                .setContentIntent(contentIntent);
        Notification arrival = notifil.build();
        mnotifi.notify(NOTIFICATION_ID, arrival);

        stopService(new Intent(getBaseContext(), Watchfi.class));

    }
}