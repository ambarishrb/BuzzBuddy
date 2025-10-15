package com.ambrxsh.buzzbuddy.fragments

import android.app.AlertDialog
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import androidx.fragment.app.Fragment
import com.ambrxsh.buzzbuddy.R
import com.ambrxsh.buzzbuddy.model.SettingsData
import com.ambrxsh.buzzbuddy.utils.SettingsManager
import com.google.android.material.appbar.MaterialToolbar

class SettingsFragment : Fragment() {

    private lateinit var settingsManager: SettingsManager
    private lateinit var settings: SettingsData

    private lateinit var seekBarVolume: SeekBar
    private lateinit var switchGradualVolume: Switch
    private lateinit var switchVibrate: Switch
    private lateinit var switchAutoDismiss: Switch
    private lateinit var tvSnoozeDuration: TextView
    private lateinit var btnEditSnooze: ImageView
    private lateinit var tvAlarmSound: TextView
    private lateinit var btnChangeSound: ImageView
    private lateinit var layoutSnooze: LinearLayout
    private lateinit var layoutAlarmSound: LinearLayout
    private lateinit var layoutGradualVolume: LinearLayout
    private lateinit var layoutVibrate: LinearLayout
    private lateinit var layoutAutoDismiss: LinearLayout

    override fun onCreateView(
        inflater: android.view.LayoutInflater, container: android.view.ViewGroup?,
        savedInstanceState: Bundle?
    ): android.view.View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Status bar
        requireActivity().window.statusBarColor =
            ContextCompat.getColor(requireContext(), R.color.app_theme)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requireActivity().window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }

        // Toolbar
        val toolbar = view.findViewById<MaterialToolbar>(R.id.settings_toolbar)
        toolbar.setNavigationIcon(R.drawable.ic_back)
        toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        // Settings manager
        settingsManager = SettingsManager(requireContext())
        settings = settingsManager.loadSettings()

        // Bind views
        seekBarVolume = view.findViewById(R.id.seekBarVolume)
        switchGradualVolume = view.findViewById(R.id.switchGradualVolume)
        switchVibrate = view.findViewById(R.id.switchVibrate)
        switchAutoDismiss = view.findViewById(R.id.switchAutoDismiss)
        tvSnoozeDuration = view.findViewById(R.id.tvSnoozeDuration)
        btnEditSnooze = view.findViewById(R.id.btnEditSnooze)
        tvAlarmSound = view.findViewById(R.id.tvAlarmSound)
        btnChangeSound = view.findViewById(R.id.btnChangeSound)

        layoutSnooze = view.findViewById(R.id.layoutSnooze)
        layoutAlarmSound = view.findViewById(R.id.layoutAlarmSound)
        layoutGradualVolume = view.findViewById(R.id.layoutGradualVolume)
        layoutVibrate = view.findViewById(R.id.layoutVibrate)
        layoutAutoDismiss = view.findViewById(R.id.layoutAutoDismiss)

        // Load initial values
        seekBarVolume.progress = settings.volume
        switchGradualVolume.isChecked = settings.gradualVolume
        switchVibrate.isChecked = settings.vibrate
        switchAutoDismiss.isChecked = settings.autoDismiss
        tvSnoozeDuration.text = "${settings.snoozeDuration} minutes"
        tvAlarmSound.text = settings.alarmSound

        // SeekBar listener
        seekBarVolume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                settings.volume = progress
                settingsManager.saveSettings(settings)
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })

        // Switch toggle rows
        layoutGradualVolume.setOnClickListener {
            switchGradualVolume.isChecked = !switchGradualVolume.isChecked
            settings.gradualVolume = switchGradualVolume.isChecked
            settingsManager.saveSettings(settings)
        }

        layoutVibrate.setOnClickListener {
            switchVibrate.isChecked = !switchVibrate.isChecked
            settings.vibrate = switchVibrate.isChecked
            settingsManager.saveSettings(settings)
        }

        layoutAutoDismiss.setOnClickListener {
            switchAutoDismiss.isChecked = !switchAutoDismiss.isChecked
            settings.autoDismiss = switchAutoDismiss.isChecked
            settingsManager.saveSettings(settings)
        }

        // Snooze picker
        val snoozeClickListener = View.OnClickListener {
            val numberPicker = NumberPicker(requireContext()).apply {
                setTextColor("#212121".toColorInt())
                minValue = 1
                maxValue = 60
                value = settings.snoozeDuration
            }

            val layout = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(30, 30, 30, 30)
                addView(numberPicker)
            }

            val dialog = AlertDialog.Builder(requireContext(), R.style.Snooze_dialog_theme)
                .setTitle("Snooze Duration (min)")
                .setView(layout)
                .setPositiveButton("OK") { d, _ ->
                    settings.snoozeDuration = numberPicker.value
                    tvSnoozeDuration.text = "${numberPicker.value} minutes"
                    settingsManager.saveSettings(settings)
                    d.dismiss()
                }
                .setNegativeButton("Cancel") { d, _ -> d.dismiss() }
                .create()

            dialog.show()
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor("#e14f62".toColorInt())
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor("#696969".toColorInt())
        }

        layoutSnooze.setOnClickListener(snoozeClickListener)
        btnEditSnooze.setOnClickListener(snoozeClickListener)

        // Alarm sound toggle on row or button
        val alarmSoundClick = View.OnClickListener {
            val newSound = if (settings.alarmSound == "Sunrise") "Beep" else "Sunrise"
            settings.alarmSound = newSound
            tvAlarmSound.text = newSound
            settingsManager.saveSettings(settings)
        }

        layoutAlarmSound.setOnClickListener(alarmSoundClick)
        btnChangeSound.setOnClickListener(alarmSoundClick)
    }
}
