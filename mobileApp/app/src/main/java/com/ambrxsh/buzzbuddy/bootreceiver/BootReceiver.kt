package com.ambrxsh.buzzbuddy

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ambrxsh.buzzbuddy.model.SmartAlarm
import com.ambrxsh.buzzbuddy.room.SmartAlarmsDatabase
import com.ambrxsh.buzzbuddy.scheduler.BuzzBuddyAlarmScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        Timber.d("Device rebooted. Rescheduling alarms...")
        val pendingResult = goAsync()
        val db = SmartAlarmsDatabase.getDatabase(context)
        val alarmDao = db.smartAlarmDao()
        val scheduler = BuzzBuddyAlarmScheduler(context)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val alarms: List<SmartAlarm> = alarmDao.getAllAlarmsSync()
                for (alarm in alarms) {
                    if (alarm.isEnabled) {
                        scheduler.schedule(alarm.alarmId, alarm.alarmTime_hour, alarm.alarmTime_minute)
                        Timber.d(
                            "Rescheduled alarm %s at %s:%s",
                            alarm.alarmId,
                            alarm.alarmTime_hour,
                            alarm.alarmTime_minute
                        )
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
