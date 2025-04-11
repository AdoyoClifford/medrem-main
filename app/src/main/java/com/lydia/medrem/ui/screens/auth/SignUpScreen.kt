package com.lydia.medrem.ui.screens.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import com.lydia.medrem.domain.model.Response
import com.lydia.medrem.presentation.viewmodel.AuthViewModel

@Composable
fun SignUpScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onSignUpSuccess: () -> Unit,
    navigateToSignIn: () -> Unit,
    // ...other parameters...
) {
    // Get the sign-up state
    val signUpState by remember { viewModel.signUpState }
    
    // Effect to handle successful sign-up
    LaunchedEffect(key1 = signUpState) {
        when (signUpState) {
            is Response.Success -> {
                // Check if sign-up was successful (true) not just initialized (false)
                if ((signUpState as Response.Success<Boolean>).data) {
                    // Reset state before navigating
                    viewModel.resetSignUpState()
                    // Navigate to the next screen
                    onSignUpSuccess()
                }
            }
            is Response.Failure -> {
                // Handle failure - maybe show an error message
                val error = (signUpState as Response.Failure).error
                // Show error message to the user
            }
            else -> { /* Loading state is handled by the UI */ }
        }
    }
    
    // Use DisposableEffect for cleanup when leaving the screen
    DisposableEffect(Unit) {
        onDispose {
            viewModel.resetSignUpState()
        }
    }
    
    // Your existing UI code with loading spinner, form fields, etc.
    // ...existing UI code...
}
