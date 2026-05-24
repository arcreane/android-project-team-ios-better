package com.example.aroundme.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.aroundme.R;
import com.example.aroundme.ui.main.MainActivity;
import com.example.aroundme.utils.NotificationHelper;

public class NotificationService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();

        NotificationHelper.createChannels(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(
                NotificationHelper.NOTIF_ID_SERVICE,
                buildServiceNotification()
        );

        return START_STICKY;
    }

    private Notification buildServiceNotification() {
        Intent intent = new Intent(this, MainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE
        );

        return new NotificationCompat.Builder(this, NotificationHelper.CHANNEL_SERVICE)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("AroundMe is monitoring events")
                .setContentText("You will be notified before your saved events start.")
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}