package com.ambrxsh.buzzbuddy

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.ambrxsh.buzzbuddy.model.SmartAlarm
import com.ambrxsh.buzzbuddy.room.SmartAlarmsDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar
import android.app.AlarmManager
import android.app.PendingIntent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("BootReceiver", "Device rebooted. Rescheduling alarms...")

            val db = SmartAlarmsDatabase.getDatabase(context)
            val alarmDao = db.smartAlarmDao()

            CoroutineScope(Dispatchers.IO).launch {
                val alarms: List<SmartAlarm> = alarmDao.getAllAlarmsSync()

                for (alarm in alarms) {
                    if (alarm.isEnabled) {
                        scheduleAlarm(context, alarm.alarmId, alarm.alarmTime_hour, alarm.alarmTime_minute)
                        Log.d(
                            "BootReceiver",
                            "Rescheduled alarm ${alarm.alarmId} at ${alarm.alarmTime_hour}:${alarm.alarmTime_minute}"
                        )
                    }
                }
            }
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    private fun scheduleAlarm(context: Context, alarmId: Int, hour: Int, minute: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_MONTH, 1)
        }

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("alarmId", alarmId)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            cal.timeInMillis,
            pendingIntent
        )
    }
}
