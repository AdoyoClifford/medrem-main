package com.lydia.medrem.domain.model

import androidx.compose.ui.graphics.Color
import com.lydia.medrem.ui.utils.enums.ReminderRepeatTypes
import java.util.*

data class Reminder(
    val id: Long? = null,
    val title: String,
    val description: String? = null,
    val repeat: ReminderRepeatTypes,
    val isPinned: Boolean,
    val isDone: Boolean,
    val color: Color,
    val date: Calendar,
    val categories: List<String>?
)
