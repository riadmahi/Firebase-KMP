# Firebase Core

The foundation module required by all KFire services.

## Installation

```kotlin
// build.gradle.kts
commonMain.dependencies {
    implementation("com.riadmahi.firebase:firebase-core:1.0.0")
}
```

## Usage

### Initialize Firebase

```kotlin
import com.riadmahi.firebase.core.FirebaseApp

// Default initialization (uses google-services.json / GoogleService-Info.plist)
val app = FirebaseApp.initialize()
```

### Custom Configuration

```kotlin
import com.riadmahi.firebase.core.firebaseOptions

val app = FirebaseApp.initialize(firebaseOptions {
    apiKey = "AIza..."
    applicationId = "1:123456789:android:abc123"
    projectId = "my-project"
    storageBucket = "my-project.appspot.com"
})
```

### Named App Instance

```kotlin
// Create a secondary app instance
val secondaryApp = FirebaseApp.initialize(options, "secondary")

// Get instances
val defaultApp = FirebaseApp.getInstance()
val namedApp = FirebaseApp.getInstance("secondary")

// List all apps
val allApps: List<FirebaseApp> = FirebaseApp.apps

// Delete an app
app.delete()
```

## API Reference

| Method | Description |
|--------|-------------|
| `FirebaseApp.initialize()` | Initialize with default config files |
| `FirebaseApp.initialize(options)` | Initialize with custom options |
| `FirebaseApp.initialize(options, name)` | Initialize a named instance |
| `FirebaseApp.getInstance()` | Get the default instance |
| `FirebaseApp.getInstance(name)` | Get a named instance |
| `FirebaseApp.apps` | List all initialized apps |
| `app.delete()` | Delete the app instance |

## See Also

- [Firebase Documentation](https://firebase.google.com/docs/reference/kotlin/com/google/firebase/FirebaseApp)
