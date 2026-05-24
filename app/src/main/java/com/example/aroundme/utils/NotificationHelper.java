package com.example.aroundme.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

public class NotificationHelper {

    public static final String CHANNEL_SERVICE = "aroundme_service";
    public static final String CHANNEL_ALERTS = "aroundme_alerts";

    public static final int NOTIF_ID_SERVICE = 1;
    public static final int NOTIF_ID_ALERT_BASE = 1000;

    public static void createChannels(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }

        NotificationManager manager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (manager == null) {
            return;
        }

        NotificationChannel serviceChannel = new NotificationChannel(
                CHANNEL_SERVICE,
                "AroundMe Service",
                NotificationManager.IMPORTANCE_LOW
        );
        serviceChannel.setDescription("Shows that AroundMe is monitoring saved events.");

        NotificationChannel alertChannel = new NotificationChannel(
                CHANNEL_ALERTS,
                "Event Alerts",
                NotificationManager.IMPORTANCE_HIGH
        );
        alertChannel.setDescription("Notifies users before saved events start.");

        manager.createNotificationChannel(serviceChannel);
        manager.createNotificationChannel(alertChannel);
    }
}