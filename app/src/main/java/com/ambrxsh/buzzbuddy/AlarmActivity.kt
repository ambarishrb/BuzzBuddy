package com.ambrxsh.buzzbuddy

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.ambrxsh.buzzbuddy.fragments.ActivityAlarmFragment
import java.util.Calendar

class AlarmActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Show over lock screen and turn screen on
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }

        setContentView(R.layout.activity_alarm)

//        val hour = intent.getIntExtra("hour", 0)
//        val minute = intent.getIntExtra("minute", 0)

//        save current hour and minute
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val minute = Calendar.getInstance().get(Calendar.MINUTE)

        AlarmPlayer.start(this, hour, minute)


        if (savedInstanceState == null || intent.getBooleanExtra("openAlarmFragment", false)) {
            val alarmId = intent.getIntExtra("alarmId", -1)
            val fragment = ActivityAlarmFragment().apply {
                arguments = Bundle().apply { putInt("alarmId", alarmId) }
            }
            supportFragmentManager.beginTransaction()
                .replace(R.id.alarm_fragment_container, fragment)
                .commit()
        }

    }

    // Handle new intent if activity is already running
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)

        if (intent.getBooleanExtra("openAlarmFragment", false)) {
            val alarmId = intent.getIntExtra("alarmId", -1)
            val fragment = ActivityAlarmFragment().apply {
                arguments = Bundle().apply { putInt("alarmId", alarmId) }
            }

            supportFragmentManager.beginTransaction()
                .replace(R.id.alarm_fragment_container, ActivityAlarmFragment())
                .commit()
        }
    }
}
