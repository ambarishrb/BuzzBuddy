package com.example.smartalarm.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.smartalarm.model.SmartAlarm

@Database (entities = [SmartAlarm::class], version = 1)
abstract class SmartAlarmsDatabase : RoomDatabase() {

    abstract fun SmartAlarmDAO(): smartAlarmDao


    companion object{

        @Volatile
        private var instance : SmartAlarmsDatabase? = null

        fun getDatabaseInstance(context: Context): SmartAlarmsDatabase{

            synchronized(this){

                if (instance == null){

                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        SmartAlarmsDatabase::class.java,
                        "smart_alarms"


                    ).build()
                }

            }
            return instance as SmartAlarmsDatabase
        }

    }

}