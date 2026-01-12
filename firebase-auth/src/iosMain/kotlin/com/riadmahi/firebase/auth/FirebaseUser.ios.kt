@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.riadmahi.firebase.auth

import cocoapods.FirebaseAuth.FIRAuthDataResult
import cocoapods.FirebaseAuth.FIRUser
import cocoapods.FirebaseAuth.FIRUserProfileChangeRequest
import com.riadmahi.firebase.core.FirebaseResult
import com.riadmahi.firebase.core.util.awaitResult
import com.riadmahi.firebase.core.util.awaitVoid
import platform.Foundation.NSURL

/**
 * iOS implementation of FirebaseUser using Firebase iOS SDK.
 */
actual class FirebaseUser internal constructor(
    internal val ios: FIRUser
) {
    actual val uid: String
        get() = ios.uid()

    actual val email: String?
        get() = ios.email()

    actual val displayName: String?
        get() = ios.displayName()

    actual val photoUrl: String?
        get() = ios.photoURL()?.absoluteString

    actual val phoneNumber: String?
        get() = ios.phoneNumber()

    actual val isAnonymous: Boolean
        get() = ios.anonymous()

    actual val isEmailVerified: Boolean
        get() = ios.emailVerified()

    actual val providerId: String
        get() = ios.providerID()

    actual val providerData: List<UserInfo>
        get() = ios.providerData().mapNotNull { provider ->
            (provider as? cocoapods.FirebaseAuth.FIRUserInfoProtocol)?.let {
                UserInfo(
                    uid = it.uid() ?: "",
                    providerId = it.providerID(),
                    email = it.email(),
                    displayName = it.displayName(),
                    photoUrl = it.photoURL()?.absoluteString,
                    phoneNumber = it.phoneNumber()
                )
            }
        }

    actual suspend fun getIdToken(forceRefresh: Boolean): FirebaseResult<String> = safeAuthCall {
        awaitResult<String> { callback ->
            ios.getIDTokenForcingRefresh(forceRefresh) { token, error ->
                callback(token, error)
            }
        }
    }

    actual suspend fun reload(): FirebaseResult<Unit> = safeAuthCall {
        awaitVoid { callback ->
            ios.reloadWithCompletion { error ->
                callback(error)
            }
        }
    }

    actual suspend fun delete(): FirebaseResult<Unit> = safeAuthCall {
        awaitVoid { callback ->
            ios.deleteWithCompletion { error ->
                callback(error)
            }
        }
    }

    actual suspend fun updateProfile(
        displayName: String?,
        photoUrl: String?
    ): FirebaseResult<Unit> = safeAuthCall {
        val changeRequest: FIRUserProfileChangeRequest = ios.profileChangeRequest()
        displayName?.let { changeRequest.setDisplayName(it) }
        photoUrl?.let { changeRequest.setPhotoURL(NSURL.URLWithString(it)) }

        awaitVoid { callback ->
            changeRequest.commitChangesWithCompletion { error ->
                callback(error)
            }
        }
    }

    actual suspend fun updateEmail(email: String): FirebaseResult<Unit> = safeAuthCall {
        awaitVoid { callback ->
            ios.sendEmailVerificationBeforeUpdatingEmail(email) { error ->
                callback(error)
            }
        }
    }

    actual suspend fun updatePassword(password: String): FirebaseResult<Unit> = safeAuthCall {
        awaitVoid { callback ->
            ios.updatePassword(password) { error ->
                callback(error)
            }
        }
    }

    actual suspend fun sendEmailVerification(): FirebaseResult<Unit> = safeAuthCall {
        awaitVoid { callback ->
            ios.sendEmailVerificationWithCompletion { error ->
                callback(error)
            }
        }
    }

    actual suspend fun linkWithCredential(credential: AuthCredential): FirebaseResult<AuthResult> = safeAuthCall {
        val firCredential = credential.toIos()
        val result = awaitResult<FIRAuthDataResult> { callback ->
            ios.linkWithCredential(firCredential) { authResult, error ->
                callback(authResult, error)
            }
        }
        AuthResult(
            user = FirebaseUser(result.user()),
            additionalUserInfo = result.additionalUserInfo()?.let {
                AdditionalUserInfo(
                    isNewUser = it.newUser(),
                    providerId = it.providerID(),
                    username = it.username(),
                    profile = it.profile() as? Map<String, Any?>
                )
            }
        )
    }

    actual suspend fun unlink(providerId: String): FirebaseResult<FirebaseUser> = safeAuthCall {
        val user = awaitResult<FIRUser> { callback ->
            ios.unlinkFromProvider(providerId) { firUser, error ->
                callback(firUser, error)
            }
        }
        FirebaseUser(user)
    }

    actual suspend fun reauthenticate(credential: AuthCredential): FirebaseResult<Unit> = safeAuthCall {
        val firCredential = credential.toIos()
        awaitVoid { callback ->
            ios.reauthenticateWithCredential(firCredential) { _, error ->
                callback(error)
            }
        }
    }
}
