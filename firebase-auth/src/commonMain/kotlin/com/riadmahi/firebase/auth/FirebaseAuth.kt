package com.riadmahi.firebase.auth

import com.riadmahi.firebase.core.FirebaseApp
import com.riadmahi.firebase.core.FirebaseResult
import kotlinx.coroutines.flow.Flow

/**
 * Entry point for Firebase Authentication.
 */
expect class FirebaseAuth {
    /**
     * Returns the currently signed-in user, or null if no user is signed in.
     */
    val currentUser: FirebaseUser?

    /**
     * A Flow that emits the current user whenever authentication state changes.
     */
    val authStateFlow: Flow<FirebaseUser?>

    /**
     * Signs in with email and password.
     */
    suspend fun signInWithEmailAndPassword(
        email: String,
        password: String
    ): FirebaseResult<AuthResult>

    /**
     * Creates a new user with email and password.
     */
    suspend fun createUserWithEmailAndPassword(
        email: String,
        password: String
    ): FirebaseResult<AuthResult>

    /**
     * Signs in with the given credential.
     */
    suspend fun signInWithCredential(
        credential: AuthCredential
    ): FirebaseResult<AuthResult>

    /**
     * Signs in anonymously.
     */
    suspend fun signInAnonymously(): FirebaseResult<AuthResult>

    /**
     * Signs in with a custom token.
     */
    suspend fun signInWithCustomToken(token: String): FirebaseResult<AuthResult>

    /**
     * Signs out the current user.
     */
    suspend fun signOut(): FirebaseResult<Unit>

    /**
     * Sends a password reset email to the given email address.
     */
    suspend fun sendPasswordResetEmail(email: String): FirebaseResult<Unit>

    /**
     * Confirms a password reset with the given code and new password.
     */
    suspend fun confirmPasswordReset(code: String, newPassword: String): FirebaseResult<Unit>

    /**
     * Configures the auth instance to use the Firebase Auth emulator.
     */
    fun useEmulator(host: String, port: Int)

    companion object {
        /**
         * Returns the FirebaseAuth instance for the default FirebaseApp.
         */
        fun getInstance(): FirebaseAuth

        /**
         * Returns the FirebaseAuth instance for the given FirebaseApp.
         */
        fun getInstance(app: FirebaseApp): FirebaseAuth
    }
}
