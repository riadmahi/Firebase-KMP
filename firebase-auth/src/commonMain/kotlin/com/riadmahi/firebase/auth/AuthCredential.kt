package com.riadmahi.firebase.auth

/**
 * Represents a credential for authenticating with Firebase.
 */
sealed class AuthCredential {
    /**
     * Email and password credential.
     */
    data class EmailPassword(
        val email: String,
        val password: String
    ) : AuthCredential()

    /**
     * Google Sign-In credential.
     */
    data class Google(
        val idToken: String,
        val accessToken: String? = null
    ) : AuthCredential()

    /**
     * Apple Sign-In credential.
     */
    data class Apple(
        val idToken: String,
        val nonce: String,
        val fullName: AppleFullName? = null
    ) : AuthCredential()

    /**
     * Phone authentication credential.
     */
    data class Phone(
        val verificationId: String,
        val smsCode: String
    ) : AuthCredential()

    /**
     * Custom token credential.
     */
    data class Custom(
        val token: String
    ) : AuthCredential()

    /**
     * OAuth credential for generic providers.
     */
    data class OAuth(
        val providerId: String,
        val idToken: String? = null,
        val accessToken: String? = null
    ) : AuthCredential()
}

/**
 * Represents the full name from Apple Sign-In.
 */
data class AppleFullName(
    val givenName: String?,
    val familyName: String?,
    val middleName: String? = null,
    val namePrefix: String? = null,
    val nameSuffix: String? = null,
    val nickname: String? = null
)
