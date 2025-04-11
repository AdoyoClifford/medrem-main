@file:OptIn(DelicateCoroutinesApi::class)

package com.lydia.medrem

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.lydia.medrem.data.reminders_db.RemindersDataSource
import com.lydia.medrem.domain.repository.StoreThemeRepository
import com.lydia.medrem.ui.theme.ReemeTheme
import com.lydia.medrem.ui.utils.enums.Theme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import com.lydia.medrem.data.alarm.AlarmScheduler
import com.lydia.medrem.data.notification.NotificationHelper
import androidx.activity.viewModels
import com.lydia.medrem.presentation.viewmodel.MedicationViewModel

@ExperimentalMaterialApi
@ExperimentalFoundationApi
@ExperimentalComposeUiApi
@ExperimentalAnimationApi
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var repository: StoreThemeRepository

    @Inject
    lateinit var remindersDatasource: RemindersDataSource

    @Inject
    lateinit var alarmScheduler: AlarmScheduler

    // Use viewModels() delegate instead of direct injection
    private val medicationViewModel: MedicationViewModel by viewModels()

    var remindersSize: Int? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission granted
        } else {
            // Permission denied
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Permission already granted
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    // Show educational UI explaining why notifications are important
                    // Then request permission
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                else -> {
                    // Directly ask for the permission
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }

    private fun checkAndRequestExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                // For Android 12+, direct the user to the exact alarm permission setting
                Intent().also { intent ->
                    intent.action = android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                    startActivity(intent)
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        installSplashScreen()

        createChannel(
            name = "Reminders",
            channelId = "reminder"
        )

        requestNotificationPermission()
        checkAndRequestExactAlarmPermission()
        
        // Setup test buttons
        setupTestButtons()

        GlobalScope.launch {
            remindersDatasource.getReminders().collect {
                remindersSize = it.size
            }
        }

        setContent {
            val currentTheme = repository.getTheme.collectAsState(initial = Theme.AUTO).value

            val systemTheme = isSystemInDarkTheme()

            ReemeTheme(
                isDarkTheme = when (currentTheme) {
                    Theme.AUTO -> systemTheme
                    Theme.LIGHT -> false
                    Theme.DARK -> true
                }
            ) {
                Navigation(repository = repository)
            }
        }
    }

    private fun createChannel(name: CharSequence, description: String = "", channelId: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, name, importance).apply {
                this.description = description
            }
            val notificationMananger = getSystemService(
                NotificationManager::class.java
            )

            notificationMananger.createNotificationChannel(channel)
        }
    }
    
    private fun testNotification() {
        // Test notification directly
        val notificationHelper = NotificationHelper(this)
        notificationHelper.showMedicationReminder(
            "Test Notification",
            "This is a test notification to check if notifications are working"
        )
    }
    
    private fun setupTestButtons() {
        // Test immediate notification
        testNotification()
        
        // Schedule a test alarm for 30 seconds from now
        alarmScheduler.scheduleTestAlarm()
        
        // Schedule another test alarm for 1 minute from now using the ViewModel
        medicationViewModel.scheduleTestAlarmIn1Minute()
    }
}
