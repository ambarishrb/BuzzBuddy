package com.ambrxsh.buzzbuddy.repository

import android.app.Application
import androidx.lifecycle.LiveData
import com.ambrxsh.buzzbuddy.model.SmartAlarm
import com.ambrxsh.buzzbuddy.room.SmartAlarmsDatabase
import com.ambrxsh.buzzbuddy.room.smartAlarmDao

class SmartAlarmRepository(application: Application) {

    private val smartAlarmDao: smartAlarmDao
    private val alarmList: LiveData<List<SmartAlarm>>

    init {
        // Updated to match the new Database class
        val database = SmartAlarmsDatabase.getDatabase(application)
        smartAlarmDao = database.smartAlarmDao()
        alarmList = smartAlarmDao.getAllAlarms()
    }

    suspend fun insertAndReturnId(smartAlarm: SmartAlarm): Long {
        return smartAlarmDao.insert(smartAlarm)
    }

    suspend fun update(smartAlarm: SmartAlarm) {
        smartAlarmDao.update(smartAlarm)
    }

    suspend fun delete(smartAlarm: SmartAlarm) {
        smartAlarmDao.delete(smartAlarm)
    }

    fun getAlarmById(alarmId: Int): LiveData<SmartAlarm?> {
        return smartAlarmDao.getAlarmByIdLive(alarmId)
    }

    fun getAllAlarms(): LiveData<List<SmartAlarm>> {
        return alarmList
    }

    suspend fun getAlarmByTime(hour: Int, minute: Int): SmartAlarm? {
        return smartAlarmDao.getAlarmByTime(hour, minute)
    }
}
