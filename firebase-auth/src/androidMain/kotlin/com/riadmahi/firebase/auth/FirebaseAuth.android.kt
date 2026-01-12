package com.riadmahi.firebase.auth

import com.google.firebase.auth.FirebaseAuth as AndroidFirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthProvider
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.EmailAuthProvider
import com.riadmahi.firebase.core.FirebaseApp
import com.riadmahi.firebase.core.FirebaseResult
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

actual class FirebaseAuth private constructor(
    private val android: AndroidFirebaseAuth
) {
    actual val currentUser: FirebaseUser?
        get() = android.currentUser?.let { FirebaseUser(it) }

    actual val authStateFlow: Flow<FirebaseUser?>
        get() = callbackFlow {
            val listener = AndroidFirebaseAuth.AuthStateListener { auth ->
                trySend(auth.currentUser?.let { FirebaseUser(it) })
            }
            android.addAuthStateListener(listener)
            awaitClose { android.removeAuthStateListener(listener) }
        }

    actual suspend fun signInWithEmailAndPassword(
        email: String,
        password: String
    ): FirebaseResult<AuthResult> = safeCall {
        val result = android.signInWithEmailAndPassword(email, password).await()
        result.toAuthResult()
    }

    actual suspend fun createUserWithEmailAndPassword(
        email: String,
        password: String
    ): FirebaseResult<AuthResult> = safeCall {
        val result = android.createUserWithEmailAndPassword(email, password).await()
        result.toAuthResult()
    }

    actual suspend fun signInWithCredential(
        credential: AuthCredential
    ): FirebaseResult<AuthResult> = safeCall {
        val androidCredential = credential.toAndroid()
        val result = android.signInWithCredential(androidCredential).await()
        result.toAuthResult()
    }

    actual suspend fun signInAnonymously(): FirebaseResult<AuthResult> = safeCall {
        val result = android.signInAnonymously().await()
        result.toAuthResult()
    }

    actual suspend fun signInWithCustomToken(token: String): FirebaseResult<AuthResult> = safeCall {
        val result = android.signInWithCustomToken(token).await()
        result.toAuthResult()
    }

    actual suspend fun signOut(): FirebaseResult<Unit> = safeCall {
        android.signOut()
    }

    actual suspend fun sendPasswordResetEmail(email: String): FirebaseResult<Unit> = safeCall {
        android.sendPasswordResetEmail(email).await()
        Unit
    }

    actual suspend fun confirmPasswordReset(code: String, newPassword: String): FirebaseResult<Unit> = safeCall {
        android.confirmPasswordReset(code, newPassword).await()
        Unit
    }

    actual fun useEmulator(host: String, port: Int) {
        android.useEmulator(host, port)
    }

    actual companion object {
        actual fun getInstance(): FirebaseAuth {
            return FirebaseAuth(AndroidFirebaseAuth.getInstance())
        }

        actual fun getInstance(app: FirebaseApp): FirebaseAuth {
            return FirebaseAuth(AndroidFirebaseAuth.getInstance(app.android))
        }
    }
}

internal suspend fun <T> safeCall(block: suspend () -> T): FirebaseResult<T> {
    return try {
        FirebaseResult.Success(block())
    } catch (e: FirebaseAuthException) {
        FirebaseResult.Failure(e.toAuthException())
    } catch (e: Exception) {
        FirebaseResult.Failure(AuthException.Unknown(e.message ?: "Unknown error", e))
    }
}

private fun com.google.firebase.auth.AuthResult.toAuthResult(): AuthResult {
    return AuthResult(
        user = FirebaseUser(user!!),
        additionalUserInfo = additionalUserInfo?.let {
            AdditionalUserInfo(
                isNewUser = it.isNewUser,
                providerId = it.providerId,
                username = it.username,
                profile = it.profile
            )
        }
    )
}

internal fun AuthCredential.toAndroid(): com.google.firebase.auth.AuthCredential {
    return when (this) {
        is AuthCredential.EmailPassword -> EmailAuthProvider.getCredential(email, password)
        is AuthCredential.Google -> GoogleAuthProvider.getCredential(idToken, accessToken)
        is AuthCredential.Apple -> OAuthProvider.newCredentialBuilder("apple.com")
            .setIdToken(idToken)
            .build()
        is AuthCredential.Phone -> PhoneAuthProvider.getCredential(verificationId, smsCode)
        is AuthCredential.Custom -> throw IllegalArgumentException("Custom tokens should use signInWithCustomToken")
        is AuthCredential.OAuth -> {
            val builder = OAuthProvider.newCredentialBuilder(providerId)
            idToken?.let { builder.setIdToken(it) }
            accessToken?.let { builder.setAccessToken(it) }
            builder.build()
        }
    }
}

internal fun FirebaseAuthException.toAuthException(): AuthException {
    return when (errorCode) {
        "ERROR_INVALID_CREDENTIAL" -> AuthException.InvalidCredential(message ?: "Invalid credential", this)
        "ERROR_USER_NOT_FOUND" -> AuthException.UserNotFound(message ?: "User not found", this)
        "ERROR_WRONG_PASSWORD" -> AuthException.WrongPassword(message ?: "Wrong password", this)
        "ERROR_EMAIL_ALREADY_IN_USE" -> AuthException.EmailAlreadyInUse(message ?: "Email already in use", this)
        "ERROR_WEAK_PASSWORD" -> AuthException.WeakPassword(message ?: "Weak password", this)
        "ERROR_INVALID_EMAIL" -> AuthException.InvalidEmail(message ?: "Invalid email", this)
        "ERROR_TOO_MANY_REQUESTS" -> AuthException.TooManyRequests(message ?: "Too many requests", this)
        "ERROR_USER_DISABLED" -> AuthException.UserDisabled(message ?: "User disabled", this)
        "ERROR_REQUIRES_RECENT_LOGIN" -> AuthException.RequiresRecentLogin(message ?: "Requires recent login", this)
        "ERROR_CREDENTIAL_ALREADY_IN_USE" -> AuthException.CredentialAlreadyInUse(message ?: "Credential already in use", this)
        "ERROR_OPERATION_NOT_ALLOWED" -> AuthException.OperationNotAllowed(message ?: "Operation not allowed", this)
        "ERROR_EXPIRED_ACTION_CODE" -> AuthException.ExpiredActionCode(message ?: "Expired action code", this)
        "ERROR_INVALID_ACTION_CODE" -> AuthException.InvalidActionCode(message ?: "Invalid action code", this)
        "ERROR_NETWORK_REQUEST_FAILED" -> AuthException.NetworkError(message ?: "Network error", this)
        else -> AuthException.Unknown(message ?: "Unknown error", this)
    }
}
