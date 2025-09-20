package com.example.smartalarm.repository

import android.app.Application
import androidx.lifecycle.LiveData
import com.example.smartalarm.model.SmartAlarm
import com.example.smartalarm.room.SmartAlarmsDatabase
import com.example.smartalarm.room.smartAlarmDao
import kotlinx.coroutines.Job

class SmartAlarmRepository(application: Application) {

    var smartAlarmDao : smartAlarmDao
    var alarmList : LiveData<List<SmartAlarm>>


    init {
        val database  = SmartAlarmsDatabase.getDatabaseInstance(application)
        smartAlarmDao = database.SmartAlarmDAO()
        alarmList = smartAlarmDao.getAllAlarms()

    }

    suspend fun insertAndReturnId(smartAlarm: SmartAlarm): Long {
        return smartAlarmDao.insert(smartAlarm)


    }

    suspend fun update(smartAlarm: SmartAlarm){
        smartAlarmDao.update(smartAlarm)

    }
    suspend fun delete(smartAlarm: SmartAlarm){
        smartAlarmDao.delete(smartAlarm)

    }
    fun getAllAlarms(): LiveData<List<SmartAlarm>> {

        return alarmList
    }

    suspend fun getAlarmByTime(hour: Int, minute: Int): SmartAlarm? {
        return smartAlarmDao.getAlarmByTime(hour, minute)
    }



}