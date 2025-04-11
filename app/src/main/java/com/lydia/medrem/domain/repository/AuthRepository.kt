package com.lydia.medrem.domain.repository

import com.lydia.medrem.domain.model.Resource
import com.lydia.medrem.domain.model.Response
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun registerUser(email: String, password: String, username: String): Resource<Boolean>
    suspend fun loginUser(email: String, password: String): Resource<Boolean>
    fun signOut()
    fun isUserAuthenticated(): Boolean
    fun getCurrentUserId(): String?
    
    // Add this method to match the implementation in FirebaseAuthRepositoryImpl
    suspend fun signUp(email: String, password: String, username: String): Flow<Response<Boolean>>
}