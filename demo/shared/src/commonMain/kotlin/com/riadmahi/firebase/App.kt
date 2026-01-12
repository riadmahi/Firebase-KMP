package com.riadmahi.firebase

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.riadmahi.firebase.demo.AnalyticsDemoScreen
import com.riadmahi.firebase.demo.AuthDemoScreen
import com.riadmahi.firebase.demo.CrashlyticsDemoScreen
import com.riadmahi.firebase.demo.FirestoreDemoScreen
import com.riadmahi.firebase.demo.MessagingDemoScreen
import com.riadmahi.firebase.demo.RemoteConfigDemoScreen
import com.riadmahi.firebase.demo.StorageDemoScreen
import org.jetbrains.compose.ui.tooling.preview.Preview

enum class DemoScreen {
    HOME,
    AUTH,
    FIRESTORE,
    STORAGE,
    MESSAGING,
    ANALYTICS,
    REMOTE_CONFIG,
    CRASHLYTICS
}

@Composable
@Preview
fun App() {
    MaterialTheme {
        var currentScreen by remember { mutableStateOf(DemoScreen.HOME) }

        when (currentScreen) {
            DemoScreen.HOME -> HomeScreen(
                onNavigate = { currentScreen = it }
            )
            DemoScreen.AUTH -> AuthDemoScreen(
                onBack = { currentScreen = DemoScreen.HOME }
            )
            DemoScreen.FIRESTORE -> FirestoreDemoScreen(
                onBack = { currentScreen = DemoScreen.HOME }
            )
            DemoScreen.STORAGE -> StorageDemoScreen(
                onBack = { currentScreen = DemoScreen.HOME }
            )
            DemoScreen.MESSAGING -> MessagingDemoScreen(
                onBack = { currentScreen = DemoScreen.HOME }
            )
            DemoScreen.ANALYTICS -> AnalyticsDemoScreen(
                onBack = { currentScreen = DemoScreen.HOME }
            )
            DemoScreen.REMOTE_CONFIG -> RemoteConfigDemoScreen(
                onBack = { currentScreen = DemoScreen.HOME }
            )
            DemoScreen.CRASHLYTICS -> CrashlyticsDemoScreen(
                onBack = { currentScreen = DemoScreen.HOME }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onNavigate: (DemoScreen) -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Firebase KMP Demo") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Firebase Kotlin Multiplatform",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Core Services
            Text(
                text = "Core Services",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth()
            )

            DemoButton(
                title = "Authentication",
                description = "Sign in, sign up, OAuth providers",
                onClick = { onNavigate(DemoScreen.AUTH) }
            )

            DemoButton(
                title = "Firestore",
                description = "NoSQL database, real-time sync",
                onClick = { onNavigate(DemoScreen.FIRESTORE) }
            )

            DemoButton(
                title = "Storage",
                description = "File upload, download, management",
                onClick = { onNavigate(DemoScreen.STORAGE) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Engagement
            Text(
                text = "Engagement",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth()
            )

            DemoButton(
                title = "Cloud Messaging",
                description = "Push notifications, topics",
                onClick = { onNavigate(DemoScreen.MESSAGING) }
            )

            DemoButton(
                title = "Analytics",
                description = "Event logging, user properties",
                onClick = { onNavigate(DemoScreen.ANALYTICS) }
            )

            DemoButton(
                title = "Remote Config",
                description = "Feature flags, A/B testing",
                onClick = { onNavigate(DemoScreen.REMOTE_CONFIG) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Quality
            Text(
                text = "Quality",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth()
            )

            DemoButton(
                title = "Crashlytics",
                description = "Crash reporting, custom logs",
                onClick = { onNavigate(DemoScreen.CRASHLYTICS) }
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "Configure Firebase using:\nkfire init",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DemoButton(
    title: String,
    description: String,
    onClick: () -> Unit
) {
    OutlinedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = ">",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
