package com.ambrxsh.buzzbuddy

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.startActivity
import com.ambrxsh.buzzbuddy.services.BuzzBuddyAlarmForegroundService

class AlarmReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "alarm_channel"
        const val NOTIFICATION_ID = 123
        const val ACTION_STOP_ALARM = "com.ambrxsh.buzzbuddy.STOP_ALARM"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getIntExtra("alarmId", -1)
        println("Received Alarm receiver")
        when (intent.action) {
            ACTION_STOP_ALARM -> {
                AlarmPlayer.stop()
                cancelNotification(context)
            }

            else -> {
                // Start alarm sound + vibration
                AlarmPlayer.start(context)
//
                // Show notification with tap + stop button
                showAlarmNotification(context, alarmId)



//                try {
////                    println("Starting activity")
////                    val launchIntent: Intent? =
////                        context.packageManager.getLaunchIntentForPackage("com.ambrxsh.buzz")
////                    if (launchIntent != null) {
////                        println("launch Intent found")
////                        context.startActivity(launchIntent) //null pointer check in case package name was not found
////                    }
//
//
//
//                    //                // Launch AlarmActivity immediately (if you want auto-open)
////                    val activityIntent = Intent(context, AlarmActivity::class.java).apply {
////                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
////                        putExtra("openAlarmFragment", true)
////                        putExtra("alarmId", alarmId)
////                    }
////                    context.startActivity(activityIntent)
//
//                    println("Starting foreground service")
//                    val serviceIntent = Intent(context, BuzzBuddyAlarmForegroundService::class.java)
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                        println("Starting foreground service")
//                        context.startForegroundService(serviceIntent)
//                    }
//                } catch (ex: Exception) {
//                    println("=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-")
//                    println(ex)
//                }
            }
        }
    }

    private fun showAlarmNotification(context: Context, alarmId: Int) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Alarm Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "Channel for alarm notifications" }
            notificationManager.createNotificationChannel(channel)
        }

        // Intent to open AlarmActivity when tapping notification
        val activityIntent = Intent(context, AlarmActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("openAlarmFragment", true)
            putExtra("alarmId", alarmId)
        }
        val activityPendingIntent = PendingIntent.getActivity(
            context,
            alarmId,
            activityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Stop action
        val stopIntent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_STOP_ALARM
            putExtra("alarmId", alarmId)
        }
        val stopPendingIntent = PendingIntent.getBroadcast(
            context,
            alarmId,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Alarm Ringing!")
            .setContentText("Tap to open alarm or stop it")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(activityPendingIntent)   // tapping opens AlarmActivity
            .addAction(android.R.drawable.ic_media_pause, "Stop", stopPendingIntent)
            .setOngoing(true)     // keeps it until user presses Stop (not dismissable)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }

    private fun cancelNotification(context: Context) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.cancel(NOTIFICATION_ID)
    }
}
