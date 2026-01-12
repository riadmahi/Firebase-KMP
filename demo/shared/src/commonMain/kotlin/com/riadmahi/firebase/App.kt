package com.riadmahi.firebase

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.riadmahi.firebase.demo.AuthDemoScreen
import com.riadmahi.firebase.demo.FirestoreDemoScreen
import org.jetbrains.compose.ui.tooling.preview.Preview

enum class DemoScreen {
    HOME,
    AUTH,
    FIRESTORE
}

@Composable
@Preview
fun App() {
    MaterialTheme {
        var currentScreen by remember { mutableStateOf(DemoScreen.HOME) }

        when (currentScreen) {
            DemoScreen.HOME -> HomeScreen(
                onAuthClick = { currentScreen = DemoScreen.AUTH },
                onFirestoreClick = { currentScreen = DemoScreen.FIRESTORE }
            )
            DemoScreen.AUTH -> AuthDemoScreen(
                onBack = { currentScreen = DemoScreen.HOME }
            )
            DemoScreen.FIRESTORE -> FirestoreDemoScreen(
                onBack = { currentScreen = DemoScreen.HOME }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onAuthClick: () -> Unit,
    onFirestoreClick: () -> Unit
) {
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Firebase Kotlin Multiplatform",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onAuthClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Authentication Demo")
            }

            Button(
                onClick = onFirestoreClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Firestore Demo")
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "Configure Firebase using:\nkmpfire configure",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
