package com.tanaka42.webremotevolumecontrol;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;


public class ForegroundService extends Service {
    public ForegroundService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        System.out.println("Starting service ...");

        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        HttpServer httpServer = new HttpServer(audioManager, getApplicationContext());
        httpServer.start();

        String channelId = getString(R.string.app_name);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
        Intent notificationIntent = new Intent(this, ForegroundService.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        Notification notification;

        createNotificationChannel(channelId);

        notification = new NotificationCompat.Builder(this, channelId)
                .setContentIntent(pendingIntent)
                .setContentTitle("Content Title")
                .setContentText("Content Text")
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();

        startForeground(42, notification);
        System.out.println("Service started.");
    }

    private void createNotificationChannel(String channelId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, getString(R.string.running_indicator), NotificationManager.IMPORTANCE_MIN);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        System.out.println("Stopping Service");
        HttpServer.stopServer();
    }
}
