package com.thevelvetalgo.screenwake;

import android.content.SharedPreferences;
import android.os.PowerManager;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

public class NotificationWakeService extends NotificationListenerService {

    private static final long WAKE_DURATION_MS = 3000;

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);

        // Don't wake the screen for our own notifications.
        if (sbn.getPackageName().equals(getPackageName())) {
            return;
        }

        SharedPreferences prefs = getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE);
        if (!prefs.getBoolean(MainActivity.ENABLED_KEY, true)) {
            return;
        }

        wakeScreen();
    }

    @SuppressWarnings("deprecation")
    private void wakeScreen() {
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        if (powerManager == null) {
            return;
        }

        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP,
                "ScreenWake:NotificationWakeLock");
        wakeLock.acquire(WAKE_DURATION_MS);
    }
}
