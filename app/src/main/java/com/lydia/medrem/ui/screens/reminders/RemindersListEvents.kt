package com.lydia.medrem.ui.screens.reminders

import remindersdb.ReminderEntity

sealed class RemindersListEvents {
    object OnSearchClick: RemindersListEvents()
    object OnSettingsClick : RemindersListEvents()
    object OnMetricsClick : RemindersListEvents()
    object OnFilterClick : RemindersListEvents()
    data class OnReminderClick(val reminder: ReminderEntity) : RemindersListEvents()
    object OnAddClick : RemindersListEvents()
}