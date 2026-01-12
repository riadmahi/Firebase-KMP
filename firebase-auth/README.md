# Firebase Auth

Authentication for KFire with support for multiple providers.

## Installation

```kotlin
// build.gradle.kts
commonMain.dependencies {
    implementation("com.riadmahi.firebase:firebase-core:1.0.0")
    implementation("com.riadmahi.firebase:firebase-auth:1.0.0")
}
```

## Usage

### Get Auth Instance

```kotlin
import com.riadmahi.firebase.auth.FirebaseAuth

val auth = FirebaseAuth.getInstance()
```

### Sign Up with Email

```kotlin
val result = auth.createUserWithEmailAndPassword("ada@example.com", "password123")
when (result) {
    is FirebaseResult.Success -> println("User created: ${result.data.user?.uid}")
    is FirebaseResult.Failure -> println("Error: ${result.exception.message}")
}
```

### Sign In with Email

```kotlin
val result = auth.signInWithEmailAndPassword("ada@example.com", "password123")
when (result) {
    is FirebaseResult.Success -> println("Signed in!")
    is FirebaseResult.Failure -> println("Error: ${result.exception.message}")
}
```

### Sign In Anonymously

```kotlin
val result = auth.signInAnonymously()
when (result) {
    is FirebaseResult.Success -> println("Anonymous user: ${result.data.user?.uid}")
    is FirebaseResult.Failure -> println("Error: ${result.exception.message}")
}
```

### Social Authentication

```kotlin
// Google Sign-In
val credential = AuthCredential.Google(idToken = "google-id-token")
auth.signInWithCredential(credential)

// Apple Sign-In
val credential = AuthCredential.Apple(
    idToken = "apple-id-token",
    nonce = "random-nonce"
)
auth.signInWithCredential(credential)
```

### Listen to Auth State

```kotlin
auth.authStateFlow.collect { user ->
    if (user != null) {
        println("Signed in as: ${user.email}")
    } else {
        println("Not signed in")
    }
}
```

### User Management

```kotlin
val user = auth.currentUser ?: return

// User properties
user.uid
user.email
user.displayName
user.isAnonymous

// Update profile
user.updateProfile(displayName = "Ada Lovelace")

// Send email verification
user.sendEmailVerification()

// Sign out
auth.signOut()
```

### Password Reset

```kotlin
auth.sendPasswordResetEmail("ada@example.com")
```

### Emulator

```kotlin
auth.useEmulator("10.0.2.2", 9099)  // Android
auth.useEmulator("localhost", 9099) // iOS
```

## API Reference

| Method | Description |
|--------|-------------|
| `signInWithEmailAndPassword()` | Sign in with email/password |
| `createUserWithEmailAndPassword()` | Create a new user |
| `signInWithCredential()` | Sign in with OAuth credential |
| `signInAnonymously()` | Sign in anonymously |
| `signInWithCustomToken()` | Sign in with custom token |
| `signOut()` | Sign out current user |
| `sendPasswordResetEmail()` | Send password reset email |
| `authStateFlow` | Observe auth state changes |

## See Also

- [Firebase Auth Documentation](https://firebase.google.com/docs/auth)
