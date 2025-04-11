package com.lydia.medrem.ui.screens.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lydia.medrem.domain.model.Resource
import com.lydia.medrem.domain.repository.AuthRepository
import com.lydia.medrem.ui.utils.Routes
import com.lydia.medrem.ui.utils.UiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private var _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    var email by mutableStateOf("")
        private set

    var password by mutableStateOf("")
        private set

    var confirmPassword by mutableStateOf("")
        private set

    var username by mutableStateOf("")
        private set

    var isLoginMode by mutableStateOf(true)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set
        
    init {
        // Check if user is already authenticated
        if (authRepository.isUserAuthenticated()) {
            viewModelScope.launch {
                sendUiEvent(UiEvent.Navigate(Routes.REMINDERS))
            }
        }
    }

    fun onEvent(event: AuthEvents) {
        when (event) {
            is AuthEvents.OnEmailChange -> {
                email = event.email
                clearErrorMessage()
            }
            is AuthEvents.OnPasswordChange -> {
                password = event.password
                clearErrorMessage()
            }
            is AuthEvents.OnConfirmPasswordChange -> {
                confirmPassword = event.confirmPassword
                clearErrorMessage()
            }
            is AuthEvents.OnUsernameChange -> {
                username = event.username
                clearErrorMessage()
            }
            is AuthEvents.OnLoginClick -> {
                login()
            }
            is AuthEvents.OnSignUpClick -> {
                signUp()
            }
            is AuthEvents.OnNavigateToSignUp -> {
                isLoginMode = false
                clearFields()
            }
            is AuthEvents.OnNavigateToLogin -> {
                isLoginMode = true
                clearFields()
            }
            is AuthEvents.OnNavigationIconClick -> {
                sendUiEvent(UiEvent.PopBackStack)
            }
        }
    }

    private fun clearFields() {
        email = ""
        password = ""
        confirmPassword = ""
        username = ""
        clearErrorMessage()
    }
    
    private fun clearErrorMessage() {
        errorMessage = null
    }

    private fun login() {
        if (!validateLoginInputs()) return
        
        isLoading = true
        viewModelScope.launch {
            when (val result = authRepository.loginUser(email, password)) {
                is Resource.Success -> {
                    isLoading = false
                    sendUiEvent(UiEvent.Navigate(Routes.REMINDERS))
                }
                is Resource.Error -> {
                    isLoading = false
                    errorMessage = result.message
                }
                is Resource.Loading -> {
                    isLoading = true
                }
            }
        }
    }

    private fun signUp() {
        if (!validateSignupInputs()) return
        
        isLoading = true
        viewModelScope.launch {
            when (val result = authRepository.registerUser(email, password, username)) {
                is Resource.Success -> {
                    isLoading = false
                    sendUiEvent(UiEvent.Navigate(Routes.REMINDERS))
                }
                is Resource.Error -> {
                    isLoading = false
                    errorMessage = result.message
                }
                is Resource.Loading -> {
                    isLoading = true
                }
            }
        }
    }

    private fun validateLoginInputs(): Boolean {
        return when {
            email.isBlank() -> {
                errorMessage = "Email cannot be empty"
                false
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                errorMessage = "Please enter a valid email address"
                false
            }
            password.isBlank() -> {
                errorMessage = "Password cannot be empty"
                false
            }
            password.length < 6 -> {
                errorMessage = "Password must be at least 6 characters long"
                false
            }
            else -> true
        }
    }

    private fun validateSignupInputs(): Boolean {
        return when {
            username.isBlank() -> {
                errorMessage = "Username cannot be empty"
                false
            }
            email.isBlank() -> {
                errorMessage = "Email cannot be empty"
                false
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                errorMessage = "Please enter a valid email address"
                false
            }
            password.isBlank() -> {
                errorMessage = "Password cannot be empty"
                false
            }
            password.length < 6 -> {
                errorMessage = "Password must be at least 6 characters long"
                false
            }
            confirmPassword != password -> {
                errorMessage = "Passwords do not match"
                false
            }
            else -> true
        }
    }

    private fun sendUiEvent(event: UiEvent) {
        viewModelScope.launch {
            _uiEvent.emit(event)
        }
    }
}