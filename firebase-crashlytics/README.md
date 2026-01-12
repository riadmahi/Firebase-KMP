# Firebase Crashlytics

Crashlytics for KFire - track and fix stability issues.

## Installation

```kotlin
// build.gradle.kts
commonMain.dependencies {
    implementation("com.riadmahi.firebase:firebase-core:1.0.0")
    implementation("com.riadmahi.firebase:firebase-crashlytics:1.0.0")
}
```

## Usage

### Get Crashlytics Instance

```kotlin
import com.riadmahi.firebase.crashlytics.FirebaseCrashlytics

val crashlytics = FirebaseCrashlytics.getInstance()
```

### Record Non-Fatal Exceptions

```kotlin
try {
    riskyOperation()
} catch (e: Exception) {
    crashlytics.recordException(e)
}
```

### Add Custom Keys

```kotlin
// Add context to crash reports
crashlytics.setCustomKey("screen", "checkout")
crashlytics.setCustomKey("user_level", 42)
crashlytics.setCustomKey("is_premium", true)
crashlytics.setCustomKey("cart_value", 99.99)

// Set multiple keys at once
crashlytics.setCustomKeys(mapOf(
    "last_action" to "add_to_cart",
    "item_count" to 3
))
```

### Add Log Messages

```kotlin
// Add breadcrumbs for debugging
crashlytics.log("User clicked checkout button")
crashlytics.log("Payment method: credit_card")
crashlytics.log("Order total: $99.99")
```

### Set User Identifier

```kotlin
// Associate crashes with a user
crashlytics.setUserId("user_12345")

// Clear user ID
crashlytics.setUserId(null)
```

### Data Collection

```kotlin
// Disable crash reporting
crashlytics.setCrashlyticsCollectionEnabled(false)

// Check if enabled
val isEnabled = crashlytics.isCrashlyticsCollectionEnabled()
```

### Manage Reports

```kotlin
// Check if app crashed previously
if (crashlytics.didCrashOnPreviousExecution()) {
    showCrashRecoveryMessage()
}

// Send unsent reports
crashlytics.sendUnsentReports()

// Delete unsent reports (e.g., for GDPR compliance)
crashlytics.deleteUnsentReports()
```

## Example: Complete Integration

```kotlin
class App {
    fun onCreate() {
        val crashlytics = FirebaseCrashlytics.getInstance()

        // Set user context
        crashlytics.setUserId(getCurrentUserId())
        crashlytics.setCustomKey("app_version", BuildConfig.VERSION_NAME)

        // Check for previous crash
        if (crashlytics.didCrashOnPreviousExecution()) {
            showCrashFeedbackDialog()
        }
    }

    fun onUserAction(action: String) {
        crashlytics.log("User action: $action")
    }

    fun onError(error: Exception, context: String) {
        crashlytics.setCustomKey("error_context", context)
        crashlytics.recordException(error)
    }
}
```

## Example: E-Commerce

```kotlin
fun processOrder(order: Order) {
    val crashlytics = FirebaseCrashlytics.getInstance()

    crashlytics.setCustomKeys(mapOf(
        "order_id" to order.id,
        "item_count" to order.items.size,
        "total" to order.total
    ))

    crashlytics.log("Processing order ${order.id}")

    try {
        paymentService.charge(order)
        crashlytics.log("Payment successful")
    } catch (e: PaymentException) {
        crashlytics.log("Payment failed: ${e.code}")
        crashlytics.recordException(e)
        throw e
    }
}
```

## API Reference

| Method | Description |
|--------|-------------|
| `recordException(throwable)` | Record non-fatal exception |
| `log(message)` | Add log message |
| `setCustomKey(key, value)` | Set custom key-value |
| `setCustomKeys(map)` | Set multiple keys |
| `setUserId(id)` | Set user identifier |
| `setCrashlyticsCollectionEnabled(enabled)` | Enable/disable collection |
| `isCrashlyticsCollectionEnabled()` | Check if enabled |
| `didCrashOnPreviousExecution()` | Check for previous crash |
| `sendUnsentReports()` | Send pending reports |
| `deleteUnsentReports()` | Delete pending reports |

## See Also

- [Crashlytics Documentation](https://firebase.google.com/docs/crashlytics)
