package com.ambrxsh.buzzbuddy.repository

import android.app.Application
import androidx.lifecycle.LiveData
import com.ambrxsh.buzzbuddy.model.SmartAlarm
import com.ambrxsh.buzzbuddy.room.SmartAlarmsDatabase
import com.ambrxsh.buzzbuddy.room.smartAlarmDao
import com.ambrxsh.buzzbuddy.scheduler.AlarmScheduleCache

class SmartAlarmRepository(application: Application) {

    private val appContext = application.applicationContext
    private val smartAlarmDao: smartAlarmDao
    private val alarmList: LiveData<List<SmartAlarm>>

    init {
        val database = SmartAlarmsDatabase.getDatabase(application)
        smartAlarmDao = database.smartAlarmDao()
        alarmList = smartAlarmDao.getAllAlarms()
    }

    suspend fun insertAndReturnId(smartAlarm: SmartAlarm): Long {
        val id = smartAlarmDao.insert(smartAlarm)
        persistScheduleCache()
        return id
    }

    suspend fun restore(smartAlarm: SmartAlarm) {
        smartAlarmDao.insert(smartAlarm)
        persistScheduleCache()
    }

    suspend fun update(smartAlarm: SmartAlarm) {
        smartAlarmDao.update(smartAlarm)
        persistScheduleCache()
    }

    suspend fun delete(smartAlarm: SmartAlarm) {
        smartAlarmDao.delete(smartAlarm)
        persistScheduleCache()
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

    suspend fun getAlarmByTimeExcluding(hour: Int, minute: Int, excludeId: Int): SmartAlarm? {
        return smartAlarmDao.getAlarmByTimeExcluding(hour, minute, excludeId)
    }

    private fun persistScheduleCache() {
        AlarmScheduleCache.save(
            appContext,
            smartAlarmDao.getAllAlarmsSync().filter { it.isEnabled }
        )
    }
}
