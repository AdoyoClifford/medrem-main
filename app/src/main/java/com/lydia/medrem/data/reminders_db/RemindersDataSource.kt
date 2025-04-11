package com.lydia.medrem.data.reminders_db

import com.lydia.medrem.domain.model.Reminder
import kotlinx.coroutines.flow.Flow
import remindersdb.ReminderEntity

interface RemindersDataSource {

    suspend fun getReminderById(id: Long): ReminderEntity?

    fun getReminders(): Flow<List<ReminderEntity>>

    suspend fun insertReminder(reminder: Reminder)

    suspend fun deleteReminderById(id: Long)

    suspend fun updateReminder(reminder: ReminderEntity)
}