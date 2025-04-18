package com.example.appdevelopmentprojectfinal.timetable;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.appdevelopmentprojectfinal.MainActivity;
import com.example.appdevelopmentprojectfinal.R;

import android.app.PendingIntent;

public class NotificationReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "timetable_notification_channel";
    private static final String TAG = "NotificationReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Notification received");

        // Extract data from intent
        int notificationId = intent.getIntExtra("NOTIFICATION_ID", 0);
        String moduleCode = intent.getStringExtra("MODULE_CODE");
        String moduleName = intent.getStringExtra("MODULE_NAME");
        String location = intent.getStringExtra("LOCATION");

        // Create intent to open the app when notification is tapped (wasn't tested yet)
        Intent mainIntent = new Intent(context, MainActivity.class);
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, mainIntent,
                PendingIntent.FLAG_IMMUTABLE);
        //TODO: Test the functionality of the method.

        // Building notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)  // You should have a proper notification icon
                .setContentTitle("Class Reminder")
                .setContentText("You have " + moduleCode + ": " + moduleName + " soon in " + location)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        // Display notification
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager != null) {
            notificationManager.notify(notificationId, builder.build());
            Log.d(TAG, "Notification displayed for " + moduleCode);
        }
    }
}