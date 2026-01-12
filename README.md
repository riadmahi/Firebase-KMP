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

### 1. Prerequisites

**Firebase CLI:**

```bash
npm install -g firebase-tools
firebase login
```

**Firebase Project:**

Go to [console.firebase.google.com](https://console.firebase.google.com) and create a project:
1. Click "Add project" and enter your project name
2. When asked to add an app, **select Flutter**
3. Skip all the Flutter setup steps (we'll use KFire instead)
4. Your project is ready

### 2. Install KFire CLI

```bash
./gradlew :firebase-cli:installDist
sudo ln -sf $(pwd)/firebase-cli/build/install/kfire/bin/kfire /usr/local/bin/kfire
```

### 3. Run the Setup Wizard

```bash
kfire init
```

```
╭────────────────────────────────────────────────────────────╮
│                                                            │
│   Welcome to KFire - Firebase for Kotlin Multiplatform     │
│                                                            │
╰────────────────────────────────────────────────────────────╯

◆ Pre-flight Checks

  ✓ firebase-tools is installed
  ✓ Authenticated as riad@example.com
  ✓ Android: com.example.myapp
  ✓ iOS: com.example.myapp

◆ Select Firebase Project

  ● my-awesome-app (my-awesome-app-12345)
  ○ another-project (another-project-67890)
  ○ Create a new project

◆ Select Modules

  ✓ Core (required)
  ✓ Authentication
  ✓ Firestore
  ✓ Storage
  ○ Messaging
  ○ Analytics
  ○ Remote Config
  ○ Crashlytics

◆ Generating Configuration

  ✓ Created google-services.json
  ✓ Created GoogleService-Info.plist
  ✓ Updated build.gradle.kts
  ✓ Generated FirebaseInit.kt

🎉 Firebase configured successfully!
```

### 4. Start Using Firebase

```kotlin
// Initialize once at app startup
FirebaseApp.initialize()

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

## CLI Commands

```bash
kfire init                    # Full setup wizard
kfire configure               # Quick configuration
kfire login                   # Authenticate with Firebase
```

**Options:**

```bash
kfire configure \
  --project=my-firebase-project \
  --modules=core,auth,firestore \
  --platforms=android,ios
```

---

## Manual Setup

<details>
<summary>If you prefer manual configuration</summary>

### Add Dependencies

```kotlin
// build.gradle.kts
commonMain.dependencies {
    implementation("com.riadmahi.firebase:firebase-core:1.0.0")
    implementation("com.riadmahi.firebase:firebase-auth:1.0.0")
    implementation("com.riadmahi.firebase:firebase-firestore:1.0.0")
}
```

### Android

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

Place `google-services.json` in your Android app module.

### iOS

Add Firebase via CocoaPods or SPM and place `GoogleService-Info.plist` in your Xcode project.

</details>

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
