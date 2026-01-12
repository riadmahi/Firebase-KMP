package com.riadmahi.firebase.auth

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertIs

/**
 * Tests for AuthCredential to validate API consistency with Firebase Auth.
 *
 * Reference: https://firebase.google.com/docs/reference/kotlin/com/google/firebase/auth/AuthCredential
 *
 * Firebase supports multiple credential types:
 * - EmailAuthProvider.getCredential(email, password)
 * - GoogleAuthProvider.getCredential(idToken, accessToken)
 * - OAuthProvider.newCredentialBuilder(providerId)
 * - PhoneAuthProvider.getCredential(verificationId, code)
 */
class AuthCredentialTest {

    // region EmailPassword - matches EmailAuthProvider

    @Test
    fun `EmailPassword credential should contain email and password`() {
        val credential = AuthCredential.EmailPassword(
            email = "user@example.com",
            password = "password123"
        )

        assertEquals("user@example.com", credential.email)
        assertEquals("password123", credential.password)
    }

    @Test
    fun `EmailPassword should be an AuthCredential`() {
        val credential: AuthCredential = AuthCredential.EmailPassword(
            email = "user@example.com",
            password = "password123"
        )

        assertIs<AuthCredential.EmailPassword>(credential)
    }

    // endregion

    // region Google - matches GoogleAuthProvider

    @Test
    fun `Google credential should require idToken`() {
        val credential = AuthCredential.Google(
            idToken = "google-id-token-123"
        )

        assertEquals("google-id-token-123", credential.idToken)
        assertNull(credential.accessToken)
    }

    @Test
    fun `Google credential should accept optional accessToken`() {
        val credential = AuthCredential.Google(
            idToken = "google-id-token-123",
            accessToken = "google-access-token-456"
        )

        assertEquals("google-id-token-123", credential.idToken)
        assertEquals("google-access-token-456", credential.accessToken)
    }

    // endregion

    // region Apple - matches OAuthProvider for Apple

    @Test
    fun `Apple credential should require idToken and nonce`() {
        val credential = AuthCredential.Apple(
            idToken = "apple-id-token-123",
            nonce = "random-nonce-456"
        )

        assertEquals("apple-id-token-123", credential.idToken)
        assertEquals("random-nonce-456", credential.nonce)
        assertNull(credential.fullName)
    }

    @Test
    fun `Apple credential should accept optional fullName`() {
        val fullName = AppleFullName(
            givenName = "John",
            familyName = "Doe"
        )

        val credential = AuthCredential.Apple(
            idToken = "apple-id-token-123",
            nonce = "random-nonce-456",
            fullName = fullName
        )

        assertEquals("John", credential.fullName?.givenName)
        assertEquals("Doe", credential.fullName?.familyName)
    }

    // endregion

    // region Phone - matches PhoneAuthProvider

    @Test
    fun `Phone credential should require verificationId and smsCode`() {
        val credential = AuthCredential.Phone(
            verificationId = "verification-id-123",
            smsCode = "123456"
        )

        assertEquals("verification-id-123", credential.verificationId)
        assertEquals("123456", credential.smsCode)
    }

    // endregion

    // region Custom - matches custom token auth

    @Test
    fun `Custom credential should contain token`() {
        val credential = AuthCredential.Custom(
            token = "custom-jwt-token-123"
        )

        assertEquals("custom-jwt-token-123", credential.token)
    }

    // endregion

    // region OAuth - matches OAuthProvider for generic providers

    @Test
    fun `OAuth credential should require providerId`() {
        val credential = AuthCredential.OAuth(
            providerId = "github.com"
        )

        assertEquals("github.com", credential.providerId)
        assertNull(credential.idToken)
        assertNull(credential.accessToken)
    }

    @Test
    fun `OAuth credential should accept idToken and accessToken`() {
        val credential = AuthCredential.OAuth(
            providerId = "github.com",
            idToken = "github-id-token",
            accessToken = "github-access-token"
        )

        assertEquals("github.com", credential.providerId)
        assertEquals("github-id-token", credential.idToken)
        assertEquals("github-access-token", credential.accessToken)
    }

    @Test
    fun `OAuth should support various providers`() {
        val githubCredential = AuthCredential.OAuth(providerId = "github.com")
        val twitterCredential = AuthCredential.OAuth(providerId = "twitter.com")
        val facebookCredential = AuthCredential.OAuth(providerId = "facebook.com")
        val microsoftCredential = AuthCredential.OAuth(providerId = "microsoft.com")

        assertEquals("github.com", githubCredential.providerId)
        assertEquals("twitter.com", twitterCredential.providerId)
        assertEquals("facebook.com", facebookCredential.providerId)
        assertEquals("microsoft.com", microsoftCredential.providerId)
    }

    // endregion

    // region Sealed class behavior

    @Test
    fun `AuthCredential should be a sealed class with all subtypes`() {
        val credentials: List<AuthCredential> = listOf(
            AuthCredential.EmailPassword("email", "pass"),
            AuthCredential.Google("token"),
            AuthCredential.Apple("token", "nonce"),
            AuthCredential.Phone("verificationId", "code"),
            AuthCredential.Custom("token"),
            AuthCredential.OAuth("provider")
        )

        assertEquals(6, credentials.size)
    }

    @Test
    fun `when expression should be exhaustive for AuthCredential`() {
        val credential: AuthCredential = AuthCredential.Google("token")

        val type = when (credential) {
            is AuthCredential.EmailPassword -> "email"
            is AuthCredential.Google -> "google"
            is AuthCredential.Apple -> "apple"
            is AuthCredential.Phone -> "phone"
            is AuthCredential.Custom -> "custom"
            is AuthCredential.OAuth -> "oauth"
        }

        assertEquals("google", type)
    }

    // endregion
}
