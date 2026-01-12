@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.riadmahi.firebase.auth

import cocoapods.FirebaseAuth.FIRAuth
import cocoapods.FirebaseAuth.FIRAuthDataResult
import cocoapods.FirebaseAuth.FIRAuthStateDidChangeListenerHandle
import cocoapods.FirebaseAuth.FIREmailAuthProvider
import cocoapods.FirebaseAuth.FIRGoogleAuthProvider
import cocoapods.FirebaseAuth.FIROAuthProvider
import cocoapods.FirebaseAuth.FIRPhoneAuthProvider
import com.riadmahi.firebase.core.FirebaseApp
import com.riadmahi.firebase.core.FirebaseResult
import com.riadmahi.firebase.core.util.awaitResult
import com.riadmahi.firebase.core.util.awaitVoid
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import platform.Foundation.NSError

/**
 * iOS implementation of FirebaseAuth using Firebase iOS SDK.
 */
actual class FirebaseAuth private constructor(
    internal val ios: FIRAuth
) {
    actual val currentUser: FirebaseUser?
        get() = ios.currentUser()?.let { FirebaseUser(it) }

    actual val authStateFlow: Flow<FirebaseUser?>
        get() = callbackFlow {
            val handle: FIRAuthStateDidChangeListenerHandle? = ios.addAuthStateDidChangeListener { _, user ->
                trySend(user?.let { FirebaseUser(it) })
            }
            awaitClose { handle?.let { ios.removeAuthStateDidChangeListener(it) } }
        }

    actual suspend fun signInWithEmailAndPassword(
        email: String,
        password: String
    ): FirebaseResult<AuthResult> = safeAuthCall {
        val result = awaitResult<FIRAuthDataResult> { callback ->
            ios.signInWithEmail(email, password = password) { authResult, error ->
                callback(authResult, error)
            }
        }
        result.toAuthResult()
    }

    actual suspend fun createUserWithEmailAndPassword(
        email: String,
        password: String
    ): FirebaseResult<AuthResult> = safeAuthCall {
        val result = awaitResult<FIRAuthDataResult> { callback ->
            ios.createUserWithEmail(email, password = password) { authResult, error ->
                callback(authResult, error)
            }
        }
        result.toAuthResult()
    }

    actual suspend fun signInWithCredential(
        credential: AuthCredential
    ): FirebaseResult<AuthResult> = safeAuthCall {
        val firCredential = credential.toIos()
        val result = awaitResult<FIRAuthDataResult> { callback ->
            ios.signInWithCredential(firCredential) { authResult, error ->
                callback(authResult, error)
            }
        }
        result.toAuthResult()
    }

    actual suspend fun signInAnonymously(): FirebaseResult<AuthResult> = safeAuthCall {
        val result = awaitResult<FIRAuthDataResult> { callback ->
            ios.signInAnonymouslyWithCompletion { authResult, error ->
                callback(authResult, error)
            }
        }
        result.toAuthResult()
    }

    actual suspend fun signInWithCustomToken(token: String): FirebaseResult<AuthResult> = safeAuthCall {
        val result = awaitResult<FIRAuthDataResult> { callback ->
            ios.signInWithCustomToken(token) { authResult, error ->
                callback(authResult, error)
            }
        }
        result.toAuthResult()
    }

    actual suspend fun signOut(): FirebaseResult<Unit> = safeAuthCall {
        ios.signOut(null)
    }

    actual suspend fun sendPasswordResetEmail(email: String): FirebaseResult<Unit> = safeAuthCall {
        awaitVoid { callback ->
            ios.sendPasswordResetWithEmail(email) { error ->
                callback(error)
            }
        }
    }

    actual suspend fun confirmPasswordReset(code: String, newPassword: String): FirebaseResult<Unit> = safeAuthCall {
        awaitVoid { callback ->
            ios.confirmPasswordResetWithCode(code, newPassword = newPassword) { error ->
                callback(error)
            }
        }
    }

    actual fun useEmulator(host: String, port: Int) {
        ios.useEmulatorWithHost(host, port.toLong())
    }

    actual companion object {
        actual fun getInstance(): FirebaseAuth {
            val firAuth = FIRAuth.auth()
            return FirebaseAuth(firAuth)
        }

        @Suppress("UNCHECKED_CAST")
        actual fun getInstance(app: FirebaseApp): FirebaseAuth {
            // Use objc interop to pass the FIRApp - both modules use the same underlying type
            val firAuth = FIRAuth.authWithApp(app.ios as objcnames.classes.FIRApp)
            return FirebaseAuth(firAuth)
        }
    }
}

/**
 * Safe wrapper for auth operations that maps exceptions to AuthException.
 */
internal suspend fun <T> safeAuthCall(block: suspend () -> T): FirebaseResult<T> {
    return try {
        FirebaseResult.Success(block())
    } catch (e: Exception) {
        FirebaseResult.Failure(e.toAuthException())
    }
}

/**
 * Convert FIRAuthDataResult to AuthResult.
 */
private fun FIRAuthDataResult.toAuthResult(): AuthResult {
    return AuthResult(
        user = FirebaseUser(this.user()),
        additionalUserInfo = this.additionalUserInfo()?.let {
            AdditionalUserInfo(
                isNewUser = it.newUser(),
                providerId = it.providerID(),
                username = it.username(),
                profile = it.profile() as? Map<String, Any?>
            )
        }
    )
}

/**
 * Convert AuthCredential to FIRAuthCredential.
 */
internal fun AuthCredential.toIos(): cocoapods.FirebaseAuth.FIRAuthCredential {
    return when (this) {
        is AuthCredential.EmailPassword ->
            FIREmailAuthProvider.credentialWithEmail(email, password = password)
        is AuthCredential.Google ->
            FIRGoogleAuthProvider.credentialWithIDToken(idToken, accessToken = accessToken ?: "")
        is AuthCredential.Apple ->
            FIROAuthProvider.appleCredentialWithIDToken(idToken, rawNonce = null, fullName = null)
        is AuthCredential.Phone ->
            FIRPhoneAuthProvider.provider().credentialWithVerificationID(verificationId, verificationCode = smsCode)
        is AuthCredential.Custom ->
            throw IllegalArgumentException("Custom tokens should use signInWithCustomToken")
        is AuthCredential.OAuth -> {
            val provider = FIROAuthProvider.providerWithProviderID(providerId)
            FIROAuthProvider.credentialWithProviderID(
                providerId,
                IDToken = idToken ?: "",
                accessToken = accessToken
            )
        }
    }
}

/**
 * Convert Exception to AuthException.
 */
internal fun Exception.toAuthException(): AuthException {
    val message = this.message ?: "Unknown error"

    // Map common Firebase Auth error codes
    return when {
        message.contains("ERROR_INVALID_EMAIL") || message.contains("17008") ->
            AuthException.InvalidEmail(message, this)
        message.contains("ERROR_WRONG_PASSWORD") || message.contains("17009") ->
            AuthException.WrongPassword(message, this)
        message.contains("ERROR_USER_NOT_FOUND") || message.contains("17011") ->
            AuthException.UserNotFound(message, this)
        message.contains("ERROR_EMAIL_ALREADY_IN_USE") || message.contains("17007") ->
            AuthException.EmailAlreadyInUse(message, this)
        message.contains("ERROR_WEAK_PASSWORD") || message.contains("17026") ->
            AuthException.WeakPassword(message, this)
        message.contains("ERROR_INVALID_CREDENTIAL") || message.contains("17004") ->
            AuthException.InvalidCredential(message, this)
        message.contains("ERROR_USER_DISABLED") || message.contains("17005") ->
            AuthException.UserDisabled(message, this)
        message.contains("ERROR_TOO_MANY_REQUESTS") || message.contains("17010") ->
            AuthException.TooManyRequests(message, this)
        message.contains("ERROR_REQUIRES_RECENT_LOGIN") || message.contains("17014") ->
            AuthException.RequiresRecentLogin(message, this)
        message.contains("ERROR_CREDENTIAL_ALREADY_IN_USE") || message.contains("17025") ->
            AuthException.CredentialAlreadyInUse(message, this)
        message.contains("ERROR_OPERATION_NOT_ALLOWED") || message.contains("17006") ->
            AuthException.OperationNotAllowed(message, this)
        message.contains("ERROR_NETWORK") || message.contains("17020") ->
            AuthException.NetworkError(message, this)
        else -> AuthException.Unknown(message, this)
    }
}
