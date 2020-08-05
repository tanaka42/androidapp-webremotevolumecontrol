package com.tanaka42.webremotesoundcontrol;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.IBinder;

import com.tanaka42.webremotesoundcontrol.R;

public class WebRemoteSoundControlService extends Service {
    public WebRemoteSoundControlService() {
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

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel chan = new NotificationChannel(channelId, "Indicateur de fonctionnement", NotificationManager.IMPORTANCE_MIN);
        chan.setDescription("Indicateur de fonctionnement");
        chan.setSound(null, null);

        notificationManager.createNotificationChannel(chan);

        Intent notificationIntent = new Intent(this, WebRemoteSoundControlService.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        Notification notification = new Notification.Builder(this, channelId)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(42, notification);

        System.out.println("Service started.");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        System.out.println("Stopping Service");
        HttpServer.stopServer();
    }
}
