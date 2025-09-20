package com.example.smartalarm.fragments

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
import android.widget.EditText
import android.widget.NumberPicker
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.graphics.toColorInt
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smartalarm.AlarmReceiver
import com.example.smartalarm.R
import com.example.smartalarm.adapter.AlarmAdapter
import com.example.smartalarm.databinding.FragmentSetAlarmBinding
import com.example.smartalarm.model.SmartAlarm
import com.example.smartalarm.viewmodel.SmartAlarmViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.util.Calendar

class SetAlarmPage : Fragment() {

    private var _binding: FragmentSetAlarmBinding? = null
    private val binding get() = _binding!!

    private lateinit var smartAlarmViewModel: SmartAlarmViewModel
    private lateinit var alarmAdapter: AlarmAdapter
    private lateinit var alarmStatus: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSetAlarmBinding.inflate(inflater, container, false)
        return binding.root

    }

    @RequiresApi(Build.VERSION_CODES.Q)
    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        smartAlarmViewModel = ViewModelProvider(requireActivity())[SmartAlarmViewModel::class.java]

        alarmAdapter = AlarmAdapter(object : AlarmAdapter.OnAlarmToggleListener {
            override fun onAlarmToggled(alarm: SmartAlarm, isEnabled: Boolean) {
                alarm.isEnabled = isEnabled
                smartAlarmViewModel.update(alarm)

                if (isEnabled) {
                    cancelCurrentAlarm(alarm.alarmId)
                    scheduleAlarm(alarm.alarmId, alarm.alarmTime_hour, alarm.alarmTime_minute)
                } else {
                    cancelCurrentAlarm(alarm.alarmId)
                }
            }
        })

        setNextAlarmStatus();

        binding.recyclerViewAlarms.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewAlarms.adapter = alarmAdapter

        smartAlarmViewModel.getAllAlarms().observe(viewLifecycleOwner) { alarms ->
            alarmAdapter.alarmList = alarms
            alarmAdapter.notifyDataSetChanged()
        }

        alarmStatus = binding.tvNextAlarm


        binding.settingsIcon.setOnClickListener {
            val settingsFragment = SettingsFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, settingsFragment)
                .addToBackStack(null)
                .commit()
        }

        binding.addAlarmButton.setOnClickListener { showCustomDialog() }

        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val alarmToDelete = alarmAdapter.returnItemGivenPosition(position)

                // Cancel scheduled alarm
                cancelCurrentAlarm(alarmToDelete.alarmId)

                // Delete from database via ViewModel
                smartAlarmViewModel.delete(alarmToDelete)

                // Remove from adapter list immediately
                val currentList = alarmAdapter.alarmList.toMutableList()
                currentList.removeAt(position)
                alarmAdapter.alarmList = currentList
                alarmAdapter.notifyItemRemoved(position)

                // Optional: Undo
                Snackbar.make(binding.root, "Alarm deleted", Snackbar.LENGTH_LONG)
                    .setAction("Undo") {
                        lifecycleScope.launch {
                            val newId = smartAlarmViewModel.insertAndReturnId(alarmToDelete).toInt()
                            alarmToDelete.alarmId = newId
                            if (alarmToDelete.isEnabled) {
                                scheduleAlarm(
                                    alarmToDelete.alarmId,
                                    alarmToDelete.alarmTime_hour,
                                    alarmToDelete.alarmTime_minute
                                )
                            }

                            // Add back to adapter list
                            val updatedList = alarmAdapter.alarmList.toMutableList()
                            updatedList.add(position, alarmToDelete)
                            alarmAdapter.alarmList = updatedList
                            alarmAdapter.notifyItemInserted(position)
                        }
                    }.show()
            }
        }

        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(binding.recyclerViewAlarms)


    }

    @RequiresApi(Build.VERSION_CODES.Q)
    @SuppressLint("SetTextI18n", "DefaultLocale")
    private fun showCustomDialog() {
        val builder = android.app.AlertDialog.Builder(requireContext())
        val dialogView = layoutInflater.inflate(R.layout.custom_dialog_layout, null)
        builder.setView(dialogView)
        val dialog = builder.create()

        val hourPicker = dialogView.findViewById<NumberPicker>(R.id.hour_picker).apply {
            minValue = 0
            maxValue = 23
            setTextColor("#212121".toColorInt())
        }

        val minutePicker = dialogView.findViewById<NumberPicker>(R.id.minute_picker).apply {
            minValue = 0
            maxValue = 59
            setTextColor("#212121".toColorInt())
        }

        val alarm_title_et = dialogView.findViewById<EditText>(R.id.alarm_title)

        dialogView.findViewById<Button>(R.id.cancel_button).setOnClickListener {
            dialog.dismiss()
        }

        dialogView.findViewById<Button>(R.id.set_button).setOnClickListener {
            val selectedHour = hourPicker.value
            val selectedMinute = minutePicker.value

            lifecycleScope.launch {
                val existingAlarm = smartAlarmViewModel.getAlarmByTime(selectedHour, selectedMinute)
                if (existingAlarm != null) {
                    Toast.makeText(requireContext(), "Alarm already set for this time!", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                    return@launch
                }

                val newAlarm = SmartAlarm(
                    alarmTitle = alarm_title_et.text.toString(),
                    alarmTime_hour = selectedHour,
                    alarmTime_minute = selectedMinute,
                    isEnabled = true,
                )

                val generatedId = smartAlarmViewModel.insertAndReturnId(newAlarm).toInt()
                newAlarm.alarmId = generatedId
                smartAlarmViewModel.update(newAlarm)

                scheduleAlarm(newAlarm.alarmId, selectedHour, selectedMinute)

                val hour12 = if (selectedHour % 12 == 0) 12 else selectedHour % 12
                val amPm = if (selectedHour >= 12) "PM" else "AM"
                alarmStatus.text = "Next alarm at ${String.format("%02d:%02d %s", hour12, selectedMinute, amPm)}"
                alarmStatus.setTextColor("#8E8E93".toColorInt())
                alarmStatus.visibility = View.VISIBLE

                setNextAlarmStatus();
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    /**
     * fetch all the alarms and figure out the next alarm and update the alarm status accordingly
     */
    private fun setNextAlarmStatus() {
        smartAlarmViewModel.getAllAlarms().observe(viewLifecycleOwner) { alarms ->
            val nextAlarm = alarms.minByOrNull { it.alarmTime_hour * 60 + it.alarmTime_minute }

            if (nextAlarm != null) {
                val hour12 = if (nextAlarm.alarmTime_hour % 12 == 0) 12 else nextAlarm.alarmTime_hour % 12
                val amPm = if (nextAlarm.alarmTime_hour >= 12) "PM" else "AM"
                alarmStatus.text = "Next alarm at ${String.format("%02d:%02d %s", hour12, nextAlarm.alarmTime_minute, amPm)}"
                alarmStatus.setTextColor("#8E8E93".toColorInt())

            } else {
                alarmStatus.text = "No alarms set"
                alarmStatus.setTextColor("#8E8E93".toColorInt())

            }
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

        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pendingIntent)
    }

    private fun cancelCurrentAlarm(alarmId: Int) {
        val intent = Intent(requireContext(), AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            alarmId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
