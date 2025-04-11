package com.lydia.medrem.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lydia.medrem.data.alarm.AlarmScheduler
import com.lydia.medrem.data.reminders_db.RemindersDataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.util.Log
import java.util.Calendar

@HiltViewModel
class MedicationViewModel @Inject constructor(
    private val remindersDatasource: RemindersDataSource,
    private val alarmScheduler: AlarmScheduler
) : ViewModel() {

    init {
        // Watch for changes in reminders and reschedule alarms
        viewModelScope.launch {
            remindersDatasource.getReminders().collect { reminders ->
                Log.d("MedicationViewModel", "Reminders changed, count: ${reminders.size}")
                // Reschedule all active reminders
                reminders.forEach { reminder ->
                    try {
                        // Extract time from the reminder's date in a safe way
                        val calendar = Calendar.getInstance()
                        
                        // Handle the date safely regardless of type
                        try {
                            // Just get the time value directly - will work with Long or Date objects
                            val reminderTimeValue = reminder.date?.time ?: System.currentTimeMillis()
                            calendar.timeInMillis = reminderTimeValue
                        } catch (e: Exception) {
                            // If any exception, fall back to current time
                            calendar.timeInMillis = System.currentTimeMillis()
                            Log.e("MedicationViewModel", "Error extracting time from reminder: ${e.message}")
                        }
                        
                        val hour = calendar.get(Calendar.HOUR_OF_DAY)
                        val minute = calendar.get(Calendar.MINUTE)
                        
                        // Calculate next alarm time 
                        val nextTime = calculateNextAlarmTime(hour, minute)
                        
                        scheduleMedicationReminder(
                            medicationId = reminder.id,
                            medicationName = reminder.title,
                            time = nextTime
                        )
                    } catch (e: Exception) {
                        Log.e("MedicationViewModel", "Error scheduling reminder ${reminder.id}: ${e.message}")
                    }
                }
            }
        }
    }
    
    private fun calculateNextAlarmTime(hour: Int, minute: Int): Long {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        // If time is in the past, schedule for tomorrow
        if (calendar.timeInMillis < System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
        
        return calendar.timeInMillis
    }

    fun scheduleMedicationReminder(medicationId: Long, medicationName: String, time: Long) {
        try {
            Log.d("MedicationViewModel", "Scheduling reminder for $medicationName at timestamp $time")
            alarmScheduler.scheduleAlarm(
                medicationId = medicationId,
                title = "Time to take $medicationName",
                message = "Don't forget to take your medication!",
                timeInMillis = time
            )
        } catch (e: Exception) {
            Log.e("MedicationViewModel", "Failed to schedule alarm", e)
        }
    }
    
    fun saveMedication(/* your parameters */) {
        viewModelScope.launch {
            val savedMedicationId = 0L // Replace with actual ID from your save operation
            val medicationName = "Your medication" // Replace with actual name
            val reminderTime = System.currentTimeMillis() + 60000 // Example: 1 minute from now
            
            scheduleMedicationReminder(savedMedicationId, medicationName, reminderTime)
        }
    }

    // Set a test alarm for 10 seconds from now
    fun scheduleTestAlarm() {
        val now = System.currentTimeMillis()
        val testTime = now + 10000 // 10 seconds from now
        scheduleMedicationReminder(999L, "Test Med", testTime)
        Log.d("MedicationViewModel", "Test alarm scheduled for 10 seconds from now")
    }
    
    // Function to test alarm for the next minute
    fun scheduleTestAlarmIn1Minute() {
        val now = System.currentTimeMillis()
        val testTime = now + 60000 // 1 minute from now
        scheduleMedicationReminder(1000L, "Test Reminder", testTime)
        Log.d("MedicationViewModel", "Test alarm scheduled for 1 minute from now: ${testTime}")
    }
}
