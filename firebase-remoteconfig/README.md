# Firebase Remote Config

Remote Config for KFire - change your app without deploying.

## Installation

```kotlin
// build.gradle.kts
commonMain.dependencies {
    implementation("com.riadmahi.firebase:firebase-core:1.0.0")
    implementation("com.riadmahi.firebase:firebase-remoteconfig:1.0.0")
}
```

## Usage

### Get Remote Config Instance

```kotlin
import com.riadmahi.firebase.remoteconfig.FirebaseRemoteConfig

val config = FirebaseRemoteConfig.getInstance()
```

### Set Default Values

```kotlin
config.setDefaults(mapOf(
    "welcome_message" to "Welcome!",
    "show_banner" to true,
    "max_items" to 10,
    "price_multiplier" to 1.0
))
```

### Fetch and Activate

```kotlin
// Fetch and activate in one call
val result = config.fetchAndActivate()
when (result) {
    is FirebaseResult.Success -> {
        val activated = result.data
        println("Config activated: $activated")
    }
    is FirebaseResult.Failure -> println("Error: ${result.exception}")
}

// Or fetch and activate separately
config.fetch()
config.activate()
```

### Get Values

```kotlin
// String value
val message = config.getString("welcome_message")

// Boolean value
val showBanner = config.getBoolean("show_banner")

// Number values
val maxItems = config.getLong("max_items")
val multiplier = config.getDouble("price_multiplier")
```

### Get All Values

```kotlin
val allValues = config.getAll()
allValues.forEach { (key, value) ->
    println("$key: ${value.asString} (source: ${value.source})")
}
```

### Check Value Source

```kotlin
val source = config.getValueSource("welcome_message")
when (source) {
    ValueSource.REMOTE -> println("Value from server")
    ValueSource.DEFAULT -> println("Value from defaults")
    ValueSource.STATIC -> println("No value found")
}
```

### Development Mode

```kotlin
// Reduce fetch interval for development
config.setMinimumFetchInterval(0) // Fetch every time

// Fetch with custom interval
config.fetch(minimumFetchIntervalInSeconds = 3600) // 1 hour
```

### Reset Config

```kotlin
config.reset()
```

## Value Sources

| Source | Description |
|--------|-------------|
| `REMOTE` | Value fetched from Firebase console |
| `DEFAULT` | Value from `setDefaults()` |
| `STATIC` | No value found, using type default |

## Example: Feature Flags

```kotlin
// Set defaults
config.setDefaults(mapOf(
    "new_checkout_enabled" to false,
    "dark_mode_enabled" to false,
    "max_cart_items" to 50
))

// Fetch latest config
config.fetchAndActivate()

// Use feature flags
if (config.getBoolean("new_checkout_enabled")) {
    showNewCheckout()
} else {
    showLegacyCheckout()
}

val maxItems = config.getLong("max_cart_items").toInt()
```

## API Reference

| Method | Description |
|--------|-------------|
| `setDefaults(map)` | Set default values |
| `fetch()` | Fetch from server |
| `activate()` | Activate fetched config |
| `fetchAndActivate()` | Fetch and activate |
| `getString(key)` | Get string value |
| `getBoolean(key)` | Get boolean value |
| `getLong(key)` | Get long value |
| `getDouble(key)` | Get double value |
| `getAll()` | Get all values |
| `getValueSource(key)` | Get value source |
| `setMinimumFetchInterval(seconds)` | Set fetch interval |
| `reset()` | Reset to defaults |

## See Also

- [Remote Config Documentation](https://firebase.google.com/docs/remote-config)
