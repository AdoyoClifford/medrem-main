package com.lydia.medrem.ui.screens.auth

sealed class AuthEvents {
    data class OnEmailChange(val email: String) : AuthEvents()
    data class OnPasswordChange(val password: String) : AuthEvents()
    data class OnConfirmPasswordChange(val confirmPassword: String) : AuthEvents()
    data class OnUsernameChange(val username: String) : AuthEvents()
    object OnLoginClick : AuthEvents()
    object OnSignUpClick : AuthEvents()
    object OnNavigateToSignUp : AuthEvents()
    object OnNavigateToLogin : AuthEvents()
    object OnNavigationIconClick : AuthEvents()
}