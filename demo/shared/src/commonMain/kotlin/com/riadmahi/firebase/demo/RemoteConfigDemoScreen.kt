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
import com.riadmahi.firebase.core.FirebaseResult
import com.riadmahi.firebase.remoteconfig.FirebaseRemoteConfig
import com.riadmahi.firebase.remoteconfig.ValueSource
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemoteConfigDemoScreen(onBack: () -> Unit) {
    val config = remember { FirebaseRemoteConfig.getInstance() }
    val scope = rememberCoroutineScope()

    var statusMessage by remember { mutableStateOf("Ready") }
    var isLoading by remember { mutableStateOf(false) }

    // Config values
    var welcomeMessage by remember { mutableStateOf("") }
    var featureEnabled by remember { mutableStateOf(false) }
    var maxItems by remember { mutableStateOf(0L) }
    var discountPercent by remember { mutableStateOf(0.0) }

    // Set defaults on launch
    LaunchedEffect(Unit) {
        config.setDefaults(mapOf(
            "welcome_message" to "Welcome to Firebase KMP!",
            "feature_enabled" to false,
            "max_items" to 10,
            "discount_percent" to 0.0
        ))
        config.setMinimumFetchInterval(60) // 1 minute for demo
        refreshValues(config) { w, f, m, d ->
            welcomeMessage = w
            featureEnabled = f
            maxItems = m
            discountPercent = d
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Remote Config Demo") },
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
                    containerColor = if (isLoading)
                        MaterialTheme.colorScheme.secondaryContainer
                    else MaterialTheme.colorScheme.primaryContainer
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
            Text("Current Values", style = MaterialTheme.typography.titleMedium)

            // Config Values Card
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    ConfigValueRow("welcome_message", welcomeMessage)
                    ConfigValueRow("feature_enabled", featureEnabled.toString())
                    ConfigValueRow("max_items", maxItems.toString())
                    ConfigValueRow("discount_percent", "$discountPercent%")
                }
            }

            Divider()
            Text("Actions", style = MaterialTheme.typography.titleMedium)

            // Fetch Config
            Button(
                onClick = {
                    scope.launch {
                        isLoading = true
                        statusMessage = "Fetching..."
                        when (val result = config.fetch()) {
                            is FirebaseResult.Success -> {
                                statusMessage = "Fetch successful! Activate to apply."
                            }
                            is FirebaseResult.Failure -> {
                                statusMessage = "Fetch failed: ${result.exception.message}"
                            }
                        }
                        isLoading = false
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                Text("Fetch Config")
            }

            // Activate Config
            Button(
                onClick = {
                    scope.launch {
                        isLoading = true
                        statusMessage = "Activating..."
                        when (val result = config.activate()) {
                            is FirebaseResult.Success -> {
                                statusMessage = if (result.data) {
                                    "New config activated!"
                                } else {
                                    "No new config to activate"
                                }
                                refreshValues(config) { w, f, m, d ->
                                    welcomeMessage = w
                                    featureEnabled = f
                                    maxItems = m
                                    discountPercent = d
                                }
                            }
                            is FirebaseResult.Failure -> {
                                statusMessage = "Activation failed: ${result.exception.message}"
                            }
                        }
                        isLoading = false
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                Text("Activate Config")
            }

            // Fetch & Activate
            Button(
                onClick = {
                    scope.launch {
                        isLoading = true
                        statusMessage = "Fetching and activating..."
                        when (val result = config.fetchAndActivate()) {
                            is FirebaseResult.Success -> {
                                statusMessage = if (result.data) {
                                    "New config fetched and activated!"
                                } else {
                                    "Config up to date"
                                }
                                refreshValues(config) { w, f, m, d ->
                                    welcomeMessage = w
                                    featureEnabled = f
                                    maxItems = m
                                    discountPercent = d
                                }
                            }
                            is FirebaseResult.Failure -> {
                                statusMessage = "Failed: ${result.exception.message}"
                            }
                        }
                        isLoading = false
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                Text("Fetch & Activate")
            }

            // Show Value Sources
            OutlinedButton(
                onClick = {
                    val sources = listOf(
                        "welcome_message" to config.getValueSource("welcome_message"),
                        "feature_enabled" to config.getValueSource("feature_enabled"),
                        "max_items" to config.getValueSource("max_items")
                    )
                    statusMessage = sources.joinToString("\n") { (k, v) -> "$k: $v" }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Show Value Sources")
            }
        }
    }
}

@Composable
private fun ConfigValueRow(key: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = key,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

private fun refreshValues(
    config: FirebaseRemoteConfig,
    onValues: (String, Boolean, Long, Double) -> Unit
) {
    onValues(
        config.getString("welcome_message"),
        config.getBoolean("feature_enabled"),
        config.getLong("max_items"),
        config.getDouble("discount_percent")
    )
}
