package vn.edu.tdtu.lhqc.budtrack.services.notifications;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import java.util.Calendar;

import vn.edu.tdtu.lhqc.budtrack.R;
import vn.edu.tdtu.lhqc.budtrack.activities.MainActivity;
import vn.edu.tdtu.lhqc.budtrack.services.settings.SettingsHandler;

/**
 * Service for managing reminder notifications.
 * Handles scheduling and canceling daily expense reminder notifications.
 */
public class ReminderNotificationService {

    private static final String CHANNEL_ID = "reminder_notification_channel";
    private static final String CHANNEL_NAME = "Expense Reminders";
    private static final int NOTIFICATION_ID = 1001;
    private static final int REQUEST_CODE = 2001;

    /**
     * Create notification channel for Android O and above
     */
    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Daily reminders to enter your expenses");
            channel.enableLights(true);
            channel.enableVibration(true);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    /**
     * Schedule daily reminder notification
     */
    public static void scheduleReminder(Context context) {
        if (!SettingsHandler.isReminderEnabled(context)) {
            return;
        }

        createNotificationChannel(context);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            return;
        }

        // Cancel any existing alarm first
        cancelReminder(context);

        Intent intent = new Intent(context, ReminderNotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Get reminder time from settings
        int hour = SettingsHandler.getReminderHour(context);
        int minute = SettingsHandler.getReminderMinute(context);

        // Set calendar to reminder time
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        // If the time has already passed today, schedule for tomorrow
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        // Use exact alarm for better reliability (Android 6.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    pendingIntent
            );
        } else {
            alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    pendingIntent
            );
        }
    }

    /**
     * Cancel scheduled reminder notification
     */
    public static void cancelReminder(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            return;
        }

        Intent intent = new Intent(context, ReminderNotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        alarmManager.cancel(pendingIntent);
        pendingIntent.cancel();
    }

    /**
     * BroadcastReceiver to handle notification when alarm fires
     */
    public static class ReminderNotificationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Check if reminder is still enabled
            if (!SettingsHandler.isReminderEnabled(context)) {
                return;
            }

            // Check notification permission
            if (!SettingsHandler.isNotificationPermissionGranted(context)) {
                // Permission not granted, reschedule anyway
                scheduleReminder(context);
                return;
            }

            createNotificationChannel(context);

            // Create intent to open app when notification is tapped
            Intent notificationIntent = new Intent(context, MainActivity.class);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    notificationIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            // Build notification
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notifications_24dp)
                    .setContentTitle(context.getString(R.string.notification_expense_reminder_title))
                    .setContentText(context.getString(R.string.notification_expense_reminder_message))
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText(context.getString(R.string.notification_expense_reminder_message)))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setDefaults(NotificationCompat.DEFAULT_ALL);

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                try {
                    notificationManager.notify(NOTIFICATION_ID, builder.build());
                } catch (SecurityException e) {
                    // Permission might have been revoked
                    e.printStackTrace();
                }
            }

            // Reschedule for next day
            scheduleReminder(context);
        }
    }
}

