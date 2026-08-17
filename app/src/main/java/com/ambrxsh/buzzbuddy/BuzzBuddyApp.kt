package com.ambrxsh.buzzbuddy

import android.app.Application
import timber.log.Timber

class BuzzBuddyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}
