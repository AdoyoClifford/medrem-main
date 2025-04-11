package com.lydia.medrem.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val dataStore: DataStore<Preferences>, // You might have a repository instead
    // ...other dependencies...
) : ViewModel() {
    
    // ...existing code...
    
    // Add this function if it doesn't exist
    fun saveTheme(theme: String) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[stringPreferencesKey("theme")] = theme
            }
        }
    }
    
    // If you're using an enum for theme
    enum class ThemeMode {
        LIGHT, DARK, SYSTEM
    }
    
    // Alternative implementation with enum
    fun saveTheme(theme: ThemeMode) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[stringPreferencesKey("theme")] = theme.name
            }
        }
    }
    
    // ...existing code...
}