package com.riadmahi.firebase.demo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.riadmahi.firebase.crashlytics.FirebaseCrashlytics

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrashlyticsDemoScreen(onBack: () -> Unit) {
    val crashlytics = remember { FirebaseCrashlytics.getInstance() }
    var statusMessage by remember { mutableStateOf("Ready") }
    var logCount by remember { mutableStateOf(0) }
    var crashedLastSession by remember { mutableStateOf(false) }
    var collectionEnabled by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        crashedLastSession = crashlytics.didCrashOnPreviousExecution()
        collectionEnabled = crashlytics.isCrashlyticsCollectionEnabled()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crashlytics Demo") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Status Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (crashedLastSession)
                        MaterialTheme.colorScheme.errorContainer
                    else MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Status",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = statusMessage)
                    Text(text = "Logs added: $logCount")
                    if (crashedLastSession) {
                        Text(
                            text = "App crashed in previous session!",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    Text(
                        text = "Collection: ${if (collectionEnabled) "Enabled" else "Disabled"}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Divider()
            Text("Logging", style = MaterialTheme.typography.titleMedium)

            // Log Message
            Button(
                onClick = {
                    crashlytics.log("User tapped log button at ${currentTime()}")
                    logCount++
                    statusMessage = "Added log message"
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add Log Message")
            }

            // Set Custom Keys
            Button(
                onClick = {
                    crashlytics.setCustomKey("screen", "CrashlyticsDemoScreen")
                    crashlytics.setCustomKey("button_taps", logCount)
                    crashlytics.setCustomKey("is_demo", true)
                    crashlytics.setCustomKey("timestamp", currentTime())
                    statusMessage = "Custom keys set"
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Set Custom Keys")
            }

            // Set User ID
            Button(
                onClick = {
                    crashlytics.setUserId("demo_user_123")
                    statusMessage = "User ID set to demo_user_123"
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Set User ID")
            }

            Divider()
            Text("Exceptions", style = MaterialTheme.typography.titleMedium)

            // Record Non-Fatal Exception
            Button(
                onClick = {
                    try {
                        throw IllegalStateException("This is a test exception from the demo app")
                    } catch (e: Exception) {
                        crashlytics.recordException(e)
                        statusMessage = "Non-fatal exception recorded"
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Record Non-Fatal Exception")
            }

            // Record Custom Exception
            Button(
                onClick = {
                    crashlytics.log("About to record custom exception")
                    crashlytics.setCustomKey("exception_type", "NetworkError")
                    try {
                        throw RuntimeException("Simulated network timeout")
                    } catch (e: Exception) {
                        crashlytics.recordException(e)
                        statusMessage = "Network error recorded"
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Record Network Error")
            }

            Divider()
            Text("Reports", style = MaterialTheme.typography.titleMedium)

            // Send Unsent Reports
            Button(
                onClick = {
                    crashlytics.sendUnsentReports()
                    statusMessage = "Sending unsent reports..."
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Send Unsent Reports")
            }

            // Delete Unsent Reports
            OutlinedButton(
                onClick = {
                    crashlytics.deleteUnsentReports()
                    statusMessage = "Deleted unsent reports"
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Delete Unsent Reports")
            }

            Divider()
            Text("Settings", style = MaterialTheme.typography.titleMedium)

            // Toggle Collection
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Crashlytics Collection")
                Switch(
                    checked = collectionEnabled,
                    onCheckedChange = { enabled ->
                        crashlytics.setCrashlyticsCollectionEnabled(enabled)
                        collectionEnabled = enabled
                        statusMessage = "Collection ${if (enabled) "enabled" else "disabled"}"
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Warning Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Test Crash",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = "The button below will crash the app. Use only for testing!",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            crashlytics.log("Test crash initiated by user")
                            throw RuntimeException("Test crash from Crashlytics demo")
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Force Crash (TEST ONLY)")
                    }
                }
            }
        }
    }
}

private fun currentTime(): String {
    return TimeUtils.currentTimeString()
}
