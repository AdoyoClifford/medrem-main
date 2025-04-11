package com.lydia.medrem.di

import com.lydia.medrem.data.repository.FirebaseAuthRepositoryImpl
import com.lydia.medrem.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    // Remove or comment out this duplicate provider
    /*
    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()
    */

    // Update this method to receive firestore as a parameter instead of providing it
    @Provides
    @Singleton
    fun provideAuthRepository(
        auth: FirebaseAuth,
        firestore: FirebaseFirestore // Inject from AppModule instead of providing here
    ): AuthRepository = FirebaseAuthRepositoryImpl(auth, firestore)
}