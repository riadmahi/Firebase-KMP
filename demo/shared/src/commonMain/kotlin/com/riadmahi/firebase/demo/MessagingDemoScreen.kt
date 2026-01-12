package com.riadmahi.firebase.demo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.riadmahi.firebase.core.FirebaseResult
import com.riadmahi.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagingDemoScreen(onBack: () -> Unit) {
    val messaging = remember { FirebaseMessaging.getInstance() }
    val scope = rememberCoroutineScope()

    var statusMessage by remember { mutableStateOf("Ready") }
    var isLoading by remember { mutableStateOf(false) }
    var fcmToken by remember { mutableStateOf<String?>(null) }
    var autoInitEnabled by remember { mutableStateOf(true) }
    var topicInput by remember { mutableStateOf("news") }

    // Get token on launch
    LaunchedEffect(Unit) {
        autoInitEnabled = messaging.isAutoInitEnabled
        isLoading = true
        when (val result = messaging.getToken()) {
            is FirebaseResult.Success -> {
                fcmToken = result.data
                statusMessage = "Token retrieved"
            }
            is FirebaseResult.Failure -> {
                statusMessage = "Failed to get token: ${result.exception.message}"
            }
        }
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Messaging Demo") },
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
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Status",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (isLoading) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Loading...")
                        }
                    } else {
                        Text(text = statusMessage)
                    }
                }
            }

            Divider()
            Text("FCM Token", style = MaterialTheme.typography.titleMedium)

            // Token Card
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (fcmToken != null) {
                        SelectionContainer {
                            Text(
                                text = fcmToken!!,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tap and hold to select and copy",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            text = "No token available",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            // Refresh Token
            Button(
                onClick = {
                    scope.launch {
                        isLoading = true
                        statusMessage = "Getting token..."
                        when (val result = messaging.getToken()) {
                            is FirebaseResult.Success -> {
                                fcmToken = result.data
                                statusMessage = "Token refreshed"
                            }
                            is FirebaseResult.Failure -> {
                                statusMessage = "Error: ${result.exception.message}"
                            }
                        }
                        isLoading = false
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                Text("Refresh Token")
            }

            // Delete Token
            OutlinedButton(
                onClick = {
                    scope.launch {
                        isLoading = true
                        statusMessage = "Deleting token..."
                        when (val result = messaging.deleteToken()) {
                            is FirebaseResult.Success -> {
                                fcmToken = null
                                statusMessage = "Token deleted. Get a new one!"
                            }
                            is FirebaseResult.Failure -> {
                                statusMessage = "Error: ${result.exception.message}"
                            }
                        }
                        isLoading = false
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                Text("Delete Token")
            }

            Divider()
            Text("Topic Subscription", style = MaterialTheme.typography.titleMedium)

            // Topic Input
            OutlinedTextField(
                value = topicInput,
                onValueChange = { topicInput = it },
                label = { Text("Topic Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Subscribe
                Button(
                    onClick = {
                        scope.launch {
                            isLoading = true
                            statusMessage = "Subscribing to '$topicInput'..."
                            when (val result = messaging.subscribeToTopic(topicInput)) {
                                is FirebaseResult.Success -> {
                                    statusMessage = "Subscribed to '$topicInput'"
                                }
                                is FirebaseResult.Failure -> {
                                    statusMessage = "Error: ${result.exception.message}"
                                }
                            }
                            isLoading = false
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading && topicInput.isNotBlank()
                ) {
                    Text("Subscribe")
                }

                // Unsubscribe
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            isLoading = true
                            statusMessage = "Unsubscribing from '$topicInput'..."
                            when (val result = messaging.unsubscribeFromTopic(topicInput)) {
                                is FirebaseResult.Success -> {
                                    statusMessage = "Unsubscribed from '$topicInput'"
                                }
                                is FirebaseResult.Failure -> {
                                    statusMessage = "Error: ${result.exception.message}"
                                }
                            }
                            isLoading = false
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading && topicInput.isNotBlank()
                ) {
                    Text("Unsubscribe")
                }
            }

            Divider()
            Text("Settings", style = MaterialTheme.typography.titleMedium)

            // Auto Init Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Auto Initialization")
                    Text(
                        text = "Automatically generate FCM tokens",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = autoInitEnabled,
                    onCheckedChange = { enabled ->
                        messaging.isAutoInitEnabled = enabled
                        autoInitEnabled = enabled
                        statusMessage = "Auto init ${if (enabled) "enabled" else "disabled"}"
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "How to Test",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "1. Copy the FCM token above\n" +
                              "2. Go to Firebase Console > Messaging\n" +
                              "3. Create a new campaign\n" +
                              "4. Send a test message to this token",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
