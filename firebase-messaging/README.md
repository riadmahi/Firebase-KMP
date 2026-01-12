# Firebase Messaging

Cloud Messaging (FCM) for KFire - send notifications and data messages.

## Installation

```kotlin
// build.gradle.kts
commonMain.dependencies {
    implementation("com.riadmahi.firebase:firebase-core:1.0.0")
    implementation("com.riadmahi.firebase:firebase-messaging:1.0.0")
}
```

## Usage

### Get Messaging Instance

```kotlin
import com.riadmahi.firebase.messaging.FirebaseMessaging

val messaging = FirebaseMessaging.getInstance()
```

### Get FCM Token

```kotlin
val result = messaging.getToken()
when (result) {
    is FirebaseResult.Success -> {
        val token = result.data
        println("FCM Token: $token")
        // Send this token to your server
    }
    is FirebaseResult.Failure -> println("Error: ${result.exception}")
}
```

### Subscribe to Topics

```kotlin
// Subscribe to a topic
val result = messaging.subscribeToTopic("news")
when (result) {
    is FirebaseResult.Success -> println("Subscribed to news")
    is FirebaseResult.Failure -> println("Error: ${result.exception}")
}

// Unsubscribe from a topic
messaging.unsubscribeFromTopic("news")
```

### Delete Token

```kotlin
val result = messaging.deleteToken()
when (result) {
    is FirebaseResult.Success -> println("Token deleted")
    is FirebaseResult.Failure -> println("Error: ${result.exception}")
}
```

### Auto-Initialization

```kotlin
// Disable auto token generation
messaging.isAutoInitEnabled = false

// Re-enable
messaging.isAutoInitEnabled = true
```

### Remote Message

```kotlin
data class RemoteMessage(
    val messageId: String?,
    val from: String?,
    val data: Map<String, String>,
    val notification: Notification?,
    val sentTime: Long,
    val ttl: Int
)

data class Notification(
    val title: String?,
    val body: String?,
    val imageUrl: String?,
    val channelId: String?  // Android only
)
```

## Platform Setup

### Android

Add to `AndroidManifest.xml`:

```xml
<service
    android:name=".MyFirebaseMessagingService"
    android:exported="false">
    <intent-filter>
        <action android:name="com.google.firebase.MESSAGING_EVENT" />
    </intent-filter>
</service>
```

### iOS

Enable Push Notifications in Xcode and add to `AppDelegate`:

```swift
UNUserNotificationCenter.current().requestAuthorization(options: [.alert, .badge, .sound]) { _, _ in }
UIApplication.shared.registerForRemoteNotifications()
```

## API Reference

| Method | Description |
|--------|-------------|
| `getToken()` | Get the FCM registration token |
| `deleteToken()` | Delete the current token |
| `subscribeToTopic(topic)` | Subscribe to a topic |
| `unsubscribeFromTopic(topic)` | Unsubscribe from a topic |
| `isAutoInitEnabled` | Enable/disable auto token generation |

## See Also

- [Cloud Messaging Documentation](https://firebase.google.com/docs/cloud-messaging)
