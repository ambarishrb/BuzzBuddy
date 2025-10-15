package com.ambrxsh.buzzbuddy

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.NumberPicker
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.ambrxsh.buzzbuddy.model.SmartAlarm
import com.ambrxsh.buzzbuddy.viewmodel.SmartAlarmViewModel
import kotlinx.coroutines.launch
import java.util.Calendar
import com.google.android.material.appbar.MaterialToolbar
import androidx.core.net.toUri


class EditAlarmActivity : AppCompatActivity() {

    private lateinit var smartAlarmViewModel: SmartAlarmViewModel
    private var alarmId: Int = -1
    private lateinit var alarm: SmartAlarm

    private lateinit var hourPicker: NumberPicker
    private lateinit var minutePicker: NumberPicker
    private lateinit var alarmTitleText: TextView
    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_alarm)

        val toolbar = findViewById<MaterialToolbar>(R.id.update_toolbar)
        toolbar.setNavigationIcon(R.drawable.ic_back)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        window.statusBarColor = ContextCompat.getColor(this, R.color.app_theme)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = 0 // light icons for dark background
        }



        smartAlarmViewModel = ViewModelProvider(this)[SmartAlarmViewModel::class.java]

        alarmId = intent.getIntExtra("alarmId", -1)
        if (alarmId == -1) finish()

        hourPicker = findViewById(R.id.hour_picker)
        minutePicker = findViewById(R.id.minute_picker)
        alarmTitleText = findViewById(R.id.alarm_title)
        saveButton = findViewById(R.id.save_button)
        cancelButton = findViewById(R.id.delete_button) // acts as cancel

        // Fetch the alarm and populate UI
        smartAlarmViewModel.getAllAlarms().observe(this) { alarms ->
            alarm = alarms.find { it.alarmId == alarmId } ?: return@observe

            hourPicker.minValue = 0
            hourPicker.maxValue = 23

            hourPicker.value = alarm.alarmTime_hour
            hourPicker.apply {
                setTextColor("#212121".toColorInt())
            }

            minutePicker.minValue = 0
            minutePicker.maxValue = 59

            minutePicker.value = alarm.alarmTime_minute
            minutePicker.apply {
                setTextColor("#212121".toColorInt())
            }

            alarmTitleText.text = alarm.alarmTitle
        }

        // Save button updates alarm and schedules it
        saveButton.setOnClickListener {
            alarm.alarmTime_hour = hourPicker.value
            alarm.alarmTime_minute = minutePicker.value
            alarm.alarmTitle = alarmTitleText.text.toString()

            lifecycleScope.launch {
                smartAlarmViewModel.update(alarm)

                // Cancel old alarm (if any) and schedule updated one
                cancelCurrentAlarm(alarm.alarmId)
                scheduleAlarm(alarm.alarmId, alarm.alarmTime_hour, alarm.alarmTime_minute)
            }
            finish()
        }

        // Cancel button just closes activity
        cancelButton.setOnClickListener {
            finish()
        }
    }

    private fun checkAndRequestFullScreenPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) { // Android 14+
            val notificationManager = NotificationManagerCompat.from(this)
            // canUseFullScreenIntent() checks if your app has the special permission
            if (!notificationManager.canUseFullScreenIntent()) {
                // Permission not granted, guide the user to settings
                val intent = Intent(
                    Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT,
                    "package:$packageName".toUri()
                )
                startActivity(intent)
                // You should show a dialog explaining WHY you need this permission first.
            }
        }
        // For Android 10-13, the permission is granted at install time,
        // but some OEMs still add their own restrictions.
    }
    @SuppressLint("ScheduleExactAlarm")
    private fun scheduleAlarm(alarmId: Int, hour: Int, minute: Int) {
        checkAndRequestFullScreenPermission();
        val alarmManager =
            getSystemService(ALARM_SERVICE) as AlarmManager

        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_MONTH, 1)
        }

        val intent = Intent(this, AlarmReceiver::class.java).apply {
            putExtra("alarmId", alarmId)
        }

        val piFlags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            alarmId,
            intent,
            piFlags
        )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            cal.timeInMillis,
            pendingIntent
        )
    }

    private fun cancelCurrentAlarm(alarmId: Int) {
        val intent = Intent(this, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            alarmId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }
    @SuppressLint("SoonBlockedPrivateApi")

    fun NumberPicker.setTextColorCompat(color: Int) {
        try {
            // Change selector wheel paint
            val selectorWheelPaintField = NumberPicker::class.java.getDeclaredField("mSelectorWheelPaint")
            selectorWheelPaintField.isAccessible = true
            val paint = selectorWheelPaintField.get(this) as Paint
            paint.color = color

            // Change EditText inside NumberPicker
            val inputTextField = NumberPicker::class.java.getDeclaredField("mInputText")
            inputTextField.isAccessible = true
            val inputText = inputTextField.get(this) as EditText
            inputText.setTextColor(color)

            // Force redraw
            invalidate()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


}