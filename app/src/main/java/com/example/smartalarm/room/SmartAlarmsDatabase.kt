package com.example.smartalarm.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.smartalarm.model.SmartAlarm

@Database(entities = [SmartAlarm::class], version = 1)
abstract class SmartAlarmsDatabase : RoomDatabase() {

    // DAO function - camelCase
    abstract fun smartAlarmDao(): smartAlarmDao

    companion object {
        @Volatile
        private var instance: SmartAlarmsDatabase? = null

        fun getDatabase(context: Context): SmartAlarmsDatabase {
            return instance ?: synchronized(this) {
                val tempInstance = Room.databaseBuilder(
                    context.applicationContext,
                    SmartAlarmsDatabase::class.java,
                    "smart_alarms"
                ).build()
                instance = tempInstance
                tempInstance
            }
        }
    }
}
