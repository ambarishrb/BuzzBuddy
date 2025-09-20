package com.example.smartalarm

import android.content.Context
import android.media.MediaPlayer
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

object AlarmPlayer {
    private var currentHour: Int = 0
    private var currentMinute: Int = 0

    fun start(context: Context, hour: Int, minute: Int) {
        currentHour = hour
        currentMinute = minute

    }

    fun getAlarmTime(): String {
        val amPm = if (currentHour >= 12) "PM" else "AM"
        val hour12 = if (currentHour % 12 == 0) 12 else currentHour % 12
        return String.format("%02d:%02d %s", hour12, currentMinute, amPm)
    }
    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null

    fun start(context: Context) {
        // --- Sound ---
        if (mediaPlayer == null) {
            val alarmUri = android.provider.Settings.System.DEFAULT_ALARM_ALERT_URI
            mediaPlayer = MediaPlayer.create(context, alarmUri)
            mediaPlayer?.isLooping = true
            mediaPlayer?.start()
        }

        // --- Vibration ---
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vm.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        val pattern = longArrayOf(0, 500, 500, 500)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(pattern, 0)
        }
    }

    fun stop() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null

        vibrator?.cancel()
        vibrator = null
    }
}
