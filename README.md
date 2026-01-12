<p align="center">
  <img src="https://firebase.google.com/static/images/brand-guidelines/logo-logomark.png" alt="Firebase" width="80" height="80">
</p>

<h1 align="center">KFire</h1>

<p align="center">
  <strong>Firebase SDK for Kotlin Multiplatform</strong>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Kotlin-2.3.0-7F52FF?logo=kotlin&logoColor=white" alt="Kotlin">
  <img src="https://img.shields.io/badge/Platform-Android%20|%20iOS-34A853" alt="Platform">
  <img src="https://img.shields.io/badge/License-MIT-blue.svg" alt="License">
</p>

---

## Quick Start

### 1. Add Dependencies

```kotlin
// build.gradle.kts
commonMain.dependencies {
    implementation("com.riadmahi.firebase:firebase-core:1.0.0")
    implementation("com.riadmahi.firebase:firebase-auth:1.0.0")       // Optional
    implementation("com.riadmahi.firebase:firebase-firestore:1.0.0") // Optional
    implementation("com.riadmahi.firebase:firebase-storage:1.0.0")   // Optional
    // ... add more modules as needed
}
```

### 2. Initialize Firebase

```kotlin
import com.riadmahi.firebase.core.FirebaseApp

// Uses google-services.json (Android) / GoogleService-Info.plist (iOS)
FirebaseApp.initialize()
```

### 3. Start Using Firebase

```kotlin
// Authentication
val auth = FirebaseAuth.getInstance()
auth.signInWithEmailAndPassword("user@example.com", "password")

// Firestore
val db = FirebaseFirestore.getInstance()
db.collection("cities").document("LA").set(mapOf(
    "name" to "Los Angeles",
    "country" to "USA"
))

// Storage
val storage = FirebaseStorage.getInstance()
storage.reference.child("images/photo.jpg").putBytes(imageData)
```

---

## Modules

| Module | Description | Documentation |
|--------|-------------|---------------|
| **Core** | Firebase initialization | [README](firebase-core/README.md) |
| **Auth** | Authentication (Email, Google, Apple, Phone) | [README](firebase-auth/README.md) |
| **Firestore** | NoSQL cloud database | [README](firebase-firestore/README.md) |
| **Storage** | File storage | [README](firebase-storage/README.md) |
| **Messaging** | Push notifications (FCM) | [README](firebase-messaging/README.md) |
| **Analytics** | User behavior tracking | [README](firebase-analytics/README.md) |
| **Remote Config** | Feature flags & configuration | [README](firebase-remoteconfig/README.md) |
| **Crashlytics** | Crash reporting | [README](firebase-crashlytics/README.md) |

---

## CLI Tool

Configure Firebase in your KMP project with the `kfire` CLI.

```bash
# Install
./gradlew :firebase-cli:installDist
sudo ln -sf $(pwd)/firebase-cli/build/install/kfire/bin/kfire /usr/local/bin/kfire

# Full setup wizard
kfire init

# Or quick configure
kfire configure --project=my-project
```

The CLI automatically:
- Detects your Android package name and iOS bundle ID
- Creates Firebase apps in your project
- Generates `google-services.json` and `GoogleService-Info.plist`
- Configures your Gradle files

---

## Platform Setup

### Android

Add Google Services plugin:

```kotlin
// Root build.gradle.kts
plugins {
    id("com.google.gms.google-services") version "4.4.2" apply false
}

// App build.gradle.kts
plugins {
    id("com.google.gms.google-services")
}
```

### iOS

Add Firebase via CocoaPods or SPM and place `GoogleService-Info.plist` in your Xcode project.

---

## Requirements

| Platform | Minimum Version |
|----------|-----------------|
| Android | API 24 |
| iOS | 15.0 |
| Kotlin | 2.3.0 |

---

## License

MIT License - see [LICENSE](LICENSE) for details.

---

<p align="center">
  <a href="https://firebase.google.com/docs">Firebase Docs</a> •
  <a href="https://kotlinlang.org/docs/multiplatform.html">Kotlin Multiplatform</a>
</p>
