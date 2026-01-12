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
import com.riadmahi.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

data class TodoItem(
    val id: String,
    val title: String,
    val completed: Boolean,
    val createdAt: Long
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FirestoreDemoScreen(onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    val firestore = remember { FirebaseFirestore.getInstance() }
    val todosCollection = remember { firestore.collection("todos") }

    var todos by remember { mutableStateOf<List<TodoItem>>(emptyList()) }
    var newTodoTitle by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }

    // Load todos on start
    LaunchedEffect(Unit) {
        loadTodos(firestore) { result ->
            when (result) {
                is FirebaseResult.Success -> todos = result.data
                is FirebaseResult.Failure -> message = "Error loading: ${result.exception.message}"
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Firestore Demo") },
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
                                loadTodos(firestore) { result ->
                                    when (result) {
                                        is FirebaseResult.Success -> {
                                            todos = result.data
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
            // Add new todo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newTodoTitle,
                    onValueChange = { newTodoTitle = it },
                    label = { Text("New Todo") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )

                Button(
                    onClick = {
                        if (newTodoTitle.isNotBlank()) {
                            scope.launch {
                                isLoading = true
                                message = null
                                val data = mapOf(
                                    "title" to newTodoTitle,
                                    "completed" to false,
                                    "createdAt" to TimeUtils.currentTimeMillis()
                                )
                                when (val result = todosCollection.add(data)) {
                                    is FirebaseResult.Success -> {
                                        message = "Todo added!"
                                        newTodoTitle = ""
                                        // Reload todos
                                        loadTodos(firestore) { loadResult ->
                                            if (loadResult is FirebaseResult.Success) {
                                                todos = loadResult.data
                                            }
                                        }
                                    }
                                    is FirebaseResult.Failure -> {
                                        message = "Error: ${result.exception.message}"
                                    }
                                }
                                isLoading = false
                            }
                        }
                    },
                    enabled = newTodoTitle.isNotBlank() && !isLoading
                ) {
                    Text("+")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

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

            // Todos list
            if (todos.isEmpty() && !isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No todos yet. Add one above!",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(todos, key = { it.id }) { todo ->
                        TodoItemCard(
                            todo = todo,
                            onToggle = {
                                scope.launch {
                                    val docRef = firestore.document("todos/${todo.id}")
                                    when (val result = docRef.update(mapOf("completed" to !todo.completed))) {
                                        is FirebaseResult.Success -> {
                                            loadTodos(firestore) { loadResult ->
                                                if (loadResult is FirebaseResult.Success) {
                                                    todos = loadResult.data
                                                }
                                            }
                                        }
                                        is FirebaseResult.Failure -> {
                                            message = "Error: ${result.exception.message}"
                                        }
                                    }
                                }
                            },
                            onDelete = {
                                scope.launch {
                                    val docRef = firestore.document("todos/${todo.id}")
                                    when (val result = docRef.delete()) {
                                        is FirebaseResult.Success -> {
                                            message = "Todo deleted!"
                                            loadTodos(firestore) { loadResult ->
                                                if (loadResult is FirebaseResult.Success) {
                                                    todos = loadResult.data
                                                }
                                            }
                                        }
                                        is FirebaseResult.Failure -> {
                                            message = "Error: ${result.exception.message}"
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TodoItemCard(
    todo: TodoItem,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = todo.completed,
                onCheckedChange = { onToggle() }
            )

            Text(
                text = todo.title,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
                color = if (todo.completed)
                    MaterialTheme.colorScheme.onSurfaceVariant
                else
                    MaterialTheme.colorScheme.onSurface
            )

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

private suspend fun loadTodos(
    firestore: FirebaseFirestore,
    onResult: (FirebaseResult<List<TodoItem>>) -> Unit
) {
    when (val result = firestore.collection("todos").get()) {
        is FirebaseResult.Success -> {
            val todoList = result.data.documents.mapNotNull { doc ->
                try {
                    val data = doc.data() ?: return@mapNotNull null
                    TodoItem(
                        id = doc.id,
                        title = data["title"] as? String ?: "",
                        completed = data["completed"] as? Boolean ?: false,
                        createdAt = (data["createdAt"] as? Number)?.toLong() ?: 0L
                    )
                } catch (e: Exception) {
                    null
                }
            }.sortedByDescending { it.createdAt }
            onResult(FirebaseResult.Success(todoList))
        }
        is FirebaseResult.Failure -> {
            onResult(FirebaseResult.Failure(result.exception))
        }
    }
}
