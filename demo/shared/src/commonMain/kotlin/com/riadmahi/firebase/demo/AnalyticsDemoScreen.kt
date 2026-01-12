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
import com.riadmahi.firebase.analytics.AnalyticsEvent
import com.riadmahi.firebase.analytics.AnalyticsParam
import com.riadmahi.firebase.analytics.FirebaseAnalytics
import com.riadmahi.firebase.analytics.logEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsDemoScreen(onBack: () -> Unit) {
    val analytics = remember { FirebaseAnalytics.getInstance() }
    var statusMessage by remember { mutableStateOf("Ready to log events") }
    var eventCount by remember { mutableStateOf(0) }
    var appInstanceId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        analytics.getAppInstanceId { id ->
            appInstanceId = id
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Analytics Demo") },
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
                    Text(text = statusMessage)
                    Text(text = "Events logged: $eventCount")
                    appInstanceId?.let {
                        Text(
                            text = "Instance ID: ${it.take(20)}...",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Divider()
            Text("Log Events", style = MaterialTheme.typography.titleMedium)

            // Log Screen View
            Button(
                onClick = {
                    analytics.setCurrentScreen("AnalyticsDemo", "AnalyticsDemoScreen")
                    eventCount++
                    statusMessage = "Logged: screen_view (AnalyticsDemo)"
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Log Screen View")
            }

            // Log Custom Event
            Button(
                onClick = {
                    analytics.logEvent("demo_button_clicked") {
                        param("button_name", "custom_event")
                        param("timestamp", currentTimeMillis())
                    }
                    eventCount++
                    statusMessage = "Logged: demo_button_clicked"
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Log Custom Event")
            }

            // Log Purchase Event
            Button(
                onClick = {
                    analytics.logEvent(AnalyticsEvent.PURCHASE) {
                        param(AnalyticsParam.TRANSACTION_ID, "TXN_${currentTimeMillis()}")
                        param(AnalyticsParam.VALUE, 29.99)
                        param(AnalyticsParam.CURRENCY, "USD")
                        param(AnalyticsParam.ITEM_NAME, "Premium Subscription")
                    }
                    eventCount++
                    statusMessage = "Logged: purchase event"
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Log Purchase Event")
            }

            // Log Level Up
            Button(
                onClick = {
                    analytics.logEvent(AnalyticsEvent.LEVEL_UP) {
                        param(AnalyticsParam.LEVEL, 5)
                        param(AnalyticsParam.CHARACTER, "Hero")
                    }
                    eventCount++
                    statusMessage = "Logged: level_up (Level 5)"
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Log Level Up Event")
            }

            Divider()
            Text("User Properties", style = MaterialTheme.typography.titleMedium)

            // Set User Property
            Button(
                onClick = {
                    analytics.setUserProperty("favorite_feature", "analytics")
                    statusMessage = "Set user property: favorite_feature = analytics"
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Set User Property")
            }

            // Set User ID
            Button(
                onClick = {
                    analytics.setUserId("demo_user_${currentTimeMillis() % 1000}")
                    statusMessage = "Set user ID"
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Set User ID")
            }

            Divider()
            Text("Settings", style = MaterialTheme.typography.titleMedium)

            // Reset Analytics
            OutlinedButton(
                onClick = {
                    analytics.resetAnalyticsData()
                    eventCount = 0
                    statusMessage = "Analytics data reset"
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Reset Analytics Data")
            }
        }
    }
}

// Helper to get current time
private fun currentTimeMillis(): Long = TimeUtils.currentTimeMillis()
