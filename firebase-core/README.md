# firebase-core

Core module for Firebase initialization in Kotlin Multiplatform projects.

## Installation

```kotlin
// build.gradle.kts
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("com.riadmahi.firebase:firebase-core:1.0.0")
        }
    }
}
```

## Main Classes

### FirebaseApp

Entry point for Firebase initialization.

```kotlin
import com.riadmahi.firebase.core.FirebaseApp
import com.riadmahi.firebase.core.FirebaseOptions

// Default initialization
val app = FirebaseApp.initialize()

// With custom options
val options = FirebaseOptions(
    apiKey = "AIza...",
    applicationId = "1:123456789:android:abc123",
    projectId = "my-project",
    storageBucket = "my-project.appspot.com",
    gcmSenderId = "123456789"
)
val app = FirebaseApp.initialize(options)

// With custom name
val secondaryApp = FirebaseApp.initialize(options, "secondary")

// Get instance
val defaultApp = FirebaseApp.getInstance()
val namedApp = FirebaseApp.getInstance("secondary")

// List all apps
val allApps: List<FirebaseApp> = FirebaseApp.apps

// Delete an app
app.delete()
```

### FirebaseOptions

Firebase project configuration.

```kotlin
data class FirebaseOptions(
    val apiKey: String,           // Web API Key
    val applicationId: String,    // App ID (Android/iOS)
    val projectId: String,        // Firebase project ID
    val databaseUrl: String? = null,
    val storageBucket: String? = null,
    val gcmSenderId: String? = null
)
```

### FirebaseResult

Wrapper for async operation results.

```kotlin
sealed class FirebaseResult<out T> {
    data class Success<T>(val data: T) : FirebaseResult<T>()
    data class Failure(val exception: FirebaseException) : FirebaseResult<Nothing>()
}

// Usage
when (val result = someOperation()) {
    is FirebaseResult.Success -> {
        val data = result.data
    }
    is FirebaseResult.Failure -> {
        val error = result.exception
    }
}

// Helpers
result.getOrNull()              // T?
result.getOrDefault(defaultVal) // T
result.getOrElse { fallback }   // T
result.isSuccess                // Boolean
result.isFailure                // Boolean
```

### FirebaseException

Base class for Firebase exceptions.

```kotlin
open class FirebaseException(
    override val message: String,
    override val cause: Throwable? = null
) : Exception(message, cause)
```

## expect/actual Architecture

```kotlin
// commonMain/FirebaseApp.kt
expect class FirebaseApp {
    val name: String
    val options: FirebaseOptions
    fun delete()

    companion object {
        val DEFAULT_APP_NAME: String
        fun initialize(): FirebaseApp
        fun initialize(options: FirebaseOptions): FirebaseApp
        fun initialize(options: FirebaseOptions, name: String): FirebaseApp
        fun getInstance(): FirebaseApp
        fun getInstance(name: String): FirebaseApp
        val apps: List<FirebaseApp>
    }
}

// androidMain - delegates to com.google.firebase.FirebaseApp
// iosMain - delegates to FIRApp (Firebase iOS SDK)
```

## Platform Configuration

### Android

Add `google-services.json` in `composeApp/src/androidMain/`:

```
composeApp/
└── src/
    └── androidMain/
        └── google-services.json
```

### iOS

Add `GoogleService-Info.plist` in the Xcode project:

```
iosApp/
└── iosApp/
    └── GoogleService-Info.plist
```
