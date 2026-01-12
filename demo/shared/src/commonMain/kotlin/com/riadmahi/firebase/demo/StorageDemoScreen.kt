package com.riadmahi.firebase.demo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.riadmahi.firebase.core.FirebaseResult
import com.riadmahi.firebase.storage.FirebaseStorage
import com.riadmahi.firebase.storage.StorageReference
import com.riadmahi.firebase.storage.storageMetadata
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

data class StorageFileItem(
    val name: String,
    val path: String,
    val isFolder: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StorageDemoScreen(onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    val storage = remember { FirebaseStorage.getInstance() }

    var currentPath by remember { mutableStateOf("demo") }
    var files by remember { mutableStateOf<List<StorageFileItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var uploadText by remember { mutableStateOf("Hello from KMP Firebase!") }

    // Load files on path change
    LaunchedEffect(currentPath) {
        isLoading = true
        loadFiles(storage, currentPath) { result ->
            when (result) {
                is FirebaseResult.Success -> files = result.data
                is FirebaseResult.Failure -> message = "Error: ${result.exception.message}"
            }
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Storage Demo") },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("< Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            scope.launch {
                                isLoading = true
                                loadFiles(storage, currentPath) { result ->
                                    when (result) {
                                        is FirebaseResult.Success -> {
                                            files = result.data
                                            message = "Refreshed!"
                                        }
                                        is FirebaseResult.Failure -> {
                                            message = "Error: ${result.exception.message}"
                                        }
                                    }
                                    isLoading = false
                                }
                            }
                        }
                    ) {
                        Text("Refresh")
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
        ) {
            // Current path display
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text(
                    text = "Path: /$currentPath",
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Upload section
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Upload Text File",
                        style = MaterialTheme.typography.titleSmall
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = uploadText,
                        onValueChange = { uploadText = it },
                        label = { Text("Content") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 4
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            scope.launch {
                                isLoading = true
                                message = null

                                val fileName = "file_${Clock.System.now().toEpochMilliseconds()}.txt"
                                val ref = storage.getReference("$currentPath/$fileName")
                                val bytes = uploadText.encodeToByteArray()

                                val metadata = storageMetadata {
                                    setContentType("text/plain")
                                    setCustomMetadata("created-by", "kmp-demo")
                                }

                                when (val result = ref.putBytes(bytes, metadata)) {
                                    is FirebaseResult.Success -> {
                                        message = "Uploaded: $fileName (${result.data.bytesTransferred} bytes)"
                                        loadFiles(storage, currentPath) { loadResult ->
                                            if (loadResult is FirebaseResult.Success) {
                                                files = loadResult.data
                                            }
                                        }
                                    }
                                    is FirebaseResult.Failure -> {
                                        message = "Upload error: ${result.exception.message}"
                                    }
                                }
                                isLoading = false
                            }
                        },
                        enabled = uploadText.isNotBlank() && !isLoading,
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Upload")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Message display
            message?.let { msg ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (msg.contains("error", ignoreCase = true))
                            MaterialTheme.colorScheme.errorContainer
                        else
                            MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Text(
                        text = msg,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Loading indicator
            if (isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Files list
            Text(
                text = "Files",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (files.isEmpty() && !isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No files yet. Upload one above!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(files, key = { it.path }) { file ->
                        FileItemCard(
                            file = file,
                            storage = storage,
                            onDownload = { content ->
                                message = "Content: $content"
                            },
                            onGetUrl = { url ->
                                message = "URL: $url"
                            },
                            onDelete = {
                                scope.launch {
                                    val ref = storage.getReference(file.path)
                                    when (val result = ref.delete()) {
                                        is FirebaseResult.Success -> {
                                            message = "Deleted: ${file.name}"
                                            loadFiles(storage, currentPath) { loadResult ->
                                                if (loadResult is FirebaseResult.Success) {
                                                    files = loadResult.data
                                                }
                                            }
                                        }
                                        is FirebaseResult.Failure -> {
                                            message = "Delete error: ${result.exception.message}"
                                        }
                                    }
                                }
                            },
                            onError = { error ->
                                message = "Error: $error"
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FileItemCard(
    file: StorageFileItem,
    storage: FirebaseStorage,
    onDownload: (String) -> Unit,
    onGetUrl: (String) -> Unit,
    onDelete: () -> Unit,
    onError: (String) -> Unit
) {
    val scope = rememberCoroutineScope()

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = file.name,
                style = MaterialTheme.typography.bodyLarge
            )

            Text(
                text = file.path,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(
                    onClick = {
                        scope.launch {
                            val ref = storage.getReference(file.path)
                            when (val result = ref.getBytes(1024 * 1024)) {
                                is FirebaseResult.Success -> {
                                    val content = result.data.decodeToString()
                                    onDownload(content)
                                }
                                is FirebaseResult.Failure -> {
                                    onError(result.exception.message ?: "Download failed")
                                }
                            }
                        }
                    }
                ) {
                    Text("Download")
                }

                TextButton(
                    onClick = {
                        scope.launch {
                            val ref = storage.getReference(file.path)
                            when (val result = ref.getDownloadUrl()) {
                                is FirebaseResult.Success -> onGetUrl(result.data)
                                is FirebaseResult.Failure -> {
                                    onError(result.exception.message ?: "Failed to get URL")
                                }
                            }
                        }
                    }
                ) {
                    Text("Get URL")
                }

                TextButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            }
        }
    }
}

private suspend fun loadFiles(
    storage: FirebaseStorage,
    path: String,
    onResult: (FirebaseResult<List<StorageFileItem>>) -> Unit
) {
    val ref = storage.getReference(path)
    when (val result = ref.listAll()) {
        is FirebaseResult.Success -> {
            val items = result.data.items.map { itemRef ->
                StorageFileItem(
                    name = itemRef.name,
                    path = itemRef.path,
                    isFolder = false
                )
            }
            val prefixes = result.data.prefixes.map { prefixRef ->
                StorageFileItem(
                    name = prefixRef.name + "/",
                    path = prefixRef.path,
                    isFolder = true
                )
            }
            onResult(FirebaseResult.Success(prefixes + items))
        }
        is FirebaseResult.Failure -> {
            onResult(FirebaseResult.Failure(result.exception))
        }
    }
}
