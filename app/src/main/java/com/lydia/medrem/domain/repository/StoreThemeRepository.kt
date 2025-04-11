package com.lydia.medrem.domain.repository

import com.lydia.medrem.ui.utils.enums.Theme
import kotlinx.coroutines.flow.Flow

interface StoreThemeRepository {
    val getTheme: Flow<Theme>

    suspend fun setTheme(theme: Theme)
}