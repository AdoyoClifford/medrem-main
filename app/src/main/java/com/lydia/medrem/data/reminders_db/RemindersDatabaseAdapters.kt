package com.lydia.medrem.data.reminders_db

import androidx.compose.ui.graphics.Color
import com.lydia.medrem.ui.utils.enums.ReminderRepeatTypes
import com.lydia.medrem.ui.utils.longToColor
import com.squareup.sqldelight.ColumnAdapter
import java.util.*

object RemindersDatabaseAdapters {
    val listOfStringsAdapter = object : ColumnAdapter<List<String>, String> {
        override fun decode(databaseValue: String) =
            if (databaseValue.isEmpty()) {
                listOf()
            } else {
                databaseValue.split(",")
            }

        override fun encode(value: List<String>) = value.joinToString(separator = ",")
    }

    val colorAdapter = object : ColumnAdapter<Color, Long> {
        override fun decode(databaseValue: Long): Color = longToColor(databaseValue)

        override fun encode(value: Color): Long = value.value.toLong()
    }

    val repeatAdapter = object : ColumnAdapter<ReminderRepeatTypes, Long> {
        override fun decode(databaseValue: Long): ReminderRepeatTypes =
            when (databaseValue) {
                0L -> ReminderRepeatTypes.ONCE
                1L -> ReminderRepeatTypes.TWICE
                2L -> ReminderRepeatTypes.THRICE
                3L -> ReminderRepeatTypes.FOURFOLD
              //  4L -> ReminderRepeatTypes.YEARLY
                else -> ReminderRepeatTypes.UNSELECTED
            }

        override fun encode(value: ReminderRepeatTypes): Long = value.ordinal.toLong()
    }

    val dateAdapter = object : ColumnAdapter<Date, Long> {
        override fun decode(databaseValue: Long): Date {
            val date = Date()

            date.time = databaseValue

            return date
        }

        override fun encode(value: Date): Long = value.time
    }
}