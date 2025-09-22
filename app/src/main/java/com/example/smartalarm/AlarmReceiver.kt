package com.example.smartalarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.os.Build
import androidx.core.app.NotificationCompat

class AlarmReceiver : BroadcastReceiver() {


    companion object {
        const val CHANNEL_ID = "alarm_channel"
        const val NOTIFICATION_ID = 123
        const val ACTION_STOP_ALARM = "com.example.smartalarm.STOP_ALARM"
    }

    override fun onReceive(context: Context, intent: Intent) {

        val alarmId = intent.getIntExtra("alarmId", -1)

        when (intent.action) {
            ACTION_STOP_ALARM -> {
                AlarmPlayer.stop()
            }
            else -> {
                // Start alarm sound + vibration
                AlarmPlayer.start(context)

                // Show notification with Stop button
                showAlarmNotification(context)

                // Launch AlarmActivity
                val activityIntent = Intent(context, AlarmActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
//                    putExtra("hour", hour)
//                    putExtra("minute", minute)
                    putExtra("openAlarmFragment", true)
                    putExtra("alarmId", alarmId)
                }
                context.startActivity(activityIntent)
            }
        }
    }

    private fun showAlarmNotification(context: Context) {
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

        val stopIntent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_STOP_ALARM
        }

        val stopPendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Alarm Ringing!")
            .setContentText("Tap to open alarm or stop it")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .addAction(android.R.drawable.ic_media_pause, "Stop", stopPendingIntent)
            .setAutoCancel(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }
}
