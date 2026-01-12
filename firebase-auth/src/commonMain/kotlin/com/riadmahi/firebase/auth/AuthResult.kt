package com.riadmahi.firebase.auth

/**
 * Result of a successful authentication operation.
 */
data class AuthResult(
    /**
     * The authenticated user.
     */
    val user: FirebaseUser,

    /**
     * Additional information about the user.
     */
    val additionalUserInfo: AdditionalUserInfo?
)

/**
 * Additional information about a user after authentication.
 */
data class AdditionalUserInfo(
    /**
     * Whether this is a new user (first sign-in).
     */
    val isNewUser: Boolean,

    /**
     * The provider ID used for authentication.
     */
    val providerId: String?,

    /**
     * The username, if available (e.g., GitHub, Twitter).
     */
    val username: String?,

    /**
     * Additional profile data from the provider.
     */
    val profile: Map<String, Any?>?
)
