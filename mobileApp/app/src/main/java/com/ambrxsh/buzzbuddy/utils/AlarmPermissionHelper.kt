package com.ambrxsh.buzzbuddy.utils

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.ambrxsh.buzzbuddy.R
import com.ambrxsh.buzzbuddy.scheduler.BuzzBuddyAlarmScheduler

object AlarmPermissionHelper {

    const val REQUEST_POST_NOTIFICATIONS = 4101

    fun requestStartupPermissions(activity: Activity) {
        requestNotificationPermission(activity)
        requestExactAlarmPermission(activity)
        requestFullScreenIntentPermission(activity)
    }

    fun requestNotificationPermission(activity: Activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val granted = ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                REQUEST_POST_NOTIFICATIONS
            )
        }
    }

    fun requestExactAlarmPermission(activity: Activity) {
        val scheduler = BuzzBuddyAlarmScheduler(activity)
        if (scheduler.canScheduleExactAlarms()) return

        AlertDialog.Builder(activity)
            .setTitle(R.string.exact_alarm_permission_title)
            .setMessage(R.string.exact_alarm_permission_message)
            .setPositiveButton(R.string.permission_open_settings) { _, _ ->
                scheduler.openExactAlarmSettings(activity)
            }
            .setNegativeButton(R.string.permission_not_now, null)
            .show()
    }

    fun requestFullScreenIntentPermission(activity: Activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) return
        if (NotificationManagerCompat.from(activity).canUseFullScreenIntent()) return

        AlertDialog.Builder(activity)
            .setTitle(R.string.full_screen_permission_title)
            .setMessage(R.string.full_screen_permission_message)
            .setPositiveButton(R.string.permission_open_settings) { _, _ ->
                val intent = Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT).apply {
                    data = "package:${activity.packageName}".toUri()
                }
                activity.startActivity(intent)
            }
            .setNegativeButton(R.string.permission_not_now, null)
            .show()
    }
}
