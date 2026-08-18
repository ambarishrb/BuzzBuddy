package com.ambrxsh.buzzbuddy

import android.app.Application
import com.ambrxsh.buzzbuddy.scheduler.AlarmRescheduler
import com.ambrxsh.buzzbuddy.utils.SettingsManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber

class BuzzBuddyApp : Application() {

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        if (AlarmRescheduler.isUserUnlocked(this)) {
            SettingsManager(this).loadSettings()
        }
        appScope.launch {
            try {
                AlarmRescheduler.rescheduleAll(this@BuzzBuddyApp)
            } catch (e: Exception) {
                Timber.e(e, "Startup reschedule failed")
            }
        }
    }
}
