package com.ambrxsh.buzzbuddy.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ambrxsh.buzzbuddy.EditAlarmActivity
import com.ambrxsh.buzzbuddy.databinding.AlarmItemBinding
import com.ambrxsh.buzzbuddy.model.SmartAlarm

class AlarmAdapter(
    private val listener: OnAlarmToggleListener
) : RecyclerView.Adapter<AlarmAdapter.SmartAlarmViewHolder>() {

    interface OnAlarmToggleListener {
        fun onAlarmToggled(alarm: SmartAlarm, isEnabled: Boolean)
    }

    var alarmList: List<SmartAlarm> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SmartAlarmViewHolder {
        val binding = AlarmItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SmartAlarmViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SmartAlarmViewHolder, position: Int) {
        val smartAlarm = alarmList[position]



        // Convert 24h -> 12h format
        val hour24 = smartAlarm.alarmTime_hour
        val minute = smartAlarm.alarmTime_minute
        val amPm = if (hour24 >= 12) "PM" else "AM"
        val hour12 = if (hour24 % 12 == 0) 12 else hour24 % 12
        val alarmTimeString = String.format("%02d:%02d", hour12, minute)

        with(holder.binding) {
            alarmTime.text = alarmTimeString
            amPmText.text = amPm
            alarmTitle.text = smartAlarm.alarmTitle

            alarmCard.setOnClickListener {
                val intent = Intent(it.context, EditAlarmActivity::class.java)
                intent.putExtra("alarmId", smartAlarm.alarmId)
                it.context.startActivity(intent)
            }

            // Prevent toggle firing during recycling
            alarmSwitch.setOnCheckedChangeListener(null)
            alarmSwitch.isChecked = smartAlarm.isEnabled
            alarmSwitch.setOnCheckedChangeListener { _, isChecked ->
                listener.onAlarmToggled(smartAlarm, isChecked)
            }
        }
    }
    fun returnItemGivenPosition(position: Int) : SmartAlarm { return alarmList[position] }

    override fun getItemCount(): Int = alarmList.size

    class SmartAlarmViewHolder(val binding: AlarmItemBinding) :
        RecyclerView.ViewHolder(binding.root)
}
