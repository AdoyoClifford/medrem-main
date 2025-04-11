package com.lydia.medrem.data.repository

import com.lydia.medrem.domain.model.Resource
import com.lydia.medrem.domain.model.Response
import com.lydia.medrem.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseAuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {
    override suspend fun registerUser(
        email: String,
        password: String,
        username: String
    ): Resource<Boolean> {
        return try {
            // Create the user with email and password
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val user = authResult.user
            
            // Update profile with username
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(username)
                .build()
            
            user?.updateProfile(profileUpdates)?.await()
            
            // Store additional user data in Firestore
            user?.uid?.let { uid ->
                val userData = hashMapOf(
                    "userId" to uid,
                    "email" to email,
                    "username" to username,
                    "createdAt" to System.currentTimeMillis()
                )
                
                firestore.collection("users")
                    .document(uid)
                    .set(userData)
                    .await()
            }
            
            Resource.Success(true)
        } catch (e: Exception) {
            val errorMessage = when (e) {
                is FirebaseAuthWeakPasswordException -> "Password too weak. Please use at least 6 characters."
                is FirebaseAuthInvalidCredentialsException -> "Invalid email format."
                is FirebaseAuthUserCollisionException -> "User with this email already exists."
                else -> e.message ?: "Registration failed. Please try again."
            }
            Resource.Error(errorMessage)
        }
    }
    
    override suspend fun loginUser(email: String, password: String): Resource<Boolean> {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            Resource.Success(true)
        } catch (e: Exception) {
            val errorMessage = when (e) {
                is FirebaseAuthInvalidUserException -> "User does not exist."
                is FirebaseAuthInvalidCredentialsException -> "Invalid email or password."
                else -> e.message ?: "Login failed. Please try again."
            }
            Resource.Error(errorMessage)
        }
    }

    override fun signOut() {
        auth.signOut()
    }

    override fun isUserAuthenticated(): Boolean {
        return auth.currentUser != null
    }

    override fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    override suspend fun signUp(
        email: String,
        password: String,
        username: String
    ): Flow<Response<Boolean>> = callbackFlow {
        trySend(Response.Loading) // Send loading state
        
        try {
            // Create the user with email and password
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            
            // If successful, save user details in Firestore
            authResult.user?.let { user ->
                val userData = hashMapOf(
                    "username" to username,
                    "email" to email,
                    "uid" to user.uid
                )
                
                // Add the user to Firestore
                firestore.collection("users")
                    .document(user.uid)
                    .set(userData)
                    .await()
                
                // Send success response
                trySend(Response.Success(true))
            } ?: run {
                // If user is null, send failure
                trySend(Response.Failure(Exception("Sign up failed: User is null")))
            }
        } catch (e: Exception) {
            // Send any exceptions as failure
            trySend(Response.Failure(e))
        }
        
        awaitClose()
    }
}