package com.lydia.medrem.ui.screens.reminders

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lydia.medrem.data.reminders_db.RemindersDataSource
import com.lydia.medrem.domain.repository.StoreCategoriesRepository
import com.lydia.medrem.ui.utils.Routes
import com.lydia.medrem.ui.utils.UiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import remindersdb.ReminderEntity
import java.time.LocalDate
import java.util.*
import javax.inject.Inject

data class AdherenceRecord(
    val date: LocalDate,
    val totalReminders: Int,
    val completedReminders: Int,
    val adherencePercentage: Float
)

@ExperimentalMaterialApi
@HiltViewModel
class RemindersListViewModel @Inject constructor(
    private val repository: RemindersDataSource,
    private val storeCategoriesRepositoryImpl: StoreCategoriesRepository,
) : ViewModel() {

    val reminders = repository.getReminders()
    var isFilterOpened by mutableStateOf(false)

    // Adherence tracking state
    private val _adherenceHistory = MutableStateFlow<List<AdherenceRecord>>(emptyList())
    val adherenceHistory = _adherenceHistory.asStateFlow()

    // Weekly adherence rate
    private val _weeklyAdherenceRate = MutableStateFlow(0f)
    val weeklyAdherenceRate = _weeklyAdherenceRate.asStateFlow()
    
    // Monthly adherence rate
    private val _monthlyAdherenceRate = MutableStateFlow(0f)
    val monthlyAdherenceRate = _monthlyAdherenceRate.asStateFlow()
    
    // Streak of days with high adherence (>= 80%)
    private val _adherenceStreak = MutableStateFlow(0)
    val adherenceStreak = _adherenceStreak.asStateFlow()

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    val categories = storeCategoriesRepositoryImpl.getCategories

    init {
        trackAdherence()
    }

    private fun trackAdherence() {
        viewModelScope.launch {
            reminders.collect { remindersList ->
                calculateDailyAdherence(remindersList)
                calculateWeeklyAdherence()
                calculateMonthlyAdherence()
                updateAdherenceStreak()
            }
        }
    }

    private fun calculateDailyAdherence(remindersList: List<ReminderEntity>) {
        val today = LocalDate.now()
        val calendar = Calendar.getInstance()
        
        val todayReminders = remindersList.filter { reminder ->
            calendar.time = reminder.date
            val reminderDay = calendar.get(Calendar.DAY_OF_MONTH)
            val reminderMonth = calendar.get(Calendar.MONTH)
            val reminderYear = calendar.get(Calendar.YEAR)
            
            reminderDay == today.dayOfMonth &&
            reminderMonth == today.monthValue - 1 &&
            reminderYear == today.year
        }
        
        val total = todayReminders.size
        val completed = todayReminders.count { it.isDone }
        val adherenceRate = if (total > 0) completed.toFloat() / total * 100 else 0f
        
        val newRecord = AdherenceRecord(
            date = today,
            totalReminders = total,
            completedReminders = completed,
            adherencePercentage = adherenceRate
        )
        
        // Update history with today's record
        val currentHistory = _adherenceHistory.value.filter { it.date != today }
        _adherenceHistory.value = currentHistory + newRecord
    }
    
    private fun calculateWeeklyAdherence() {
        val now = LocalDate.now()
        val oneWeekAgo = now.minusWeeks(1)
        
        val weeklyRecords = _adherenceHistory.value.filter { record ->
            record.date.isAfter(oneWeekAgo) || record.date.isEqual(oneWeekAgo)
        }
        
        if (weeklyRecords.isEmpty()) {
            _weeklyAdherenceRate.value = 0f
            return
        }
        
        val totalCompleted = weeklyRecords.sumOf { it.completedReminders }
        val totalReminders = weeklyRecords.sumOf { it.totalReminders }
        
        _weeklyAdherenceRate.value = if (totalReminders > 0) {
            (totalCompleted.toFloat() / totalReminders) * 100
        } else {
            0f
        }
    }
    
    private fun calculateMonthlyAdherence() {
        val now = LocalDate.now()
        val oneMonthAgo = now.minusMonths(1)
        
        val monthlyRecords = _adherenceHistory.value.filter { record ->
            record.date.isAfter(oneMonthAgo) || record.date.isEqual(oneMonthAgo)
        }
        
        if (monthlyRecords.isEmpty()) {
            _monthlyAdherenceRate.value = 0f
            return
        }
        
        val totalCompleted = monthlyRecords.sumOf { it.completedReminders }
        val totalReminders = monthlyRecords.sumOf { it.totalReminders }
        
        _monthlyAdherenceRate.value = if (totalReminders > 0) {
            (totalCompleted.toFloat() / totalReminders) * 100
        } else {
            0f
        }
    }
    
    private fun updateAdherenceStreak() {
        val sortedRecords = _adherenceHistory.value.sortedByDescending { it.date }
        var streakCount = 0
        
        for (record in sortedRecords) {
            if (record.adherencePercentage >= 80f && record.totalReminders > 0) {
                streakCount++
            } else {
                break
            }
        }
        
        _adherenceStreak.value = streakCount
    }

    // Generate personalized feedback based on adherence data
    fun generateFeedback(): String {
        val currentAdherence = _adherenceHistory.value.lastOrNull()?.adherencePercentage ?: 0f
        val streak = _adherenceStreak.value
        val weeklyRate = _weeklyAdherenceRate.value
        
        return when {
            currentAdherence == 100f && streak >= 7 -> 
                "Amazing! Perfect adherence for ${streak} days straight! Your consistent medication routine is helping you stay at your healthiest."
            
            currentAdherence >= 90f ->
                "Great job! Your adherence rate is excellent at ${currentAdherence.toInt()}%. Keep up the good work!"
            
            currentAdherence >= 80f ->
                "Good adherence at ${currentAdherence.toInt()}%. You're on the right track to better health outcomes."
            
            currentAdherence >= 60f ->
                "Your adherence is at ${currentAdherence.toInt()}%. Try to take your medications more consistently to improve your treatment effectiveness."
            
            weeklyRate < 50f ->
                "Your weekly adherence is below 50%. Setting reminders and creating a routine can help improve your medication schedule."
            
            else ->
                "Your adherence is at ${currentAdherence.toInt()}%. Regular medication intake is crucial for effective treatment. Let's work on improving this together."
        }
    }

    // Update reminder completion status and recalculate adherence
    fun updateAdherenceOnReminderChange() {
        viewModelScope.launch {
            val remindersList = reminders.first()
            calculateDailyAdherence(remindersList)
            calculateWeeklyAdherence()
            calculateMonthlyAdherence()
            updateAdherenceStreak()
        }
    }

    fun onEvent(event: RemindersListEvents) {
        when (event) {
            is RemindersListEvents.OnReminderClick -> {
                sendUiEvent(
                    UiEvent.Navigate(
                        Routes.EDIT_REMINDER.replace(
                            "{reminderId}",
                            event.reminder.id.toString()
                        )
                    )
                )
            }
            is RemindersListEvents.OnAddClick -> {
                sendUiEvent(UiEvent.Navigate(Routes.CREATE_REMINDER))
            }
            is RemindersListEvents.OnSearchClick -> {
                sendUiEvent(UiEvent.BackDropScaffold)
            }
            is RemindersListEvents.OnFilterClick -> {
                isFilterOpened = !isFilterOpened
            }
            is RemindersListEvents.OnSettingsClick -> {
                sendUiEvent(UiEvent.Navigate(Routes.SETTINGS))
            }
            RemindersListEvents.OnMetricsClick -> {
                sendUiEvent(UiEvent.Navigate(Routes.METRICS))
            }
        }
    }

    private fun sendUiEvent(event: UiEvent) {
        viewModelScope.launch {
            _uiEvent.emit(event)
        }
    }
}