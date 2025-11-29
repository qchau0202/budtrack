package vn.edu.tdtu.lhqc.budtrack.controllers.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

/**
 * Central settings handler for the app.
 * Manages all app-related settings including reminder notifications.
 */
public final class SettingsHandler {

    private static final String PREFS_NAME = "app_settings";
    
    // Reminder notification keys
    private static final String KEY_REMINDER_ENABLED = "reminder_enabled";
    private static final String KEY_REMINDER_HOUR = "reminder_hour";
    private static final String KEY_REMINDER_MINUTE = "reminder_minute";
    
    // Default values
    private static final boolean DEFAULT_REMINDER_ENABLED = false;
    private static final int DEFAULT_REMINDER_HOUR = 20; // 8 PM
    private static final int DEFAULT_REMINDER_MINUTE = 0;

    private SettingsHandler() {
    }

    /**
     * Get SharedPreferences instance for app settings
     */
    private static SharedPreferences getPrefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    // ==================== Reminder Notification Settings ====================

    /**
     * Check if reminder notifications are enabled
     */
    public static boolean isReminderEnabled(Context context) {
        return getPrefs(context).getBoolean(KEY_REMINDER_ENABLED, DEFAULT_REMINDER_ENABLED);
    }

    /**
     * Set reminder notification enabled state
     */
    public static void setReminderEnabled(Context context, boolean enabled) {
        getPrefs(context).edit().putBoolean(KEY_REMINDER_ENABLED, enabled).apply();
    }

    /**
     * Get reminder hour (0-23)
     */
    public static int getReminderHour(Context context) {
        return getPrefs(context).getInt(KEY_REMINDER_HOUR, DEFAULT_REMINDER_HOUR);
    }

    /**
     * Set reminder hour (0-23)
     */
    public static void setReminderHour(Context context, int hour) {
        if (hour < 0 || hour > 23) {
            hour = DEFAULT_REMINDER_HOUR;
        }
        getPrefs(context).edit().putInt(KEY_REMINDER_HOUR, hour).apply();
    }

    /**
     * Get reminder minute (0-59)
     */
    public static int getReminderMinute(Context context) {
        return getPrefs(context).getInt(KEY_REMINDER_MINUTE, DEFAULT_REMINDER_MINUTE);
    }

    /**
     * Set reminder minute (0-59)
     */
    public static void setReminderMinute(Context context, int minute) {
        if (minute < 0 || minute > 59) {
            minute = DEFAULT_REMINDER_MINUTE;
        }
        getPrefs(context).edit().putInt(KEY_REMINDER_MINUTE, minute).apply();
    }

    /**
     * Set reminder time (hour and minute)
     */
    public static void setReminderTime(Context context, int hour, int minute) {
        setReminderHour(context, hour);
        setReminderMinute(context, minute);
    }

    /**
     * Format reminder time as a display string in 24-hour format
     */
    public static String formatReminderTime(Context context) {
        int hour = getReminderHour(context);
        int minute = getReminderMinute(context);
        
        return String.format("%02d:%02d", hour, minute);
    }

    // ==================== Notification Permission ====================

    /**
     * Check if notification permission is granted
     */
    public static boolean isNotificationPermissionGranted(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(
                    context.getApplicationContext(),
                    android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED;
        } else {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            return notificationManager.areNotificationsEnabled();
        }
    }
}

