package com.ambrxsh.buzzbuddy

import android.annotation.SuppressLint
import android.graphics.Paint
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.NumberPicker
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.ambrxsh.buzzbuddy.model.SmartAlarm
import com.ambrxsh.buzzbuddy.scheduler.BuzzBuddyAlarmScheduler
import com.ambrxsh.buzzbuddy.utils.AlarmPermissionHelper
import com.ambrxsh.buzzbuddy.utils.setTwoDigitRange
import com.ambrxsh.buzzbuddy.viewmodel.SmartAlarmViewModel
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.launch
import timber.log.Timber


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
        val alarmScheduler = BuzzBuddyAlarmScheduler(this)

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

            hourPicker.setTwoDigitRange(0, 23)
            hourPicker.value = alarm.alarmTime_hour
            hourPicker.setTextColor("#212121".toColorInt())

            minutePicker.setTwoDigitRange(0, 59)
            minutePicker.value = alarm.alarmTime_minute
            minutePicker.setTextColor("#212121".toColorInt())

            alarmTitleText.text = alarm.alarmTitle
        }

        // Save button updates alarm and schedules it
        saveButton.setOnClickListener {
            alarm.alarmTime_hour = hourPicker.value
            alarm.alarmTime_minute = minutePicker.value
            alarm.alarmTitle = alarmTitleText.text.toString()

            lifecycleScope.launch {
                smartAlarmViewModel.update(alarm)
                alarmScheduler.cancel(alarm.alarmId)
                if (!alarmScheduler.schedule(alarm.alarmId, alarm.alarmTime_hour, alarm.alarmTime_minute)) {
                    AlarmPermissionHelper.requestExactAlarmPermission(this@EditAlarmActivity)
                }
            }
            finish()
        }

        // Cancel button just closes activity
        cancelButton.setOnClickListener {
            finish()
        }
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
            Timber.e(e, "Could not set NumberPicker text color")
        }
    }


}