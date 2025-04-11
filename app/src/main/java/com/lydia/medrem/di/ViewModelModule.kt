package com.lydia.medrem.di

import androidx.compose.material.ExperimentalMaterialApi
import com.lydia.medrem.data.reminders_db.RemindersDataSource
import com.lydia.medrem.domain.repository.StoreCategoriesRepository
import com.lydia.medrem.ui.screens.reminders.RemindersListViewModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@ExperimentalMaterialApi
@Module
@InstallIn(ViewModelComponent::class)
object ViewModelModule {

    @Provides
    @ViewModelScoped
    fun provideRemindersListViewModel(
        repository: RemindersDataSource,
        storeCategoriesRepository: StoreCategoriesRepository
    ): RemindersListViewModel {
        return RemindersListViewModel(repository, storeCategoriesRepository)
    }
}