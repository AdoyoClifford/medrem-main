package com.lydia.medrem.data.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.lydia.medrem.data.receiver.AlarmReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleAlarm(medicationId: Long, title: String, message: String, timeInMillis: Long) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("MEDICATION_ID", medicationId)
            putExtra("TITLE", title)
            putExtra("MESSAGE", message)
            // Add a unique action to ensure intent is unique
            action = "com.atitienei_daniel.reeme.MEDICATION_ALARM_$medicationId"
        }

        // Create a unique request code based on medicationId
        val requestCode = medicationId.toInt() 
        
        // Cancel any existing alarms for this medication
        cancelAlarm(medicationId)
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    Log.d("AlarmScheduler", "Scheduling exact alarm for $title at $timeInMillis, requestCode=$requestCode")
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        timeInMillis,
                        pendingIntent
                    )
                } else {
                    Log.w("AlarmScheduler", "Cannot schedule exact alarms, using inexact alarm")
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        timeInMillis,
                        pendingIntent
                    )
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Log.d("AlarmScheduler", "Using setExactAndAllowWhileIdle for Android M+")
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    timeInMillis,
                    pendingIntent
                )
            } else {
                Log.d("AlarmScheduler", "Using setExact for Android pre-M")
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    timeInMillis,
                    pendingIntent
                )
            }
            Log.d("AlarmScheduler", "Alarm scheduled successfully for $title at $timeInMillis")
        } catch (e: Exception) {
            Log.e("AlarmScheduler", "Error scheduling alarm for $title: ${e.message}", e)
        }
    }
    
    fun cancelAlarm(medicationId: Long) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = "com.atitienei_daniel.reeme.MEDICATION_ALARM_$medicationId"
        }
        try {
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                medicationId.toInt(),
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
                Log.d("AlarmScheduler", "Alarm canceled for medication ID: $medicationId")
            }
        } catch (e: Exception) {
            Log.e("AlarmScheduler", "Error canceling alarm: ${e.message}")
        }
    }
    
    fun scheduleTestAlarm() {
        val timeInMillis = System.currentTimeMillis() + 30000 // 30 seconds from now
        scheduleAlarm(
            medicationId = 9999L,
            title = "Test Alarm",
            message = "This is a test alarm triggered at ${System.currentTimeMillis()}",
            timeInMillis = timeInMillis
        )
    }
}
