package com.example.aroundme.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.aroundme.R;
import com.example.aroundme.data.local.AppDatabase;
import com.example.aroundme.data.local.EventDao;
import com.example.aroundme.model.Event;
import com.example.aroundme.ui.main.MainActivity;
import com.example.aroundme.utils.NotificationHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class NotificationService extends Service {

    private static final long CHECK_INTERVAL_MS = 60_000;
    private static final long ALERT_THRESHOLD_MINUTES = 30;

    public static void startService(android.content.Context context, Event event) {
        Intent intent = new Intent(context, NotificationService.class);
        intent.putExtra("event_id", event.getId());
        intent.putExtra("event_name", event.getName());
        context.startForegroundService(intent);
    }

    private Handler handler;
    private Executor executor;
    private EventDao eventDao;

    private final Runnable checkEventsRunnable = new Runnable() {
        @Override
        public void run() {
            checkFavoriteEvents();
            handler.postDelayed(this, CHECK_INTERVAL_MS);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        handler = new Handler(Looper.getMainLooper());
        executor = Executors.newSingleThreadExecutor();
        eventDao = AppDatabase.getInstance(this).eventDao();

        NotificationHelper.createChannels(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(
                NotificationHelper.NOTIF_ID_SERVICE,
                buildServiceNotification()
        );

        handler.removeCallbacks(checkEventsRunnable);
        handler.post(checkEventsRunnable);

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

    private void checkFavoriteEvents() {
        executor.execute(() -> {
            List<Event> favorites = eventDao.getAllFavoritesNow();

            if (favorites == null || favorites.isEmpty()) {
                stopSelf();
                return;
            }

            boolean hasUpcomingEvent = false;

            for (Event event : favorites) {
                long minutes = minutesUntilEvent(event);

                if (minutes > 0) {
                    hasUpcomingEvent = true;
                }

                if (minutes >= 0 && minutes <= ALERT_THRESHOLD_MINUTES) {
                    sendEventAlert(event, minutes);
                }
            }

            if (!hasUpcomingEvent) {
                stopSelf();
            }
        });
    }

    private void sendEventAlert(Event event, long minutes) {
        Intent intent = new Intent(this, MainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                event.getId().hashCode(),
                intent,
                PendingIntent.FLAG_IMMUTABLE
        );

        Notification notification = new NotificationCompat.Builder(this, NotificationHelper.CHANNEL_ALERTS)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Event starts soon")
                .setContentText(event.getName() + " starts in " + minutes + " minutes.")
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build();

        int notificationId =
                NotificationHelper.NOTIF_ID_ALERT_BASE + Math.abs(event.getId().hashCode());

        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.TIRAMISU ||
                getApplicationContext().checkSelfPermission(
                        android.Manifest.permission.POST_NOTIFICATIONS)
                        == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            NotificationManagerCompat.from(this).notify(notificationId, notification);
        }

    }

    public static long minutesUntilEvent(Event event) {
        if (event == null || event.getDate() == null) {
            return -1;
        }

        String dateTimeString = event.getDate();

        if (event.getTime() != null && !event.getTime().isEmpty()) {
            dateTimeString += " " + event.getTime();
        } else {
            dateTimeString += " 00:00:00";
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

        try {
            Date eventDate = sdf.parse(dateTimeString);

            if (eventDate == null) {
                return -1;
            }

            long diffMs = eventDate.getTime() - System.currentTimeMillis();
            return TimeUnit.MILLISECONDS.toMinutes(diffMs);

        } catch (Exception e) {
            return -1;
        }
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacks(checkEventsRunnable);
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}