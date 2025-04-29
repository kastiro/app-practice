//package com.example.appdevelopmentprojectfinal.timetable;
//
//import android.app.AlarmManager;
//import android.app.NotificationChannel;
//import android.app.NotificationManager;
//import android.app.PendingIntent;
//import android.content.Context;
//import android.content.Intent;
//import android.net.Uri;
//import android.os.Build;
//import android.provider.Settings;
//import android.util.Log;
//import android.widget.Toast;
//
//import androidx.core.app.NotificationCompat;
//
//import com.example.appdevelopmentprojectfinal.R;
//
//import java.text.ParseException;
//import java.text.SimpleDateFormat;
//import java.util.Calendar;
//import java.util.Date;
//import java.util.Locale;
//
//public class TimetableNotificationManager {
//    private static final String CHANNEL_ID = "timetable_notification_channel";
//    private static final String TAG = "TimetableNotification";
//    private static final long NOTIFICATION_OFFSET_MILLIS = 5 * 60 * 1000; // 5 minutes (will be used to define how long before the module does nfc come)
//
//    // Create the notification channel
//    public static void createNotificationChannel(Context context) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            CharSequence name = "Timetable Notifications";
//            String description = "Notifications for upcoming classes";
//            int importance = NotificationManager.IMPORTANCE_DEFAULT;
//            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
//            channel.setDescription(description);
//
//            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
//            if (notificationManager != null) {
//                notificationManager.createNotificationChannel(channel);
//            }
//        }
//    }
//
//    // Schedule a notification for a module
//    public static void scheduleNotification(Context context, ModuleSchedule moduleSchedule) {
//        if (!moduleSchedule.isNotificationsEnabled()) {
//            return;
//        }
//
//        // Get alarm manager service
//        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
//        if (alarmManager == null) {
//            return;
//        }
//
//        // Check if we have permission to schedule alarms
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//            if (!alarmManager.canScheduleExactAlarms()) {
//                // If we don't have permission well show a toast and open settings
//                Toast.makeText(context, "Permission to schedule alarms is required", Toast.LENGTH_SHORT).show();
//                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
//                intent.setData(Uri.parse("package:" + context.getPackageName()));
//                context.startActivity(intent);
//                return;
//            }
//        }
//
//        Module module = moduleSchedule.getModule();
//        TimeSlot timeSlot = moduleSchedule.getTimeSlot();
//
//        // Unique ID for notification
//        int notificationId = (module.getCode() + timeSlot.getDay() + timeSlot.getStartTime()).hashCode();
//
//        // Calculate the time for the next occurrence of the class
//        long notificationTimeMillis = getNextOccurrenceTime(timeSlot);
//        if (notificationTimeMillis == -1) {
//            Log.e(TAG, "Failed to calculate next occurrence time");
//            return;
//        }
//
//        // Calculate the -5 minutes to notify before class starts
//        notificationTimeMillis -= NOTIFICATION_OFFSET_MILLIS;
//
//        // Create intent for notification
//        Intent intent = new Intent(context, NotificationReceiver.class);
//        intent.putExtra("NOTIFICATION_ID", notificationId);
//        intent.putExtra("MODULE_CODE", module.getCode());
//        intent.putExtra("MODULE_NAME", module.getName());
//        intent.putExtra("LOCATION", timeSlot.getLocation());
//
//        PendingIntent pendingIntent = PendingIntent.getBroadcast(
//                context,
//                notificationId,
//                intent,
//                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
//        );
//
//        // Schedule the alarm
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            alarmManager.setExactAndAllowWhileIdle(
//                    AlarmManager.RTC_WAKEUP,
//                    notificationTimeMillis,
//                    pendingIntent
//            );
//        } else {
//            alarmManager.setExact(
//                    AlarmManager.RTC_WAKEUP,
//                    notificationTimeMillis,
//                    pendingIntent
//            );
//        }
//        Log.d(TAG, "Scheduled notification for " + module.getCode() + " at " + new Date(notificationTimeMillis));
//    }
//
//    // Cancel a notification for a module
//    public static void cancelNotification(Context context, ModuleSchedule moduleSchedule) {
//        Module module = moduleSchedule.getModule();
//        TimeSlot timeSlot = moduleSchedule.getTimeSlot();
//
//    //(Same ID calculation as in scheduleNotification)
//        int notificationId = (module.getCode() + timeSlot.getDay() + timeSlot.getStartTime()).hashCode();
//
//        Intent intent = new Intent(context, NotificationReceiver.class);
//        PendingIntent pendingIntent = PendingIntent.getBroadcast(
//                context,
//                notificationId,
//                intent,
//                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
//        );
//
//        // Cancel the alarm
//        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
//        if (alarmManager != null) {
//            alarmManager.cancel(pendingIntent);
//            Log.d(TAG, "Cancelled notification for " + module.getCode());
//        }
//    }
//
//    // Calculate next occurrence time based on day and time
//    private static long getNextOccurrenceTime(TimeSlot timeSlot) {
//        try {
//            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
//            Date timeDate = timeFormat.parse(timeSlot.getStartTime());
//            if (timeDate == null) {
//                return -1;
//            }
//            Calendar calendar = Calendar.getInstance();
//            Calendar targetCalendar = Calendar.getInstance();
//            targetCalendar.setTime(timeDate);
//
//            calendar.set(Calendar.HOUR_OF_DAY, targetCalendar.get(Calendar.HOUR_OF_DAY));
//            calendar.set(Calendar.MINUTE, targetCalendar.get(Calendar.MINUTE));
//            calendar.set(Calendar.SECOND, 0);
//            calendar.set(Calendar.MILLISECOND, 0);
//
//            int targetDay = getDayOfWeek(timeSlot.getDay());
//            int currentDay = calendar.get(Calendar.DAY_OF_WEEK);
//
//            if (targetDay == -1) {
//                return -1;
//            }
//
//            // If it's the same day but time has passed, add 7 days (next week)
//        if (targetDay == currentDay && calendar.getTimeInMillis() < System.currentTimeMillis()) {
//                calendar.add(Calendar.DAY_OF_MONTH, 7);
//            }
//            // If target day is before current day in the week, add days until next week's occurrence
//        else if (targetDay < currentDay) {
//                calendar.add(Calendar.DAY_OF_MONTH, 7 - (currentDay - targetDay));
//            }
//    // If target day after current day, add days until occurrence
//        else if (targetDay > currentDay) {
//                calendar.add(Calendar.DAY_OF_MONTH, targetDay - currentDay);
//            }
//
//            return calendar.getTimeInMillis();
//        } catch (ParseException e) {
//            Log.e(TAG, "Failed to parse time: " + e.getMessage());
//            return -1;
//        }
//    }
//
//    // Helper to convert day string to Calendar.DAY_OF_WEEK
//    private static int getDayOfWeek(String day) {
//        switch (day.toLowerCase()) {
//            case "monday": return Calendar.MONDAY;
//            case "tuesday": return Calendar.TUESDAY;
//            case "wednesday": return Calendar.WEDNESDAY;
//            case "thursday": return Calendar.THURSDAY;
//            case "friday": return Calendar.FRIDAY;
//            case "saturday": return Calendar.SATURDAY;
//            case "sunday": return Calendar.SUNDAY;
//            default: return -1;
//        }
//    }
//}