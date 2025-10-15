package com.ambrxsh.buzzbuddy.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService

import com.ambrxsh.buzzbuddy.AlarmActivity
import com.ambrxsh.buzzbuddy.R
import com.ambrxsh.buzzbuddy.fragments.ActivityAlarmFragment
import com.ambrxsh.buzzbuddy.model.MainActivity

class BuzzBuddyAlarmForegroundService : Service() {

    private val CHANNEL_ID = "alarm_channel"
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        println("OnStartCommand Called")
        // Intent to open your Activity
        val activityIntent = Intent(this, AlarmActivity::class.java).apply {
//            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            activityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build a notification
        val notification: Notification = NotificationCompat.Builder(this, "alarm_channel")
            .setContentTitle("Launching App")
            .setContentText("Opening MainActivity…")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .build()

        println("Start Foreground Method")
        try {
            // Start foreground service
            startForeground(1, createFullScreenNotification(this, 1))
        } catch (ex: Exception) {
            print(ex)
        }

        // Launch activity immediately (optional but works safely in foreground context)
//        startActivity(activityIntent)

//        stopSelf()
        return START_NOT_STICKY
    }

    private fun createFullScreenNotification(context: Context, alarmId: Int): Notification {
        println("Creating Full Screen Notification ")
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Ringing Alarms",
                NotificationManager.IMPORTANCE_HIGH // Importance must be HIGH
            ).apply {
                description = "Shows when an alarm is actively ringing."
                // Optional: You can configure sound/vibration for the channel, but
                // since you're managing it with AlarmPlayer, it's fine to leave it.
            }
            notificationManager.createNotificationChannel(channel)
        }

        // This is the Intent that will launch your AlarmActivity
        val activityIntent = Intent(context, AlarmActivity::class.java).apply {
            // It's good practice to ensure a new task is created for the alarm screen
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

        // (Your existing stopIntent logic is correct)
//        val stopIntent = Intent(context, AlarmReceiver::class.java).apply {
//            action = AlarmReceiver.ACTION_STOP_ALARM
//        }
//        val stopPendingIntent = PendingIntent.getBroadcast(
//            context,
//            alarmId,
//            stopIntent,
//            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
//        )
        println("Full Screen Notification created")
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Alarm Ringing!")
            .setContentText("Your alarm is going off.")
            .setPriority(NotificationCompat.PRIORITY_HIGH) // Priority must be HIGH
            .setCategory(NotificationCompat.CATEGORY_ALARM) // Important category for alarms
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            // THE MOST IMPORTANT PART:
            .setFullScreenIntent(activityPendingIntent, true) // Pass true for high priority
//            .addAction(android.R.drawable.ic_media_pause, "Stop", stopPendingIntent)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "alarm_channel",
                "Foreground Service Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}