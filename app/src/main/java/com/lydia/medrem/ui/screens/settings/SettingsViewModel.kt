package com.lydia.medrem.ui.screens.settings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lydia.medrem.domain.repository.AuthRepository
import com.lydia.medrem.domain.repository.StoreThemeRepository
import com.lydia.medrem.ui.utils.Routes
import com.lydia.medrem.ui.utils.UiEvent
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val storeThemeRepository: StoreThemeRepository,
    private val authRepository: AuthRepository,
    private val firestore: FirebaseFirestore,
) : ViewModel() {

    private var _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    val theme = storeThemeRepository.getTheme

    fun onEvent(event: SettingsEvents) {
        when (event) {
            is SettingsEvents.OnChangeThemeClick -> {
                //saveTheme(event.theme)
            }
            is SettingsEvents.OnNavigationIconClick -> {
                sendUiEvent(UiEvent.PopBackStack)
            }
            is SettingsEvents.SendFeedback -> {
                viewModelScope.launch {
                    try {
                        firestore.collection("feedback")
                            .add(hashMapOf("text" to event.text))
                            .await()
                        Log.d("feedback", "Feedback sent successfully")
                        sendUiEvent(UiEvent.AlertDialog(isOpen = false))
                    } catch (e: Exception) {
                        Log.w("feedback", "Error sending feedback", e)
                    }
                }
            }
            is SettingsEvents.ToggleFeedbackAlert -> {
                sendUiEvent(UiEvent.AlertDialog(isOpen = event.isOpen))
            }
            is SettingsEvents.OnSignOutClick -> {
                viewModelScope.launch {
                    try {
                        authRepository.signOut()
                        sendUiEvent(UiEvent.Navigate(Routes.AUTH))
                    } catch (e: Exception) {
                        Log.e("Settings", "Error signing out", e)
                    }
                }
            }
        }
    }

    private fun sendUiEvent(uiEvent: UiEvent) {
        viewModelScope.launch {
            _uiEvent.emit(uiEvent)
        }
    }

//    private fun saveTheme(theme: Theme) {
//        viewModelScope.launch {
//            storeThemeRepository.saveTheme(theme)
//        }
//    }
}