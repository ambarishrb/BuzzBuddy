package com.example.smartalarm.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.example.smartalarm.model.SmartAlarm
import com.example.smartalarm.repository.SmartAlarmRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class SmartAlarmViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SmartAlarmRepository = SmartAlarmRepository(application)
    val alarmList: LiveData<List<SmartAlarm>> = repository.getAllAlarms()

    fun getAllAlarms(): LiveData<List<SmartAlarm>> = alarmList

    suspend fun insertAndReturnId(smartAlarm: SmartAlarm): Long {
        return withContext(Dispatchers.IO) {
            repository.insertAndReturnId(smartAlarm)
        }
    }

    fun update(smartAlarm: SmartAlarm) = viewModelScope.launch(Dispatchers.IO) {
        repository.update(smartAlarm)
    }

    fun getAlarmById(alarmId: Int): LiveData<SmartAlarm?> {
        return repository.getAlarmById(alarmId)
    }




    fun delete(smartAlarm: SmartAlarm) = viewModelScope.launch(Dispatchers.IO) {
        repository.delete(smartAlarm)
    }
    suspend fun getAlarmByTime(hour: Int, minute: Int): SmartAlarm? {return withContext(Dispatchers.IO){repository.getAlarmByTime(hour,minute)

    }

    }

}
