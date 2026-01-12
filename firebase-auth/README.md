# firebase-auth

Firebase Authentication module for Kotlin Multiplatform.

## Installation

```kotlin
// build.gradle.kts
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("com.riadmahi.firebase:firebase-core:1.0.0")
            implementation("com.riadmahi.firebase:firebase-auth:1.0.0")
        }
    }
}
```

## Main Classes

### FirebaseAuth

Main authentication service.

```kotlin
import com.riadmahi.firebase.auth.FirebaseAuth
import com.riadmahi.firebase.core.FirebaseResult

val auth = FirebaseAuth.getInstance()

// Current user
val user = auth.currentUser
```

### Authentication Methods

#### Email/Password

```kotlin
// Sign up
when (val result = auth.createUserWithEmailAndPassword("email@example.com", "password123")) {
    is FirebaseResult.Success -> {
        val user = result.data.user
        println("Signed up: ${user.uid}")
    }
    is FirebaseResult.Failure -> {
        println("Error: ${result.exception.message}")
    }
}

// Sign in
when (val result = auth.signInWithEmailAndPassword("email@example.com", "password123")) {
    is FirebaseResult.Success -> println("Signed in!")
    is FirebaseResult.Failure -> println("Error: ${result.exception.message}")
}

// Password reset
auth.sendPasswordResetEmail("email@example.com")

// Confirm password reset
auth.confirmPasswordReset(code = "oobCode", newPassword = "newPassword123")
```

#### OAuth Providers

```kotlin
import com.riadmahi.firebase.auth.AuthCredential

// Google Sign-In
val googleCredential = AuthCredential.Google(
    idToken = "google-id-token",
    accessToken = "google-access-token" // optional
)
auth.signInWithCredential(googleCredential)

// Apple Sign-In
val appleCredential = AuthCredential.Apple(idToken = "apple-id-token")
auth.signInWithCredential(appleCredential)

// Generic OAuth
val oauthCredential = AuthCredential.OAuth(
    providerId = "github.com",
    idToken = "oauth-id-token",
    accessToken = "oauth-access-token"
)
auth.signInWithCredential(oauthCredential)
```

#### Phone Authentication

```kotlin
val phoneCredential = AuthCredential.Phone(
    verificationId = "verification-id",
    smsCode = "123456"
)
auth.signInWithCredential(phoneCredential)
```

#### Anonymous Authentication

```kotlin
when (val result = auth.signInAnonymously()) {
    is FirebaseResult.Success -> {
        val user = result.data.user
        println("Anonymous user: ${user.uid}")
    }
    is FirebaseResult.Failure -> println("Error")
}
```

#### Custom Token

```kotlin
auth.signInWithCustomToken("custom-jwt-token")
```

### Sign Out

```kotlin
auth.signOut()
```

### Auth State Observer

```kotlin
// Observe authentication state changes
auth.authStateFlow.collect { user ->
    if (user != null) {
        println("Signed in: ${user.email}")
    } else {
        println("Signed out")
    }
}
```

## FirebaseUser

### Properties

```kotlin
val user = auth.currentUser

user?.uid              // Unique user ID
user?.email            // Email address
user?.displayName      // Display name
user?.photoUrl         // Profile photo URL
user?.phoneNumber      // Phone number
user?.isEmailVerified  // Email verification status
user?.isAnonymous      // Anonymous account flag
user?.providerId       // Primary provider ID
user?.providerData     // List of linked providers
```

### Profile Management

```kotlin
// Update profile
user?.updateProfile(
    displayName = "John Doe",
    photoUrl = "https://example.com/photo.jpg"
)

// Update email (sends verification)
user?.updateEmail("new@example.com")

// Update password
user?.updatePassword("newPassword123")

// Send email verification
user?.sendEmailVerification()

// Reload user data
user?.reload()

// Delete account
user?.delete()
```

### ID Token

```kotlin
when (val result = user?.getIdToken(forceRefresh = false)) {
    is FirebaseResult.Success -> {
        val token = result.data
        // Use token for backend authentication
    }
    is FirebaseResult.Failure -> println("Error")
}
```

### Account Linking

```kotlin
// Link additional provider
val credential = AuthCredential.Google(idToken = "...")
user?.linkWithCredential(credential)

// Unlink provider
user?.unlink("google.com")

// Re-authenticate before sensitive operations
user?.reauthenticate(credential)
```

## AuthCredential Types

```kotlin
sealed class AuthCredential {
    data class EmailPassword(val email: String, val password: String)
    data class Google(val idToken: String, val accessToken: String? = null)
    data class Apple(val idToken: String)
    data class Phone(val verificationId: String, val smsCode: String)
    data class Custom(val token: String)
    data class OAuth(val providerId: String, val idToken: String?, val accessToken: String?)
}
```

## AuthResult

```kotlin
data class AuthResult(
    val user: FirebaseUser,
    val additionalUserInfo: AdditionalUserInfo?
)

data class AdditionalUserInfo(
    val isNewUser: Boolean,
    val providerId: String?,
    val username: String?,
    val profile: Map<String, Any?>?
)
```

## Error Handling

```kotlin
when (val result = auth.signInWithEmailAndPassword(email, password)) {
    is FirebaseResult.Success -> { /* success */ }
    is FirebaseResult.Failure -> {
        when (result.exception) {
            is AuthException.InvalidCredential -> "Invalid credentials"
            is AuthException.UserNotFound -> "User not found"
            is AuthException.WrongPassword -> "Wrong password"
            is AuthException.EmailAlreadyInUse -> "Email already in use"
            is AuthException.WeakPassword -> "Password too weak"
            is AuthException.InvalidEmail -> "Invalid email format"
            is AuthException.TooManyRequests -> "Too many attempts, try later"
            is AuthException.UserDisabled -> "Account disabled"
            is AuthException.RequiresRecentLogin -> "Please re-authenticate"
            is AuthException.CredentialAlreadyInUse -> "Credential already linked"
            is AuthException.OperationNotAllowed -> "Operation not allowed"
            is AuthException.NetworkError -> "Network error"
            else -> "Unknown error"
        }
    }
}
```

## Emulator Support

```kotlin
// Connect to Firebase Auth Emulator
auth.useEmulator("localhost", 9099)
```

## UserInfo

Information about linked providers.

```kotlin
data class UserInfo(
    val uid: String,
    val providerId: String,
    val email: String?,
    val displayName: String?,
    val photoUrl: String?,
    val phoneNumber: String?
)

// Get linked providers
val providers: List<UserInfo> = user?.providerData ?: emptyList()
```
