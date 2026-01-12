package com.riadmahi.firebase.auth

import com.riadmahi.firebase.core.FirebaseException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

/**
 * Tests for AuthException to validate API consistency with Firebase Auth errors.
 *
 * Reference: https://firebase.google.com/docs/reference/kotlin/com/google/firebase/auth/FirebaseAuthException
 *
 * Firebase Auth error codes are mapped to typed exceptions for better error handling.
 */
class AuthExceptionTest {

    // region Exception hierarchy

    @Test
    fun `AuthException should extend FirebaseException`() {
        val exception: FirebaseException = AuthException.InvalidCredential()

        assertIs<AuthException>(exception)
    }

    // endregion

    // region Error codes - matching Firebase Auth error codes

    @Test
    fun `InvalidCredential should have INVALID_CREDENTIAL code`() {
        val exception = AuthException.InvalidCredential()

        assertEquals(AuthErrorCode.INVALID_CREDENTIAL, exception.code)
        assertEquals("Invalid credential", exception.message)
    }

    @Test
    fun `UserNotFound should have USER_NOT_FOUND code`() {
        val exception = AuthException.UserNotFound()

        assertEquals(AuthErrorCode.USER_NOT_FOUND, exception.code)
        assertEquals("User not found", exception.message)
    }

    @Test
    fun `WrongPassword should have WRONG_PASSWORD code`() {
        val exception = AuthException.WrongPassword()

        assertEquals(AuthErrorCode.WRONG_PASSWORD, exception.code)
        assertEquals("Wrong password", exception.message)
    }

    @Test
    fun `EmailAlreadyInUse should have EMAIL_ALREADY_IN_USE code`() {
        val exception = AuthException.EmailAlreadyInUse()

        assertEquals(AuthErrorCode.EMAIL_ALREADY_IN_USE, exception.code)
        assertEquals("Email already in use", exception.message)
    }

    @Test
    fun `WeakPassword should have WEAK_PASSWORD code`() {
        val exception = AuthException.WeakPassword()

        assertEquals(AuthErrorCode.WEAK_PASSWORD, exception.code)
        assertEquals("Password is too weak", exception.message)
    }

    @Test
    fun `InvalidEmail should have INVALID_EMAIL code`() {
        val exception = AuthException.InvalidEmail()

        assertEquals(AuthErrorCode.INVALID_EMAIL, exception.code)
        assertEquals("Invalid email address", exception.message)
    }

    @Test
    fun `TooManyRequests should have TOO_MANY_REQUESTS code`() {
        val exception = AuthException.TooManyRequests()

        assertEquals(AuthErrorCode.TOO_MANY_REQUESTS, exception.code)
        assertEquals("Too many requests", exception.message)
    }

    @Test
    fun `UserDisabled should have USER_DISABLED code`() {
        val exception = AuthException.UserDisabled()

        assertEquals(AuthErrorCode.USER_DISABLED, exception.code)
        assertEquals("User has been disabled", exception.message)
    }

    @Test
    fun `RequiresRecentLogin should have REQUIRES_RECENT_LOGIN code`() {
        val exception = AuthException.RequiresRecentLogin()

        assertEquals(AuthErrorCode.REQUIRES_RECENT_LOGIN, exception.code)
        assertEquals("This operation requires recent authentication", exception.message)
    }

    @Test
    fun `CredentialAlreadyInUse should have CREDENTIAL_ALREADY_IN_USE code`() {
        val exception = AuthException.CredentialAlreadyInUse()

        assertEquals(AuthErrorCode.CREDENTIAL_ALREADY_IN_USE, exception.code)
        assertEquals("Credential already in use by another account", exception.message)
    }

    @Test
    fun `OperationNotAllowed should have OPERATION_NOT_ALLOWED code`() {
        val exception = AuthException.OperationNotAllowed()

        assertEquals(AuthErrorCode.OPERATION_NOT_ALLOWED, exception.code)
        assertEquals("Operation not allowed", exception.message)
    }

    @Test
    fun `ExpiredActionCode should have EXPIRED_ACTION_CODE code`() {
        val exception = AuthException.ExpiredActionCode()

        assertEquals(AuthErrorCode.EXPIRED_ACTION_CODE, exception.code)
        assertEquals("Action code has expired", exception.message)
    }

    @Test
    fun `InvalidActionCode should have INVALID_ACTION_CODE code`() {
        val exception = AuthException.InvalidActionCode()

        assertEquals(AuthErrorCode.INVALID_ACTION_CODE, exception.code)
        assertEquals("Invalid action code", exception.message)
    }

    @Test
    fun `NetworkError should have NETWORK_ERROR code`() {
        val exception = AuthException.NetworkError()

        assertEquals(AuthErrorCode.NETWORK_ERROR, exception.code)
        assertEquals("Network error", exception.message)
    }

    @Test
    fun `Unknown should have UNKNOWN code`() {
        val exception = AuthException.Unknown()

        assertEquals(AuthErrorCode.UNKNOWN, exception.code)
        assertEquals("Unknown error", exception.message)
    }

    // endregion

    // region Custom messages

    @Test
    fun `AuthException should accept custom message`() {
        val exception = AuthException.InvalidEmail("Please enter a valid email")

        assertEquals("Please enter a valid email", exception.message)
        assertEquals(AuthErrorCode.INVALID_EMAIL, exception.code)
    }

    @Test
    fun `AuthException should accept cause`() {
        val cause = RuntimeException("Original error")
        val exception = AuthException.NetworkError("Connection failed", cause)

        assertEquals("Connection failed", exception.message)
        assertEquals(cause, exception.cause)
    }

    // endregion

    // region Exhaustive when - ensures all error types are handled

    @Test
    fun `when expression should be exhaustive for AuthException`() {
        val exceptions: List<AuthException> = listOf(
            AuthException.InvalidCredential(),
            AuthException.UserNotFound(),
            AuthException.WrongPassword(),
            AuthException.EmailAlreadyInUse(),
            AuthException.WeakPassword(),
            AuthException.InvalidEmail(),
            AuthException.TooManyRequests(),
            AuthException.UserDisabled(),
            AuthException.RequiresRecentLogin(),
            AuthException.CredentialAlreadyInUse(),
            AuthException.OperationNotAllowed(),
            AuthException.ExpiredActionCode(),
            AuthException.InvalidActionCode(),
            AuthException.NetworkError(),
            AuthException.Unknown()
        )

        exceptions.forEach { exception ->
            val handled = when (exception) {
                is AuthException.InvalidCredential -> true
                is AuthException.UserNotFound -> true
                is AuthException.WrongPassword -> true
                is AuthException.EmailAlreadyInUse -> true
                is AuthException.WeakPassword -> true
                is AuthException.InvalidEmail -> true
                is AuthException.TooManyRequests -> true
                is AuthException.UserDisabled -> true
                is AuthException.RequiresRecentLogin -> true
                is AuthException.CredentialAlreadyInUse -> true
                is AuthException.OperationNotAllowed -> true
                is AuthException.ExpiredActionCode -> true
                is AuthException.InvalidActionCode -> true
                is AuthException.NetworkError -> true
                is AuthException.Unknown -> true
            }
            assertEquals(true, handled, "Exception ${exception::class.simpleName} was not handled")
        }

        assertEquals(15, exceptions.size, "All 15 AuthException types should be tested")
    }

    // endregion

    // region AuthErrorCode enum

    @Test
    fun `AuthErrorCode should have all expected values`() {
        val codes = AuthErrorCode.entries

        assertEquals(15, codes.size)
        assertEquals(AuthErrorCode.INVALID_CREDENTIAL, codes[0])
        assertEquals(AuthErrorCode.USER_NOT_FOUND, codes[1])
        assertEquals(AuthErrorCode.UNKNOWN, codes.last())
    }

    // endregion
}
