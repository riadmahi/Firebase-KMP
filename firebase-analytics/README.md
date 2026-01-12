# Firebase Analytics

Analytics for KFire - understand user behavior in your app.

## Installation

```kotlin
// build.gradle.kts
commonMain.dependencies {
    implementation("com.riadmahi.firebase:firebase-core:1.0.0")
    implementation("com.riadmahi.firebase:firebase-analytics:1.0.0")
}
```

## Usage

### Get Analytics Instance

```kotlin
import com.riadmahi.firebase.analytics.FirebaseAnalytics

val analytics = FirebaseAnalytics.getInstance()
```

### Log Events

```kotlin
// Log a custom event
analytics.logEvent("level_complete") {
    param("level_name", "World 1-1")
    param("score", 9000)
    param("time_spent", 120.5)
}

// Log a predefined event
analytics.logEvent(AnalyticsEvent.SELECT_CONTENT) {
    param(AnalyticsParam.ITEM_ID, "shirt_123")
    param(AnalyticsParam.CONTENT_TYPE, "product")
}

// Log purchase
analytics.logEvent(AnalyticsEvent.PURCHASE) {
    param(AnalyticsParam.CURRENCY, "USD")
    param(AnalyticsParam.VALUE, 9.99)
    param(AnalyticsParam.TRANSACTION_ID, "T12345")
}
```

### Set User Properties

```kotlin
// Set a custom user property
analytics.setUserProperty("favorite_food", "pizza")

// Set user ID for cross-platform tracking
analytics.setUserId("user_12345")
```

### Screen Tracking

```kotlin
analytics.setCurrentScreen("HomeScreen", "MainActivity")
```

### Default Parameters

```kotlin
// Set parameters included with every event
analytics.setDefaultEventParameters(mapOf(
    "app_version" to "1.0.0",
    "environment" to "production"
))

// Clear default parameters
analytics.setDefaultEventParameters(null)
```

### Data Collection

```kotlin
// Disable analytics collection
analytics.setAnalyticsCollectionEnabled(false)

// Reset all analytics data
analytics.resetAnalyticsData()
```

### Get App Instance ID

```kotlin
analytics.getAppInstanceId { instanceId ->
    println("Instance ID: $instanceId")
}
```

## Predefined Events

```kotlin
object AnalyticsEvent {
    const val ADD_TO_CART = "add_to_cart"
    const val BEGIN_CHECKOUT = "begin_checkout"
    const val LEVEL_START = "level_start"
    const val LEVEL_END = "level_end"
    const val LOGIN = "login"
    const val PURCHASE = "purchase"
    const val SCREEN_VIEW = "screen_view"
    const val SEARCH = "search"
    const val SELECT_CONTENT = "select_content"
    const val SHARE = "share"
    const val SIGN_UP = "sign_up"
    const val TUTORIAL_BEGIN = "tutorial_begin"
    const val TUTORIAL_COMPLETE = "tutorial_complete"
    // ... and more
}
```

## Predefined Parameters

```kotlin
object AnalyticsParam {
    const val ITEM_ID = "item_id"
    const val ITEM_NAME = "item_name"
    const val CONTENT_TYPE = "content_type"
    const val CURRENCY = "currency"
    const val VALUE = "value"
    const val PRICE = "price"
    const val QUANTITY = "quantity"
    const val LEVEL = "level"
    const val SCORE = "score"
    const val SEARCH_TERM = "search_term"
    // ... and more
}
```

## API Reference

| Method | Description |
|--------|-------------|
| `logEvent(name, params)` | Log an event |
| `setUserProperty(name, value)` | Set user property |
| `setUserId(id)` | Set user ID |
| `setCurrentScreen(name, class)` | Set current screen |
| `setDefaultEventParameters(params)` | Set default params |
| `setAnalyticsCollectionEnabled(enabled)` | Enable/disable collection |
| `resetAnalyticsData()` | Clear analytics data |
| `getAppInstanceId(callback)` | Get app instance ID |

## See Also

- [Analytics Documentation](https://firebase.google.com/docs/analytics)
