package com.lydia.medrem.data.di

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.lydia.medrem.RemindersDatabase
import com.lydia.medrem.data.repository.datastore.StoreCategoriesRepositoryImpl
import com.lydia.medrem.data.reminders_db.RemindersDataSourceImpl
import com.lydia.medrem.data.reminders_db.RemindersDataSource
import com.lydia.medrem.data.reminders_db.RemindersDatabaseAdapters
import com.lydia.medrem.data.repository.datastore.StoreThemeRepositoryImpl
import com.lydia.medrem.domain.repository.StoreCategoriesRepository
import com.lydia.medrem.domain.repository.StoreThemeRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.squareup.sqldelight.android.AndroidSqliteDriver
import com.squareup.sqldelight.db.SqlDriver
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import remindersdb.ReminderEntity
import javax.inject.Singleton

// Define a singleton preferencesDataStore at the file level
private val Application.dataStore by preferencesDataStore(name = "settings")

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideMoshi(): Moshi =
        Moshi.Builder().addLast(KotlinJsonAdapterFactory())
            .build()

    @Provides
    @Singleton
    fun provideSqlDriver(app: Application): SqlDriver = AndroidSqliteDriver(
        schema = RemindersDatabase.Schema,
        context = app,
        name = "reminders.db"
    )

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideReminderDataSource(driver: SqlDriver): RemindersDataSource =
        RemindersDataSourceImpl(
            db = RemindersDatabase(
                driver = driver, ReminderEntityAdapter = ReminderEntity.Adapter(
                    categoriesAdapter = RemindersDatabaseAdapters.listOfStringsAdapter,
                    repeatAdapter = RemindersDatabaseAdapters.repeatAdapter,
                    colorAdapter = RemindersDatabaseAdapters.colorAdapter,
                    dateAdapter = RemindersDatabaseAdapters.dateAdapter
                )
            )
        )

    @Provides
    @Singleton
    fun provideStoreCategories(app: Application): StoreCategoriesRepository = StoreCategoriesRepositoryImpl(context = app)

    @Provides
    @Singleton
    fun provideTheme(app: Application): StoreThemeRepository = StoreThemeRepositoryImpl(context = app)

    @Provides
    @Singleton
    fun provideDataStore(app: Application): DataStore<Preferences> {
        return app.dataStore
    }
}