package com.lydia.medrem.presentation.viewmodel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lydia.medrem.domain.model.Response
import com.lydia.medrem.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    // ...existing code...

    // SignUp state
    private val _signUpState: MutableState<Response<Boolean>> = mutableStateOf(Response.Success(false))
    val signUpState: State<Response<Boolean>> = _signUpState

    fun signUp(email: String, password: String, username: String) {
        viewModelScope.launch {
            // Reset state to loading at the start
            _signUpState.value = Response.Loading
            
            repository.signUp(email, password, username).onEach { response ->
                _signUpState.value = response
            }.launchIn(viewModelScope)
        }
    }

    // Function to reset the sign-up state (call when leaving the screen or after navigation)
    fun resetSignUpState() {
        _signUpState.value = Response.Success(false)
    }

    // ...existing code...
}