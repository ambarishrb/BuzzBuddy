package com.ambrxsh.buzzbuddy.fragments

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.ambrxsh.buzzbuddy.AlarmPlayer
import com.ambrxsh.buzzbuddy.AlarmReceiver
import com.ambrxsh.buzzbuddy.R
import com.ambrxsh.buzzbuddy.utils.SettingsManager
import com.ambrxsh.buzzbuddy.viewmodel.SmartAlarmViewModel
import java.util.Calendar

class ActivityAlarmFragment : Fragment() {

    private lateinit var smartAlarmViewModel: SmartAlarmViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_activity_alarm, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().window.statusBarColor = ContextCompat.getColor(requireContext(), R.color.activityalarm_bg)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requireActivity().window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR // dark icons for light background
        }

        smartAlarmViewModel = ViewModelProvider(this).get(SmartAlarmViewModel::class.java)
        val alarmLabelTextView = view.findViewById<TextView>(R.id.tvAlarmLabel)
        val alarmTimeTextView = view.findViewById<TextView>(R.id.tvAlarmTime)

        alarmTimeTextView.text = AlarmPlayer.getAlarmTime()

        val alarmId = arguments?.getInt("alarmId", -1) ?: -1
        if (alarmId != -1) {
            smartAlarmViewModel.getAlarmById(alarmId).observe(viewLifecycleOwner) { alarm ->
                alarmLabelTextView.text = alarm?.alarmTitle?.takeIf { it.isNotBlank() }
            }
        } else {
            alarmLabelTextView.text = null
        }


        // Dismiss
        view.findViewById<Button>(R.id.dismiss).setOnClickListener {
            val alarmId = arguments?.getInt("alarmId", -1) ?: -1

            // Tell AlarmReceiver to stop alarm + cancel notification
            val stopIntent = Intent(requireContext(), AlarmReceiver::class.java).apply {
                action = AlarmReceiver.ACTION_STOP_ALARM
                putExtra("alarmId", alarmId)
            }
            requireContext().sendBroadcast(stopIntent)

            Toast.makeText(requireContext(), "Alarm dismissed", Toast.LENGTH_SHORT).show()
            activity?.finish()
        }

        // Snooze
        view.findViewById<Button>(R.id.btnSnooze).setOnClickListener {
            val settingsManager = SettingsManager(requireContext())
            val snoozeMinutes = settingsManager.loadSettings().snoozeDuration

            val stopIntent = Intent(requireContext(), AlarmReceiver::class.java).apply {
                action = AlarmReceiver.ACTION_STOP_ALARM
                putExtra("alarmId", alarmId)
            }
            requireContext().sendBroadcast(stopIntent)
            
            Toast.makeText(requireContext(), "Alarm snoozed for $snoozeMinutes min", Toast.LENGTH_SHORT).show()

            val calendar = Calendar.getInstance().apply {
                add(Calendar.MINUTE, snoozeMinutes)
            }
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            scheduleAlarm(alarmId, hour, minute)
            activity?.finish()
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    private fun scheduleAlarm(alarmId: Int, hour: Int, minute: Int) {
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_MONTH, 1)
        }

        val intent = Intent(requireContext(), AlarmReceiver::class.java).apply {
            putExtra("alarmId", alarmId)
        }

        val piFlags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val pendingIntent = PendingIntent.getBroadcast(requireContext(), alarmId, intent, piFlags)

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            cal.timeInMillis,
            pendingIntent
        )
    }
}
