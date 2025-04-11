package com.lydia.medrem.data.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.lydia.medrem.data.notification.NotificationHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {
    
    @Inject
    lateinit var notificationHelper: NotificationHelper
    
    @DelicateCoroutinesApi
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("AlarmReceiver", "Received alarm: action=${intent.action}")
        
        // Extract medication data from intent
        val medicationId = intent.getLongExtra("MEDICATION_ID", -1)
        // Get title and add fallback
        val title = intent.getStringExtra("title") ?: "Medication Reminder"
        // Get description and add fallback
        val description = intent.getStringExtra("description") ?: "Time to take your medication!"
        
        Log.d("AlarmReceiver", "Medication alarm received - ID: $medicationId, Title: $title")
        
        // Important: Acquire a wakelock if needed to ensure notification gets shown
        
        // Use GlobalScope since BroadcastReceivers have limited lifetime
        GlobalScope.launch {
            try {
                if (::notificationHelper.isInitialized) {
                    Log.d("AlarmReceiver", "Showing notification via injected helper")
                    // Show a clean title without the ID
                    notificationHelper.showMedicationReminder(
                        "Time to take $title",
                        description
                    )
                } else {
                    Log.e("AlarmReceiver", "NotificationHelper not initialized, using fallback")
                    val helper = NotificationHelper(context)
                    helper.showMedicationReminder(
                        "Time to take $title",
                        description
                    )
                }
                
                // Reschedule for recurring reminders if needed
                // scheduleNextAlarm(context, medicationId, title, message)
            } catch (e: Exception) {
                Log.e("AlarmReceiver", "Error showing notification: ${e.message}", e)
            }
        }
    }
}
