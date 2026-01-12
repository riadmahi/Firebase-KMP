package com.riadmahi.firebase.demo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.riadmahi.firebase.auth.FirebaseAuth
import com.riadmahi.firebase.auth.FirebaseUser
import com.riadmahi.firebase.core.FirebaseResult
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthDemoScreen(onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    val auth = remember { FirebaseAuth.getInstance() }

    var currentUser by remember { mutableStateOf<FirebaseUser?>(null) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }

    // Observe auth state
    LaunchedEffect(Unit) {
        auth.authStateFlow.collect { user ->
            currentUser = user
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Authentication Demo") },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("< Back")
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Current user status
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (currentUser != null)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (currentUser != null) "Signed In" else "Not Signed In",
                        style = MaterialTheme.typography.titleMedium
                    )
                    currentUser?.let { user ->
                        Text("UID: ${user.uid}")
                        user.email?.let { Text("Email: $it") }
                        user.displayName?.let { Text("Name: $it") }
                        Text("Anonymous: ${user.isAnonymous}")
                        Text("Email Verified: ${user.isEmailVerified}")
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Email/Password form
            if (currentUser == null) {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            scope.launch {
                                isLoading = true
                                message = null
                                when (val result = auth.signInWithEmailAndPassword(email, password)) {
                                    is FirebaseResult.Success -> {
                                        message = "Signed in successfully!"
                                    }
                                    is FirebaseResult.Failure -> {
                                        message = "Error: ${result.exception.message}"
                                    }
                                }
                                isLoading = false
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading && email.isNotBlank() && password.isNotBlank()
                    ) {
                        Text("Sign In")
                    }

                    Button(
                        onClick = {
                            scope.launch {
                                isLoading = true
                                message = null
                                when (val result = auth.createUserWithEmailAndPassword(email, password)) {
                                    is FirebaseResult.Success -> {
                                        message = "Account created successfully!"
                                    }
                                    is FirebaseResult.Failure -> {
                                        message = "Error: ${result.exception.message}"
                                    }
                                }
                                isLoading = false
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading && email.isNotBlank() && password.isNotBlank()
                    ) {
                        Text("Sign Up")
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // Anonymous sign in
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            isLoading = true
                            message = null
                            when (val result = auth.signInAnonymously()) {
                                is FirebaseResult.Success -> {
                                    message = "Signed in anonymously!"
                                }
                                is FirebaseResult.Failure -> {
                                    message = "Error: ${result.exception.message}"
                                }
                            }
                            isLoading = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                ) {
                    Text("Sign In Anonymously")
                }
            } else {
                // Signed in - show actions
                Button(
                    onClick = {
                        scope.launch {
                            isLoading = true
                            message = null
                            when (val result = auth.signOut()) {
                                is FirebaseResult.Success -> {
                                    message = "Signed out!"
                                    email = ""
                                    password = ""
                                }
                                is FirebaseResult.Failure -> {
                                    message = "Error: ${result.exception.message}"
                                }
                            }
                            isLoading = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Sign Out")
                }

                if (!currentUser!!.isEmailVerified && currentUser!!.email != null) {
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                isLoading = true
                                message = null
                                when (val result = currentUser!!.sendEmailVerification()) {
                                    is FirebaseResult.Success -> {
                                        message = "Verification email sent!"
                                    }
                                    is FirebaseResult.Failure -> {
                                        message = "Error: ${result.exception.message}"
                                    }
                                }
                                isLoading = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading
                    ) {
                        Text("Send Verification Email")
                    }
                }

                OutlinedButton(
                    onClick = {
                        scope.launch {
                            isLoading = true
                            message = null
                            when (val result = currentUser!!.reload()) {
                                is FirebaseResult.Success -> {
                                    message = "User reloaded!"
                                }
                                is FirebaseResult.Failure -> {
                                    message = "Error: ${result.exception.message}"
                                }
                            }
                            isLoading = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                ) {
                    Text("Reload User")
                }

                OutlinedButton(
                    onClick = {
                        scope.launch {
                            isLoading = true
                            message = null
                            when (val result = currentUser!!.getIdToken(forceRefresh = false)) {
                                is FirebaseResult.Success -> {
                                    message = "Token: ${result.data.take(50)}..."
                                }
                                is FirebaseResult.Failure -> {
                                    message = "Error: ${result.exception.message}"
                                }
                            }
                            isLoading = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                ) {
                    Text("Get ID Token")
                }
            }

            // Loading indicator
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.padding(8.dp))
            }

            // Message display
            message?.let { msg ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (msg.startsWith("Error"))
                            MaterialTheme.colorScheme.errorContainer
                        else
                            MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Text(
                        text = msg,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
