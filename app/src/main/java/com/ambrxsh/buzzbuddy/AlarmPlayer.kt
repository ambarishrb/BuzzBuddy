package com.ambrxsh.buzzbuddy

import android.content.Context
import android.media.MediaPlayer
import android.os.*
import com.ambrxsh.buzzbuddy.utils.SettingsManager
import java.util.Calendar

object AlarmPlayer {
    private var currentHour: Int = 0
    private var currentMinute: Int = 0

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null

    fun start(context: Context, hour: Int, minute: Int) {
        currentHour = hour
        currentMinute = minute
        start(context)
    }

    fun getAlarmTime(): String {
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)

        val amPm = if (currentHour >= 12) "PM" else "AM"
        val hour12 = if (currentHour % 12 == 0) 12 else currentHour % 12
        return String.format("%02d:%02d %s", hour12, currentMinute, amPm)
    }



    fun start(context: Context) {
        val settingsManager = SettingsManager(context)
        val settings = settingsManager.loadSettings()

        // --- Sound ---
        if (mediaPlayer == null) {
            val alarmUri = android.provider.Settings.System.DEFAULT_ALARM_ALERT_URI
            mediaPlayer = MediaPlayer.create(context, alarmUri)
            mediaPlayer?.isLooping = true

            if (settings.gradualVolume) {
                gradualVolumeIncrease(settings.volume)
            } else {
                val userVolume = settings.volume / 100f
                mediaPlayer?.setVolume(userVolume, userVolume)
            }

            mediaPlayer?.start()
        }


        if (settings.vibrate) {
            vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm =
                    context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
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


        if (settings.autoDismiss) {
            Handler(Looper.getMainLooper()).postDelayed({
                stop()
            }, 120_000)
        }
    }

    private fun gradualVolumeIncrease(targetPercent: Int) {
        val targetVolume = targetPercent / 100f
        var currentVolume = 0f
        val handler = Handler(Looper.getMainLooper())

        val runnable = object : Runnable {
            override fun run() {
                if (mediaPlayer != null && currentVolume < targetVolume) {
                    currentVolume += 0.05f
                    if (currentVolume > targetVolume) currentVolume = targetVolume
                    mediaPlayer?.setVolume(currentVolume, currentVolume)
                    handler.postDelayed(this, 500)
                }
            }
        }
        handler.post(runnable)
    }

    fun stop() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null

        vibrator?.cancel()
        vibrator = null
    }
}
