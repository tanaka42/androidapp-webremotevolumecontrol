package com.tanaka42.webremotevolumecontrol;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
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
        //System.out.println("Starting service ...");

        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        HttpServer httpServer = new HttpServer(audioManager, getApplicationContext());
        httpServer.start();

        String channelId = getString(R.string.app_name);

        createNotificationChannel(channelId);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
        Notification notification;

        Intent notificationIntent = new Intent(getApplicationContext(), StartupActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 1, notificationIntent, 0);

        notification = new NotificationCompat.Builder(this, channelId)
                .setOngoing(true)
                .setContentTitle(getString(R.string.ongoing_notification_title))
                .setContentText(getString(R.string.ongoing_notification_text))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(42, notification);
        //System.out.println("Service started.");
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
        //System.out.println("Stopping Service...");
        HttpServer.stopServer();
        //System.out.println("Service stopped");
    }
}
