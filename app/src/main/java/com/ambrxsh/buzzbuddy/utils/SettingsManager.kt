package com.ambrxsh.buzzbuddy.utils

import android.content.Context
import com.ambrxsh.buzzbuddy.model.SettingsData
import com.google.gson.Gson

class SettingsManager(context: Context) {

    private val prefs = context.getSharedPreferences("buzz_settings", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun saveSettings(settings: SettingsData) {
        val json = gson.toJson(settings)
        prefs.edit().putString("settings_json", json).apply()
    }

    fun loadSettings(): SettingsData {
        val json = prefs.getString("settings_json", null)
        return if (json != null) {
            gson.fromJson(json, SettingsData::class.java)
        } else {
            SettingsData()
        }
    }
}