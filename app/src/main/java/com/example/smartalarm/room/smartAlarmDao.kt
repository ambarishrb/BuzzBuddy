package com.example.smartalarm.room

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.smartalarm.model.SmartAlarm

@Dao
interface smartAlarmDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(smartAlarm: SmartAlarm): Long

    @Update
    suspend fun update(smartAlarm: SmartAlarm)

    @Delete
    suspend fun delete(smartAlarm: SmartAlarm)

    @Query("SELECT * FROM smart_alarms ORDER BY alarmTime_hour, alarmTime_minute")
    fun getAllAlarms(): LiveData<List<SmartAlarm>>

    @Query("SELECT * FROM smart_alarms WHERE alarmTime_hour = :hour AND alarmTime_minute = :minute LIMIT 1")
    suspend fun getAlarmByTime(hour: Int, minute: Int): SmartAlarm?

    @Query("SELECT * FROM smart_alarms WHERE alarmId = :id LIMIT 1")
    fun getAlarmByIdLive(id: Int): LiveData<SmartAlarm?>

    // Synchronous function needed for BootReceiver
    @Query("SELECT * FROM smart_alarms")
    fun getAllAlarmsSync(): List<SmartAlarm>
}
