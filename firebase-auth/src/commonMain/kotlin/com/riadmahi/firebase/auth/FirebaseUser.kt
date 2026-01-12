package com.riadmahi.firebase.auth

import com.riadmahi.firebase.core.FirebaseResult

/**
 * Represents a user in Firebase Authentication.
 */
expect class FirebaseUser {
    /**
     * The unique identifier for this user.
     */
    val uid: String

    /**
     * The user's email address.
     */
    val email: String?

    /**
     * The user's display name.
     */
    val displayName: String?

    /**
     * The URL of the user's profile photo.
     */
    val photoUrl: String?

    /**
     * The user's phone number.
     */
    val phoneNumber: String?

    /**
     * Whether the user is signed in anonymously.
     */
    val isAnonymous: Boolean

    /**
     * Whether the user's email has been verified.
     */
    val isEmailVerified: Boolean

    /**
     * The provider ID for this user (e.g., "firebase", "google.com").
     */
    val providerId: String

    /**
     * Returns additional provider-specific information.
     */
    val providerData: List<UserInfo>

    /**
     * Returns an ID token for the current user.
     */
    suspend fun getIdToken(forceRefresh: Boolean = false): FirebaseResult<String>

    /**
     * Reloads the user profile data from the server.
     */
    suspend fun reload(): FirebaseResult<Unit>

    /**
     * Deletes this user account.
     */
    suspend fun delete(): FirebaseResult<Unit>

    /**
     * Updates the user's profile.
     */
    suspend fun updateProfile(
        displayName: String? = null,
        photoUrl: String? = null
    ): FirebaseResult<Unit>

    /**
     * Updates the user's email address.
     */
    suspend fun updateEmail(email: String): FirebaseResult<Unit>

    /**
     * Updates the user's password.
     */
    suspend fun updatePassword(password: String): FirebaseResult<Unit>

    /**
     * Sends an email verification to the user.
     */
    suspend fun sendEmailVerification(): FirebaseResult<Unit>

    /**
     * Links this user to the given credential.
     */
    suspend fun linkWithCredential(credential: AuthCredential): FirebaseResult<AuthResult>

    /**
     * Unlinks the provider from this user.
     */
    suspend fun unlink(providerId: String): FirebaseResult<FirebaseUser>

    /**
     * Re-authenticates the user with the given credential.
     */
    suspend fun reauthenticate(credential: AuthCredential): FirebaseResult<Unit>
}

/**
 * Represents provider-specific user information.
 */
data class UserInfo(
    val uid: String,
    val providerId: String,
    val email: String?,
    val displayName: String?,
    val photoUrl: String?,
    val phoneNumber: String?
)
