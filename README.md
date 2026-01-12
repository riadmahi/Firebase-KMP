<p align="center">
  <img src="https://firebase.google.com/static/images/brand-guidelines/logo-logomark.png" alt="Firebase" width="80" height="80">
</p>

<h1 align="center">KFire</h1>

<p align="center">
  <strong>Firebase SDK for Kotlin Multiplatform</strong>
</p>

<p align="center">
  A type-safe, coroutine-first Firebase SDK for Kotlin Multiplatform with a configuration CLI inspired by FlutterFire.
</p>

<p align="center">
  <a href="#installation">Installation</a> •
  <a href="#quick-start">Quick Start</a> •
  <a href="#modules">Modules</a> •
  <a href="#api-reference">API Reference</a> •
  <a href="#cli">CLI</a>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Kotlin-2.3.0-7F52FF?logo=kotlin&logoColor=white" alt="Kotlin">
  <img src="https://img.shields.io/badge/Platform-Android%20|%20iOS-34A853?logo=android&logoColor=white" alt="Platform">
  <img src="https://img.shields.io/badge/Firebase%20Android-34.7.0-FFCA28?logo=firebase&logoColor=black" alt="Firebase Android">
  <img src="https://img.shields.io/badge/Firebase%20iOS-11.6.0-FFCA28?logo=firebase&logoColor=black" alt="Firebase iOS">
  <img src="https://img.shields.io/badge/License-MIT-blue.svg" alt="License">
</p>

---

## Table of Contents

- [Features](#features)
- [Requirements](#requirements)
- [Installation](#installation)
- [Quick Start](#quick-start)
- [Modules](#modules)
  - [Firebase Core](#firebase-core)
  - [Firebase Auth](#firebase-auth)
  - [Firebase Firestore](#firebase-firestore)
- [CLI Tool](#cli-tool)
- [API Reference](#api-reference)
- [Error Handling](#error-handling)
- [Architecture](#architecture)
- [Roadmap](#roadmap)
- [Contributing](#contributing)
- [License](#license)

---

## Features

- **100% Kotlin** - Designed from the ground up for Kotlin Multiplatform
- **Coroutines First** - All async operations use `suspend` functions and `Flow`
- **Type-Safe** - Sealed classes for results, credentials, and exceptions
- **Consistent API** - Same API across Android and iOS platforms
- **CLI Tool** - `kfire` CLI for quick project configuration (similar to FlutterFire)
- **Modern** - Uses latest Kotlin 2.3.0 and Firebase SDK versions

---

## Requirements

| Platform | Minimum Version |
|----------|-----------------|
| Android | API 24 (Android 7.0) |
| iOS | 15.0+ |
| Kotlin | 2.3.0+ |
| Gradle | 8.11+ |

---

## Installation

### Step 1: Add the Repository

KFire is published to Maven Central. Add the dependencies to your `libs.versions.toml`:

```toml
[versions]
kfire = "1.0.0"

[libraries]
kfire-core = { module = "com.riadmahi.firebase:firebase-core", version.ref = "kfire" }
kfire-auth = { module = "com.riadmahi.firebase:firebase-auth", version.ref = "kfire" }
kfire-firestore = { module = "com.riadmahi.firebase:firebase-firestore", version.ref = "kfire" }
```

### Step 2: Add Dependencies

In your shared module's `build.gradle.kts`:

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.kfire.core)
            implementation(libs.kfire.auth)       // Optional
            implementation(libs.kfire.firestore)  // Optional
        }
    }
}
```

### Step 3: Platform Configuration

#### Android

Add the Google Services plugin to your app's `build.gradle.kts`:

```kotlin
plugins {
    id("com.google.gms.google-services")
}
```

And in your root `build.gradle.kts`:

```kotlin
plugins {
    id("com.google.gms.google-services") version "4.4.2" apply false
}
```

> See [Firebase Android Setup](https://firebase.google.com/docs/android/setup) for detailed instructions.

#### iOS

Add Firebase to your iOS project via CocoaPods or Swift Package Manager. The SDK automatically bridges to the native Firebase iOS SDK.

> See [Firebase iOS Setup](https://firebase.google.com/docs/ios/setup) for detailed instructions.

---

## Quick Start

### Using the CLI (Recommended)

The fastest way to configure Firebase is using the `kfire` CLI:

```bash
# Install the CLI
./gradlew :firebase-cli:installDist
sudo ln -sf $(pwd)/firebase-cli/build/install/kfire/bin/kfire /usr/local/bin/kfire

# Login to Firebase
kfire login

# Configure your project
kfire configure
```

The CLI automatically:
- Detects your Android package name and iOS bundle ID
- Creates or retrieves Firebase apps from your project
- Generates `google-services.json` for Android
- Generates `GoogleService-Info.plist` for iOS
- Configures your Gradle files

### Manual Configuration

```kotlin
import com.riadmahi.firebase.core.FirebaseApp

// Initialize with default configuration files
FirebaseApp.initialize()

// Or initialize with custom options
val options = firebaseOptions {
    apiKey = "your-api-key"
    applicationId = "your-app-id"
    projectId = "your-project-id"
    storageBucket = "your-bucket.appspot.com"
}
FirebaseApp.initialize(options)
```

---

## Modules

### Firebase Core

The foundation module required by all other Firebase services.

```kotlin
import com.riadmahi.firebase.core.FirebaseApp
import com.riadmahi.firebase.core.FirebaseOptions
import com.riadmahi.firebase.core.firebaseOptions
```

#### Initialize Firebase

```kotlin
// Default initialization (uses google-services.json / GoogleService-Info.plist)
val app = FirebaseApp.initialize()

// Using builder DSL
val app = FirebaseApp.initialize(firebaseOptions {
    apiKey = "AIza..."
    applicationId = "1:123456789:android:abc123"
    projectId = "my-project"
    storageBucket = "my-project.appspot.com"
    gcmSenderId = "123456789"
    databaseUrl = "https://my-project.firebaseio.com"
})

// Named app instance
val secondaryApp = FirebaseApp.initialize(options, "secondary")

// Get instances
val defaultApp = FirebaseApp.getInstance()
val namedApp = FirebaseApp.getInstance("secondary")

// List all apps
val allApps: List<FirebaseApp> = FirebaseApp.apps

// Delete an app
app.delete()
```

> **Firebase Documentation:** [Firebase Initialization](https://firebase.google.com/docs/reference/kotlin/com/google/firebase/FirebaseApp)

---

### Firebase Auth

Complete authentication solution supporting multiple providers.

```kotlin
import com.riadmahi.firebase.auth.FirebaseAuth
import com.riadmahi.firebase.auth.FirebaseUser
import com.riadmahi.firebase.auth.AuthCredential
import com.riadmahi.firebase.auth.AuthResult
```

#### Get Auth Instance

```kotlin
val auth = FirebaseAuth.getInstance()

// For a specific app
val auth = FirebaseAuth.getInstance(myApp)
```

#### Authentication State

```kotlin
// Current user (nullable)
val user: FirebaseUser? = auth.currentUser

// Observe auth state changes (Flow)
auth.authStateFlow.collect { user ->
    if (user != null) {
        println("Signed in as: ${user.email}")
    } else {
        println("Not signed in")
    }
}
```

#### Email & Password Authentication

```kotlin
// Create account
when (val result = auth.createUserWithEmailAndPassword("user@example.com", "password123")) {
    is FirebaseResult.Success -> {
        val user = result.data.user
        println("Account created: ${user.uid}")
    }
    is FirebaseResult.Failure -> {
        println("Error: ${result.exception.message}")
    }
}

// Sign in
when (val result = auth.signInWithEmailAndPassword("user@example.com", "password123")) {
    is FirebaseResult.Success -> println("Signed in!")
    is FirebaseResult.Failure -> println("Error: ${result.exception.message}")
}

// Password reset
auth.sendPasswordResetEmail("user@example.com")
auth.confirmPasswordReset(code = "oobCode", newPassword = "newPassword123")
```

> **Firebase Documentation:** [Email/Password Auth](https://firebase.google.com/docs/auth/android/password-auth)

#### Social Authentication

```kotlin
// Google Sign-In
val googleCredential = AuthCredential.Google(
    idToken = "google-id-token",
    accessToken = "google-access-token"  // Optional
)
auth.signInWithCredential(googleCredential)

// Apple Sign-In
val appleCredential = AuthCredential.Apple(
    idToken = "apple-id-token",
    nonce = "random-nonce",
    fullName = AppleFullName(
        givenName = "John",
        familyName = "Doe"
    )
)
auth.signInWithCredential(appleCredential)

// Phone Authentication
val phoneCredential = AuthCredential.Phone(
    verificationId = "verification-id",
    smsCode = "123456"
)
auth.signInWithCredential(phoneCredential)

// Generic OAuth
val oauthCredential = AuthCredential.OAuth(
    providerId = "github.com",
    accessToken = "github-access-token"
)
auth.signInWithCredential(oauthCredential)
```

> **Firebase Documentation:** [Google Sign-In](https://firebase.google.com/docs/auth/android/google-signin) | [Apple Sign-In](https://firebase.google.com/docs/auth/ios/apple) | [Phone Auth](https://firebase.google.com/docs/auth/android/phone-auth)

#### Anonymous Authentication

```kotlin
when (val result = auth.signInAnonymously()) {
    is FirebaseResult.Success -> {
        val user = result.data.user
        println("Anonymous user: ${user.uid}")
        println("Is anonymous: ${user.isAnonymous}")  // true
    }
    is FirebaseResult.Failure -> println("Error: ${result.exception}")
}
```

> **Firebase Documentation:** [Anonymous Auth](https://firebase.google.com/docs/auth/android/anonymous-auth)

#### Custom Token Authentication

```kotlin
// Sign in with a token generated by your backend
val result = auth.signInWithCustomToken("custom-jwt-token")
```

> **Firebase Documentation:** [Custom Auth](https://firebase.google.com/docs/auth/android/custom-auth)

#### User Management

```kotlin
val user = auth.currentUser ?: return

// User properties
user.uid              // Unique identifier
user.email            // Email address
user.displayName      // Display name
user.photoUrl         // Profile photo URL
user.phoneNumber      // Phone number
user.isAnonymous      // Is anonymous user
user.isEmailVerified  // Is email verified
user.providerId       // Provider ID
user.providerData     // List<UserInfo> - all linked providers

// Update profile
user.updateProfile(
    displayName = "John Doe",
    photoUrl = "https://example.com/photo.jpg"
)

// Update email
user.updateEmail("newemail@example.com")

// Update password
user.updatePassword("newSecurePassword123")

// Send email verification
user.sendEmailVerification()

// Get ID token (for backend verification)
when (val result = user.getIdToken(forceRefresh = false)) {
    is FirebaseResult.Success -> {
        val token = result.data
        // Send token to your backend
    }
    is FirebaseResult.Failure -> println("Error getting token")
}

// Reload user data from server
user.reload()

// Link additional provider
val credential = AuthCredential.Google(idToken = "...")
user.linkWithCredential(credential)

// Unlink provider
user.unlink("google.com")

// Re-authenticate (required for sensitive operations)
user.reauthenticate(credential)

// Delete account
user.delete()
```

#### Sign Out

```kotlin
when (val result = auth.signOut()) {
    is FirebaseResult.Success -> println("Signed out")
    is FirebaseResult.Failure -> println("Error signing out")
}
```

#### Emulator Support

```kotlin
// Connect to Firebase Auth Emulator for local development
auth.useEmulator("10.0.2.2", 9099)  // Android emulator
auth.useEmulator("localhost", 9099)  // iOS simulator
```

> **Firebase Documentation:** [Auth Emulator](https://firebase.google.com/docs/emulator-suite/connect_auth)

---

### Firebase Firestore

A flexible, scalable NoSQL cloud database.

```kotlin
import com.riadmahi.firebase.firestore.FirebaseFirestore
import com.riadmahi.firebase.firestore.CollectionReference
import com.riadmahi.firebase.firestore.DocumentReference
import com.riadmahi.firebase.firestore.Query
import com.riadmahi.firebase.firestore.Direction
import com.riadmahi.firebase.firestore.SetOptions
import com.riadmahi.firebase.firestore.Source
import com.riadmahi.firebase.firestore.FieldValue
```

#### Get Firestore Instance

```kotlin
val db = FirebaseFirestore.getInstance()

// For a specific app
val db = FirebaseFirestore.getInstance(myApp)
```

#### References

```kotlin
// Collection reference
val usersCollection: CollectionReference = db.collection("users")

// Document reference
val userDoc: DocumentReference = db.collection("users").document("user123")

// Subcollection
val postsCollection = db.collection("users").document("user123").collection("posts")

// Document path
val specificDoc = db.document("users/user123/posts/post456")

// Collection group (query across all collections with same name)
val allPosts: Query = db.collectionGroup("posts")
```

> **Firebase Documentation:** [Firestore Data Model](https://firebase.google.com/docs/firestore/data-model)

#### Create Documents

```kotlin
// Add document with auto-generated ID
val data = mapOf(
    "name" to "John Doe",
    "email" to "john@example.com",
    "age" to 30,
    "tags" to listOf("developer", "kotlin"),
    "metadata" to mapOf("createdAt" to FieldValue.serverTimestamp())
)

when (val result = db.collection("users").add(data)) {
    is FirebaseResult.Success -> {
        val docRef = result.data
        println("Document created with ID: ${docRef.id}")
    }
    is FirebaseResult.Failure -> println("Error: ${result.exception}")
}

// Set document with specific ID
db.collection("users").document("user123").set(data)

// Set with merge (update existing fields, keep others)
db.collection("users").document("user123").set(data, SetOptions.Merge)

// Set with merge fields (only merge specified fields)
db.collection("users").document("user123").set(
    data,
    SetOptions.MergeFields("name", "email")
)
```

> **Firebase Documentation:** [Add Data](https://firebase.google.com/docs/firestore/manage-data/add-data)

#### Read Documents

```kotlin
// Get a single document
when (val result = db.collection("users").document("user123").get()) {
    is FirebaseResult.Success -> {
        val snapshot = result.data
        if (snapshot.exists) {
            val data: Map<String, Any?>? = snapshot.data()
            val name: String? = snapshot.get("name")
            val age: Long? = snapshot.get("age")
            println("User: $name, Age: $age")
        } else {
            println("Document does not exist")
        }
    }
    is FirebaseResult.Failure -> println("Error: ${result.exception}")
}

// Get from specific source
val doc = db.collection("users").document("user123")
doc.get(Source.SERVER)   // Force server fetch
doc.get(Source.CACHE)    // Force cache read
doc.get(Source.DEFAULT)  // Server with cache fallback

// Real-time listener
db.collection("users").document("user123")
    .snapshots()
    .collect { snapshot ->
        if (snapshot.exists) {
            println("Document updated: ${snapshot.data()}")
        }
    }

// Include metadata changes
db.collection("users").document("user123")
    .snapshots(includeMetadataChanges = true)
    .collect { snapshot ->
        println("From cache: ${snapshot.metadata.isFromCache}")
        println("Has pending writes: ${snapshot.metadata.hasPendingWrites}")
    }
```

> **Firebase Documentation:** [Get Data](https://firebase.google.com/docs/firestore/query-data/get-data)

#### Query Documents

```kotlin
// Basic query
val query = db.collection("users")
    .whereEqualTo("city", "Paris")
    .orderBy("name", Direction.ASCENDING)
    .limit(10)

when (val result = query.get()) {
    is FirebaseResult.Success -> {
        val snapshot = result.data
        println("Found ${snapshot.size} documents")

        snapshot.documents.forEach { doc ->
            println("${doc.id}: ${doc.data()}")
        }
    }
    is FirebaseResult.Failure -> println("Error: ${result.exception}")
}

// Query operators
db.collection("users")
    .whereEqualTo("active", true)
    .whereNotEqualTo("role", "admin")
    .whereLessThan("age", 30)
    .whereLessThanOrEqualTo("score", 100)
    .whereGreaterThan("loginCount", 5)
    .whereGreaterThanOrEqualTo("level", 10)
    .whereArrayContains("tags", "premium")
    .whereArrayContainsAny("tags", listOf("vip", "premium"))
    .whereIn("status", listOf("active", "pending"))
    .whereNotIn("status", listOf("banned", "deleted"))

// Ordering
db.collection("posts")
    .orderBy("createdAt", Direction.DESCENDING)
    .orderBy("title", Direction.ASCENDING)

// Pagination
db.collection("users")
    .orderBy("name")
    .limit(25)
    .startAfter("John")
    .endBefore("Mike")

// Cursor pagination with document snapshot
val lastDoc = previousQuerySnapshot.documents.last()
db.collection("users")
    .orderBy("name")
    .startAfter(lastDoc)
    .limit(25)

// Real-time query listener
db.collection("messages")
    .whereEqualTo("roomId", "room123")
    .orderBy("timestamp", Direction.DESCENDING)
    .limit(50)
    .snapshots()
    .collect { snapshot ->
        // Handle document changes
        snapshot.documentChanges.forEach { change ->
            when (change.type) {
                DocumentChangeType.ADDED -> println("New: ${change.document.id}")
                DocumentChangeType.MODIFIED -> println("Modified: ${change.document.id}")
                DocumentChangeType.REMOVED -> println("Removed: ${change.document.id}")
            }
        }
    }
```

> **Firebase Documentation:** [Queries](https://firebase.google.com/docs/firestore/query-data/queries) | [Order and Limit](https://firebase.google.com/docs/firestore/query-data/order-limit-data)

#### Update Documents

```kotlin
// Update specific fields
db.collection("users").document("user123").update(
    mapOf(
        "name" to "Jane Doe",
        "age" to 31,
        "updatedAt" to FieldValue.serverTimestamp()
    )
)

// Update with vararg syntax
db.collection("users").document("user123").update(
    "name" to "Jane Doe",
    "age" to 31
)

// Field operations
db.collection("users").document("user123").update(
    mapOf(
        "loginCount" to FieldValue.increment(1),           // Increment number
        "balance" to FieldValue.increment(-10.5),          // Decrement number
        "tags" to FieldValue.arrayUnion("newTag"),         // Add to array
        "oldTags" to FieldValue.arrayRemove("deprecatedTag"), // Remove from array
        "lastLogin" to FieldValue.serverTimestamp(),       // Server timestamp
        "tempField" to FieldValue.delete()                 // Delete field
    )
)
```

> **Firebase Documentation:** [Update Data](https://firebase.google.com/docs/firestore/manage-data/add-data#update-data)

#### Delete Documents

```kotlin
// Delete a document
when (val result = db.collection("users").document("user123").delete()) {
    is FirebaseResult.Success -> println("Document deleted")
    is FirebaseResult.Failure -> println("Error: ${result.exception}")
}

// Delete a field
db.collection("users").document("user123").update(
    "temporaryData" to FieldValue.delete()
)
```

> **Firebase Documentation:** [Delete Data](https://firebase.google.com/docs/firestore/manage-data/delete-data)

#### Transactions

Transactions ensure atomic read-modify-write operations.

```kotlin
when (val result = db.runTransaction { transaction ->
    // Read
    val userDoc = db.collection("users").document("user123")
    val snapshot = transaction.get(userDoc)
    val currentBalance = snapshot.get<Long>("balance") ?: 0L

    // Validate
    if (currentBalance < 100) {
        throw IllegalStateException("Insufficient balance")
    }

    // Write
    transaction.update(userDoc, mapOf("balance" to currentBalance - 100))

    // Return value
    currentBalance - 100
}) {
    is FirebaseResult.Success -> println("New balance: ${result.data}")
    is FirebaseResult.Failure -> println("Transaction failed: ${result.exception}")
}
```

> **Firebase Documentation:** [Transactions](https://firebase.google.com/docs/firestore/manage-data/transactions)

#### Batch Writes

Batch writes perform multiple operations atomically.

```kotlin
val batch = db.batch()

// Set
batch.set(
    db.collection("users").document("user1"),
    mapOf("name" to "User 1", "active" to true)
)

// Update
batch.update(
    db.collection("users").document("user2"),
    mapOf("lastSeen" to FieldValue.serverTimestamp())
)

// Delete
batch.delete(db.collection("users").document("user3"))

// Commit
when (val result = batch.commit()) {
    is FirebaseResult.Success -> println("Batch committed successfully")
    is FirebaseResult.Failure -> println("Batch failed: ${result.exception}")
}
```

> **Firebase Documentation:** [Batched Writes](https://firebase.google.com/docs/firestore/manage-data/transactions#batched-writes)

#### Firestore Settings

```kotlin
import com.riadmahi.firebase.firestore.FirestoreSettings
import com.riadmahi.firebase.firestore.LocalCacheSettings
import com.riadmahi.firebase.firestore.MemoryGarbageCollectorSettings

// Configure with persistent cache (default)
db.settings = FirestoreSettings(
    cacheSettings = LocalCacheSettings.Persistent(
        sizeBytes = 100 * 1024 * 1024  // 100 MB
    )
)

// Configure with memory-only cache
db.settings = FirestoreSettings(
    cacheSettings = LocalCacheSettings.Memory(
        garbageCollectorSettings = MemoryGarbageCollectorSettings.Lru(
            sizeBytes = 50 * 1024 * 1024  // 50 MB
        )
    )
)

// Eager garbage collection (removes data immediately when not needed)
db.settings = FirestoreSettings(
    cacheSettings = LocalCacheSettings.Memory(
        garbageCollectorSettings = MemoryGarbageCollectorSettings.Eager
    )
)

// Emulator support
db.useEmulator("10.0.2.2", 8080)  // Android emulator
db.useEmulator("localhost", 8080)  // iOS simulator

// Other operations
db.waitForPendingWrites()  // Wait for offline writes to sync
db.clearPersistence()      // Clear local cache
db.terminate()             // Terminate the instance
```

> **Firebase Documentation:** [Offline Persistence](https://firebase.google.com/docs/firestore/manage-data/enable-offline) | [Emulator](https://firebase.google.com/docs/emulator-suite/connect_firestore)

---

## CLI Tool

The `kfire` CLI provides a beautiful, interactive experience for configuring Firebase in your KMP projects. Inspired by the best CLI tools like FlutterFire and Claude Code.

### Installation

```bash
# Build the CLI
./gradlew :firebase-cli:installDist

# Add to PATH (choose one)

# Option 1: Symlink (recommended)
sudo ln -sf $(pwd)/firebase-cli/build/install/kfire/bin/kfire /usr/local/bin/kfire

# Option 2: Add to .zshrc/.bashrc
echo 'export PATH="$PATH:/path/to/firebase-cli/build/install/kfire/bin"' >> ~/.zshrc
```

### Commands

#### `kfire init` (Recommended)

Full onboarding wizard - get from zero to a working Firebase setup in minutes.

```bash
kfire init
```

This interactive wizard will:
1. Check prerequisites (firebase-tools, authentication)
2. Detect your KMP project structure
3. Let you select a Firebase project
4. Choose which modules you need (Auth, Firestore)
5. Generate all configuration files
6. Update your Gradle dependencies
7. Create sample initialization code

```
╭────────────────────────────────────────────────────────────╮
│                                                            │
│   Welcome to KFire - Firebase for Kotlin Multiplatform     │
│                                                            │
│   This wizard will help you set up Firebase in your        │
│   KMP project in just a few steps.                         │
│                                                            │
╰────────────────────────────────────────────────────────────╯

◆ Pre-flight Checks

  ℹ Checking firebase-tools...
  ✓ firebase-tools is installed
  ℹ Checking authentication...
  ✓ Authenticated with Firebase
  ℹ Scanning project...
  ✓ Android: com.example.myapp
  ✓ iOS: com.example.myapp
```

#### `kfire login`

Authenticate with Firebase.

```bash
kfire login
```

#### `kfire configure`

Quick configuration for existing projects.

```bash
# Interactive mode
kfire configure

# With options
kfire configure \
  --project=my-firebase-project \
  --android-package=com.example.app \
  --ios-bundle-id=com.example.app \
  --platforms=android,ios \
  --modules=core,auth,firestore

# Non-interactive mode (CI/CD)
kfire configure -y
```

**Options:**

| Option | Description |
|--------|-------------|
| `--path, -p` | Path to project root (default: `.`) |
| `--project` | Firebase project ID |
| `--android-package` | Android package name (auto-detected) |
| `--ios-bundle-id` | iOS bundle ID (auto-detected) |
| `--platforms` | Platforms to configure: `android,ios` |
| `--modules, -m` | Firebase modules: `core,auth,firestore` |
| `--yes, -y` | Accept defaults, run non-interactively |

### CLI Features

- **Beautiful UI** - Colored output, boxes, tables, and clear formatting
- **Interactive prompts** - Easy selection with sensible defaults
- **Smart detection** - Auto-detects package names and bundle IDs
- **Progress feedback** - Clear step-by-step progress indication
- **Helpful errors** - Troubleshooting tips when things go wrong
- **Sample code generation** - Creates `FirebaseInit.kt` with best practices

---

## Error Handling

### FirebaseResult

All async operations return `FirebaseResult<T>`, a sealed class for type-safe error handling.

```kotlin
sealed class FirebaseResult<out T> {
    data class Success<T>(val data: T) : FirebaseResult<T>()
    data class Failure(val exception: FirebaseException) : FirebaseResult<Nothing>()
}
```

#### Usage

```kotlin
when (val result = auth.signInWithEmailAndPassword(email, password)) {
    is FirebaseResult.Success -> {
        val authResult = result.data
        val user = authResult.user
        println("Welcome, ${user.displayName}")
    }
    is FirebaseResult.Failure -> {
        val exception = result.exception
        showError(exception.message)
    }
}

// Helper extensions
val user = result.getOrNull()                    // T?
val user = result.getOrDefault(defaultUser)     // T
val user = result.getOrElse { createGuest() }   // T

if (result.isSuccess) { /* ... */ }
if (result.isFailure) { /* ... */ }
```

### Auth Exceptions

```kotlin
when (val exception = result.exception) {
    is AuthException.InvalidCredential -> "Invalid email or password"
    is AuthException.UserNotFound -> "No account found with this email"
    is AuthException.WrongPassword -> "Incorrect password"
    is AuthException.EmailAlreadyInUse -> "Email is already registered"
    is AuthException.WeakPassword -> "Password is too weak"
    is AuthException.InvalidEmail -> "Invalid email format"
    is AuthException.TooManyRequests -> "Too many attempts. Try again later"
    is AuthException.UserDisabled -> "This account has been disabled"
    is AuthException.RequiresRecentLogin -> "Please sign in again to continue"
    is AuthException.NetworkError -> "Network error. Check your connection"
    is AuthException.Unknown -> "An unexpected error occurred"
}
```

### Firestore Exceptions

```kotlin
when (val exception = result.exception) {
    is FirestoreException.NotFound -> "Document not found"
    is FirestoreException.PermissionDenied -> "Permission denied"
    is FirestoreException.AlreadyExists -> "Document already exists"
    is FirestoreException.Unavailable -> "Service temporarily unavailable"
    is FirestoreException.Cancelled -> "Operation was cancelled"
    is FirestoreException.DeadlineExceeded -> "Operation timed out"
    is FirestoreException.InvalidArgument -> "Invalid data provided"
    is FirestoreException.Unknown -> "An unexpected error occurred"
}
```

---

## Architecture

```
kfire/
├── firebase-core/           # Core SDK
│   ├── commonMain/          # expect declarations
│   ├── androidMain/         # actual implementations (Firebase Android SDK)
│   └── iosMain/             # actual implementations (Firebase iOS SDK via cinterop)
├── firebase-auth/           # Authentication SDK
│   ├── commonMain/
│   ├── androidMain/
│   └── iosMain/
├── firebase-firestore/      # Firestore SDK
│   ├── commonMain/
│   ├── androidMain/
│   └── iosMain/
├── firebase-cli/            # CLI tool (JVM)
└── demo/                    # Demo application
    ├── shared/              # Shared KMP code
    └── androidApp/          # Android app
```

### expect/actual Pattern

KFire uses Kotlin's `expect`/`actual` mechanism for platform-specific implementations:

```kotlin
// commonMain - API declaration
expect class FirebaseAuth {
    val currentUser: FirebaseUser?
    suspend fun signInWithEmailAndPassword(email: String, password: String): FirebaseResult<AuthResult>
}

// androidMain - Android implementation
actual class FirebaseAuth(private val android: com.google.firebase.auth.FirebaseAuth) {
    actual val currentUser: FirebaseUser?
        get() = android.currentUser?.let { FirebaseUser(it) }

    actual suspend fun signInWithEmailAndPassword(
        email: String,
        password: String
    ): FirebaseResult<AuthResult> = runCatching {
        android.signInWithEmailAndPassword(email, password).await().toAuthResult()
    }
}

// iosMain - iOS implementation (via cinterop)
actual class FirebaseAuth(private val ios: FIRAuth) {
    actual val currentUser: FirebaseUser?
        get() = ios.currentUser?.let { FirebaseUser(it) }

    actual suspend fun signInWithEmailAndPassword(
        email: String,
        password: String
    ): FirebaseResult<AuthResult> = suspendCoroutine { continuation ->
        ios.signInWithEmail(email, password) { result, error ->
            // Handle result
        }
    }
}
```

---

## Roadmap

### Modules

- [x] Firebase Core
- [x] Firebase Auth
- [x] Firebase Firestore
- [ ] Firebase Storage
- [ ] Firebase Messaging (FCM)
- [ ] Firebase Analytics
- [ ] Firebase Remote Config
- [ ] Firebase Crashlytics

### Features

- [ ] Complete iOS implementation
- [ ] Offline persistence improvements
- [ ] Multi-database support
- [ ] Compound queries
- [ ] Full-text search integration

### Distribution

- [ ] Maven Central publication
- [ ] Homebrew formula for CLI
- [ ] CocoaPods distribution
- [ ] Documentation website

---

## Contributing

Contributions are welcome! Please read our [Contributing Guide](CONTRIBUTING.md) for details.

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/amazing-feature`
3. Commit your changes: `git commit -m 'Add amazing feature'`
4. Push to the branch: `git push origin feature/amazing-feature`
5. Open a Pull Request

---

## License

```
MIT License

Copyright (c) 2024 Riad Mahi

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

---

<p align="center">
  Made with ❤️ for the Kotlin Multiplatform community
</p>

<p align="center">
  <a href="https://firebase.google.com/docs">Firebase Documentation</a> •
  <a href="https://kotlinlang.org/docs/multiplatform.html">Kotlin Multiplatform</a> •
  <a href="https://github.com/riadmahi/kfire/issues">Report Bug</a>
</p>
