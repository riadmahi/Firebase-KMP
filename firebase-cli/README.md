# firebase-cli (kmpfire)

Command-line interface for configuring Firebase in Kotlin Multiplatform projects.

Similar to FlutterFire CLI, this tool automates Firebase setup for KMP projects.

## Installation

### Build from Source

```bash
# Build the CLI
./gradlew :firebase-cli:installDist

# Run the CLI
./firebase-cli/build/install/firebase-cli/bin/kmpfire --help
```

### Add to PATH (Optional)

```bash
# Add to your shell profile (.zshrc, .bashrc, etc.)
export PATH="$PATH:/path/to/firebase/firebase-cli/build/install/firebase-cli/bin"

# Now you can run from anywhere
kmpfire --help
```

## Commands

### kmpfire configure

Configure Firebase for your KMP project.

```bash
# Interactive mode
kmpfire configure

# With options
kmpfire configure \
  --project=my-firebase-project \
  --android-package=com.example.app \
  --ios-bundle-id=com.example.app \
  --platforms=android,ios

# Non-interactive mode (accept defaults)
kmpfire configure -y
```

#### Options

| Option | Description |
|--------|-------------|
| `-p, --path` | Path to KMP project root (default: current directory) |
| `--project` | Firebase project ID |
| `--android-package` | Android package name (auto-detected from build.gradle.kts) |
| `--ios-bundle-id` | iOS bundle identifier (auto-detected from Xcode project) |
| `--platforms` | Platforms to configure (comma-separated: android,ios) |
| `-y, --yes` | Accept all defaults, run non-interactively |

#### What it does

1. **Detects project structure** - Finds Android package name and iOS bundle ID
2. **Connects to Firebase** - Uses Firebase Management API
3. **Creates/selects Firebase apps** - For each platform
4. **Downloads configuration files**:
   - `google-services.json` for Android
   - `GoogleService-Info.plist` for iOS
5. **Places files** in correct locations

### kmpfire login

Authenticate with Firebase.

```bash
kmpfire login
```

This delegates to `firebase-tools` CLI for authentication. Make sure you have `firebase-tools` installed:

```bash
npm install -g firebase-tools
```

## Output Structure

After running `kmpfire configure`:

```
your-kmp-project/
├── composeApp/
│   └── src/
│       └── androidMain/
│           └── google-services.json    # Generated
├── iosApp/
│   └── iosApp/
│       └── GoogleService-Info.plist    # Generated
└── ...
```

## Prerequisites

1. **Firebase CLI** - For authentication
   ```bash
   npm install -g firebase-tools
   firebase login
   ```

2. **Firebase Project** - Create one at [Firebase Console](https://console.firebase.google.com)

3. **KMP Project** - With standard structure (composeApp, iosApp)

## Configuration File Formats

### google-services.json (Android)

```json
{
  "project_info": {
    "project_number": "123456789",
    "project_id": "my-project",
    "storage_bucket": "my-project.appspot.com"
  },
  "client": [{
    "client_info": {
      "mobilesdk_app_id": "1:123456789:android:abc123",
      "android_client_info": {
        "package_name": "com.example.app"
      }
    },
    "api_key": [{
      "current_key": "AIza..."
    }]
  }]
}
```

### GoogleService-Info.plist (iOS)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "...">
<plist version="1.0">
<dict>
    <key>API_KEY</key>
    <string>AIza...</string>
    <key>GCM_SENDER_ID</key>
    <string>123456789</string>
    <key>PROJECT_ID</key>
    <string>my-project</string>
    <key>BUNDLE_ID</key>
    <string>com.example.app</string>
    <key>GOOGLE_APP_ID</key>
    <string>1:123456789:ios:abc123</string>
</dict>
</plist>
```

## Troubleshooting

### "Firebase CLI not found"

Install Firebase CLI:
```bash
npm install -g firebase-tools
```

### "Not logged in"

Authenticate with Firebase:
```bash
firebase login
```

### "Project not found"

Make sure:
1. You're logged in with the correct account
2. The project ID is correct
3. You have access to the project

### "Package name mismatch"

The Android package in `google-services.json` must match your app's `applicationId` in `build.gradle.kts`.

## Development

### Build CLI

```bash
./gradlew :firebase-cli:build
```

### Run Tests

```bash
./gradlew :firebase-cli:test
```

### Create Distribution

```bash
./gradlew :firebase-cli:distZip
# Output: firebase-cli/build/distributions/firebase-cli.zip
```

## Architecture

```
firebase-cli/
└── src/main/kotlin/com/riadmahi/firebase/cli/
    ├── Main.kt              # Entry point
    ├── KmpFireCli.kt        # Root command
    ├── ConfigureCommand.kt  # configure command
    ├── LoginCommand.kt      # login command
    └── FirebaseApi.kt       # Firebase Management API client
```

### Dependencies

- **Clikt** - Command-line parsing
- **Ktor** - HTTP client for Firebase API
- **kotlinx.serialization** - JSON parsing
