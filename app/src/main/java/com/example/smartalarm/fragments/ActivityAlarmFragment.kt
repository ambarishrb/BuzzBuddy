package com.example.smartalarm.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.smartalarm.AlarmPlayer
import com.example.smartalarm.R

class ActivityAlarmFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_activity_alarm, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val alarmTimeTextView = view.findViewById<TextView>(R.id.tvAlarmTime)

        alarmTimeTextView.text = AlarmPlayer.getAlarmTime()


        view.findViewById<Button>(R.id.dismiss).setOnClickListener {
            AlarmPlayer.stop()
            Toast.makeText(requireContext(), "Alarm dismissed", Toast.LENGTH_SHORT).show()
        }

        view.findViewById<Button>(R.id.btnSnooze).setOnClickListener {
            AlarmPlayer.stop()
            Toast.makeText(requireContext(), "Alarm snoozed", Toast.LENGTH_SHORT).show()
        }

    }
}
