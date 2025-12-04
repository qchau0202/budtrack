package vn.edu.tdtu.lhqc.budtrack.controllers.settings;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import java.util.Calendar;

import vn.edu.tdtu.lhqc.budtrack.controllers.exchangerate.ExchangeRateUpdateReceiver;

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
    
    // Currency keys
    public static final String KEY_CURRENCY = "currency";
    private static final String KEY_EXCHANGE_RATE = "exchange_rate";
    private static final String KEY_EXCHANGE_RATE_LAST_UPDATE = "exchange_rate_last_update";
    private static final String KEY_EXCHANGE_RATE_NEXT_UPDATE = "exchange_rate_next_update";
    
    // Exchange rate update receiver
    private static final int EXCHANGE_RATE_UPDATE_REQUEST_CODE = 3001;
    
    // Backup sync keys
    private static final String KEY_BACKUP_LAST_SYNC = "backup_last_sync";
    
    // Default values
    private static final boolean DEFAULT_REMINDER_ENABLED = false;
    private static final int DEFAULT_REMINDER_HOUR = 20; // 8 PM
    private static final int DEFAULT_REMINDER_MINUTE = 0;
    private static final String DEFAULT_CURRENCY = "VND";
    private static final float DEFAULT_EXCHANGE_RATE = 1.0f; // 1 USD = 1 VND (placeholder, should be actual rate)

    private SettingsHandler() {
    }

    /**
     * Get SharedPreferences instance for app settings
     */
    public static SharedPreferences getPrefs(Context context) {
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

    // ==================== Currency Settings ====================

    /**
     * Get current currency (VND or USD)
     */
    public static String getCurrency(Context context) {
        return getPrefs(context).getString(KEY_CURRENCY, DEFAULT_CURRENCY);
    }

    /**
     * Set currency (VND or USD)
     */
    public static void setCurrency(Context context, String currency) {
        if (currency == null || (!currency.equals("VND") && !currency.equals("USD"))) {
            currency = DEFAULT_CURRENCY;
        }
        // Use commit() instead of apply() for immediate synchronous save
        // This ensures currency change is immediately available and triggers preference change listeners
        getPrefs(context).edit().putString(KEY_CURRENCY, currency).commit();
    }

    /**
     * Get exchange rate (USD to VND)
     */
    public static float getExchangeRate(Context context) {
        return getPrefs(context).getFloat(KEY_EXCHANGE_RATE, DEFAULT_EXCHANGE_RATE);
    }

    /**
     * Set exchange rate (USD to VND)
     */
    public static void setExchangeRate(Context context, float rate) {
        if (rate <= 0) {
            rate = DEFAULT_EXCHANGE_RATE;
        }
        getPrefs(context).edit().putFloat(KEY_EXCHANGE_RATE, rate).apply();
        // Update last update timestamp
        getPrefs(context).edit().putLong(KEY_EXCHANGE_RATE_LAST_UPDATE, System.currentTimeMillis()).apply();
    }

    /**
     * Get last exchange rate update timestamp
     */
    public static long getExchangeRateLastUpdate(Context context) {
        return getPrefs(context).getLong(KEY_EXCHANGE_RATE_LAST_UPDATE, 0);
    }

    /**
     * Format last update time as a readable string
     */
    public static String formatLastUpdateTime(Context context) {
        long timestamp = getExchangeRateLastUpdate(context);
        if (timestamp == 0) {
            return null; // Never updated
        }
        
        long now = System.currentTimeMillis();
        long diff = now - timestamp;
        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        
        if (days > 0) {
            return days + (days == 1 ? " day ago" : " days ago");
        } else if (hours > 0) {
            return hours + (hours == 1 ? " hour ago" : " hours ago");
        } else if (minutes > 0) {
            return minutes + (minutes == 1 ? " minute ago" : " minutes ago");
        } else {
            return "Just now";
        }
    }

    /**
     * Get next automatic update timestamp
     */
    public static long getNextUpdateTime(Context context) {
        return getPrefs(context).getLong(KEY_EXCHANGE_RATE_NEXT_UPDATE, 0);
    }

    /**
     * Format next update time as a readable string
     */
    public static String formatNextUpdateTime(Context context) {
        long timestamp = getNextUpdateTime(context);
        if (timestamp == 0) {
            return null; // Not scheduled
        }
        
        long now = System.currentTimeMillis();
        long diff = timestamp - now;
        
        if (diff <= 0) {
            return "Due now";
        }
        
        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        
        if (days > 0) {
            return "In " + days + (days == 1 ? " day" : " days");
        } else if (hours > 0) {
            return "In " + hours + (hours == 1 ? " hour" : " hours");
        } else if (minutes > 0) {
            return "In " + minutes + (minutes == 1 ? " minute" : " minutes");
        } else {
            return "In a moment";
        }
    }

    /**
     * Schedule weekly automatic exchange rate update
     */
    public static void scheduleWeeklyExchangeRateUpdate(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            return;
        }

        // Cancel any existing alarm first
        cancelWeeklyExchangeRateUpdate(context);

        Intent intent = new Intent(context, ExchangeRateUpdateReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                EXCHANGE_RATE_UPDATE_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Schedule for 7 days from now (same time)
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, 7);
        calendar.set(Calendar.HOUR_OF_DAY, 2); // 2 AM
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        long nextUpdateTime = calendar.getTimeInMillis();
        
        // Save next update time
        getPrefs(context).edit().putLong(KEY_EXCHANGE_RATE_NEXT_UPDATE, nextUpdateTime).apply();

        // Use exact alarm for better reliability (Android 6.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    nextUpdateTime,
                    pendingIntent
            );
        } else {
            alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    nextUpdateTime,
                    pendingIntent
            );
        }
    }

    /**
     * Cancel scheduled weekly exchange rate update
     */
    public static void cancelWeeklyExchangeRateUpdate(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            return;
        }

        Intent intent = new Intent(context, ExchangeRateUpdateReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                EXCHANGE_RATE_UPDATE_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        alarmManager.cancel(pendingIntent);
        
        // Clear next update time
        getPrefs(context).edit().putLong(KEY_EXCHANGE_RATE_NEXT_UPDATE, 0).apply();
    }

    // ==================== Backup Sync Settings ====================

    /**
     * Get last backup sync timestamp
     */
    public static long getBackupLastSync(Context context) {
        return getPrefs(context).getLong(KEY_BACKUP_LAST_SYNC, 0);
    }

    /**
     * Set last backup sync timestamp to current time
     */
    public static void setBackupLastSync(Context context) {
        getPrefs(context).edit().putLong(KEY_BACKUP_LAST_SYNC, System.currentTimeMillis()).apply();
    }

    /**
     * Format last backup sync time as a readable string
     */
    public static String formatBackupLastSyncTime(Context context) {
        long timestamp = getBackupLastSync(context);
        if (timestamp == 0) {
            return null; // Never synced
        }
        
        long now = System.currentTimeMillis();
        long diff = now - timestamp;
        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        
        if (days > 0) {
            return days + (days == 1 ? " day ago" : " days ago");
        } else if (hours > 0) {
            return hours + (hours == 1 ? " hour ago" : " hours ago");
        } else if (minutes > 0) {
            return minutes + (minutes == 1 ? " minute ago" : " minutes ago");
        } else {
            return "Just now";
        }
    }
}

