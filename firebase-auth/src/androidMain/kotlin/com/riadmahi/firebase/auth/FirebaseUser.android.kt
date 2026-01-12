package com.riadmahi.firebase.auth

import android.net.Uri
import com.google.firebase.auth.FirebaseUser as AndroidFirebaseUser
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.UserProfileChangeRequest
import com.riadmahi.firebase.core.FirebaseResult
import kotlinx.coroutines.tasks.await

actual class FirebaseUser internal constructor(
    internal val android: AndroidFirebaseUser
) {
    actual val uid: String
        get() = android.uid

    actual val email: String?
        get() = android.email

    actual val displayName: String?
        get() = android.displayName

    actual val photoUrl: String?
        get() = android.photoUrl?.toString()

    actual val phoneNumber: String?
        get() = android.phoneNumber

    actual val isAnonymous: Boolean
        get() = android.isAnonymous

    actual val isEmailVerified: Boolean
        get() = android.isEmailVerified

    actual val providerId: String
        get() = android.providerId

    actual val providerData: List<UserInfo>
        get() = android.providerData.map { info ->
            UserInfo(
                uid = info.uid,
                providerId = info.providerId,
                email = info.email,
                displayName = info.displayName,
                photoUrl = info.photoUrl?.toString(),
                phoneNumber = info.phoneNumber
            )
        }

    actual suspend fun getIdToken(forceRefresh: Boolean): FirebaseResult<String> = safeCall {
        val result = android.getIdToken(forceRefresh).await()
        result.token ?: throw AuthException.Unknown("Failed to get ID token")
    }

    actual suspend fun reload(): FirebaseResult<Unit> = safeCall {
        android.reload().await()
        Unit
    }

    actual suspend fun delete(): FirebaseResult<Unit> = safeCall {
        android.delete().await()
        Unit
    }

    actual suspend fun updateProfile(
        displayName: String?,
        photoUrl: String?
    ): FirebaseResult<Unit> = safeCall {
        val request = UserProfileChangeRequest.Builder().apply {
            displayName?.let { setDisplayName(it) }
            photoUrl?.let { setPhotoUri(Uri.parse(it)) }
        }.build()
        android.updateProfile(request).await()
        Unit
    }

    actual suspend fun updateEmail(email: String): FirebaseResult<Unit> = safeCall {
        android.verifyBeforeUpdateEmail(email).await()
        Unit
    }

    actual suspend fun updatePassword(password: String): FirebaseResult<Unit> = safeCall {
        android.updatePassword(password).await()
        Unit
    }

    actual suspend fun sendEmailVerification(): FirebaseResult<Unit> = safeCall {
        android.sendEmailVerification().await()
        Unit
    }

    actual suspend fun linkWithCredential(credential: AuthCredential): FirebaseResult<AuthResult> = safeCall {
        val result = android.linkWithCredential(credential.toAndroid()).await()
        AuthResult(
            user = FirebaseUser(result.user!!),
            additionalUserInfo = result.additionalUserInfo?.let {
                AdditionalUserInfo(
                    isNewUser = it.isNewUser,
                    providerId = it.providerId,
                    username = it.username,
                    profile = it.profile
                )
            }
        )
    }

    actual suspend fun unlink(providerId: String): FirebaseResult<FirebaseUser> = safeCall {
        val result = android.unlink(providerId).await()
        FirebaseUser(result.user!!)
    }

    actual suspend fun reauthenticate(credential: AuthCredential): FirebaseResult<Unit> = safeCall {
        android.reauthenticate(credential.toAndroid()).await()
        Unit
    }
}
