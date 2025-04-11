package com.lydia.medrem.ui.screens.settings

import com.lydia.medrem.ui.utils.enums.Theme

sealed class SettingsEvents {
    object OnNavigationIconClick : SettingsEvents()
    data class OnChangeThemeClick(val theme: Theme) : SettingsEvents()
    data class ToggleFeedbackAlert(val isOpen: Boolean) : SettingsEvents()
    data class SendFeedback(val text: String) : SettingsEvents()
    object OnSignOutClick : SettingsEvents()
}