# firebase-firestore

Firebase Firestore module for Kotlin Multiplatform.

## Installation

```kotlin
// build.gradle.kts
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("com.riadmahi.firebase:firebase-core:1.0.0")
            implementation("com.riadmahi.firebase:firebase-firestore:1.0.0")
        }
    }
}
```

## Getting Started

```kotlin
import com.riadmahi.firebase.firestore.FirebaseFirestore
import com.riadmahi.firebase.core.FirebaseResult

val db = FirebaseFirestore.getInstance()
```

## References

### Collection Reference

```kotlin
val usersCollection = db.collection("users")
val nestedCollection = db.collection("users/user123/posts")
```

### Document Reference

```kotlin
val userDoc = db.collection("users").document("user123")
val autoIdDoc = db.collection("users").document() // Auto-generated ID
```

## CRUD Operations

### Create / Write

```kotlin
val data = mapOf(
    "name" to "John Doe",
    "email" to "john@example.com",
    "age" to 30,
    "active" to true
)

// Add document (auto-generated ID)
when (val result = usersCollection.add(data)) {
    is FirebaseResult.Success -> println("Created: ${result.data.id}")
    is FirebaseResult.Failure -> println("Error: ${result.exception}")
}

// Set document (specific ID)
userDoc.set(data)

// Set with merge (update existing fields, keep others)
userDoc.set(data, SetOptions.Merge)

// Merge specific fields only
userDoc.set(data, SetOptions.MergeFields(listOf("name", "email")))
```

### Read

```kotlin
// Get single document
when (val result = userDoc.get()) {
    is FirebaseResult.Success -> {
        val snapshot = result.data
        if (snapshot.exists) {
            val name = snapshot.getString("name")
            val age = snapshot.getLong("age")
            val data = snapshot.data
            println("User: $name, $age years old")
        }
    }
    is FirebaseResult.Failure -> println("Error")
}

// Get from cache only
userDoc.get(Source.CACHE)

// Get from server only
userDoc.get(Source.SERVER)
```

### Update

```kotlin
// Update specific fields
userDoc.update(mapOf(
    "name" to "Jane Doe",
    "age" to 31
))
```

### Delete

```kotlin
userDoc.delete()
```

## Queries

### Basic Queries

```kotlin
val query = db.collection("users")
    .whereEqualTo("city", "Paris")
    .orderBy("name", Direction.ASCENDING)
    .limit(10)

when (val result = query.get()) {
    is FirebaseResult.Success -> {
        result.data.documents.forEach { doc ->
            println("${doc.id}: ${doc.data}")
        }
    }
    is FirebaseResult.Failure -> println("Error")
}
```

### Query Operators

```kotlin
// Equality
query.whereEqualTo("status", "active")
query.whereNotEqualTo("status", "deleted")

// Comparison
query.whereLessThan("age", 30)
query.whereLessThanOrEqualTo("age", 30)
query.whereGreaterThan("age", 18)
query.whereGreaterThanOrEqualTo("age", 18)

// Array operations
query.whereArrayContains("tags", "premium")
query.whereArrayContainsAny("tags", listOf("premium", "vip"))

// In/Not In
query.whereIn("status", listOf("active", "pending"))
query.whereNotIn("status", listOf("deleted", "banned"))
```

### Ordering and Pagination

```kotlin
// Order by field
query.orderBy("createdAt", Direction.DESCENDING)
query.orderBy("name", Direction.ASCENDING)

// Limit results
query.limit(20)
query.limitToLast(20)

// Cursor-based pagination
query.startAt("John")
query.startAfter("John")
query.endAt("Mike")
query.endBefore("Mike")

// Pagination with document snapshot
val lastDoc = previousResults.documents.last()
query.startAfter(lastDoc)
```

## Real-time Updates

### Document Listener

```kotlin
userDoc.snapshots().collect { snapshot ->
    println("Updated: ${snapshot.data}")
}

// With metadata changes
userDoc.snapshots(includeMetadataChanges = true).collect { snapshot ->
    val source = if (snapshot.metadata.isFromCache) "cache" else "server"
    println("Data from $source: ${snapshot.data}")
}
```

### Query Listener

```kotlin
db.collection("users")
    .whereEqualTo("active", true)
    .snapshots()
    .collect { snapshot ->
        snapshot.documents.forEach { doc ->
            println("${doc.id}: ${doc.data}")
        }
    }
```

## FieldValue Operations

```kotlin
import com.riadmahi.firebase.firestore.FieldValue

userDoc.update(mapOf(
    // Server timestamp
    "updatedAt" to FieldValue.serverTimestamp(),

    // Increment number
    "loginCount" to FieldValue.increment(1),
    "balance" to FieldValue.increment(-10.5),

    // Array operations
    "tags" to FieldValue.arrayUnion("premium", "verified"),
    "oldTags" to FieldValue.arrayRemove("free"),

    // Delete field
    "temporaryField" to FieldValue.delete()
))
```

## Transactions

Atomic read-then-write operations.

```kotlin
when (val result = db.runTransaction { transaction ->
    val snapshot = transaction.get(userDoc)
    val currentBalance = snapshot.getLong("balance") ?: 0L

    if (currentBalance < 100) {
        throw Exception("Insufficient balance")
    }

    transaction.update(userDoc, mapOf(
        "balance" to currentBalance - 100
    ))

    currentBalance - 100 // Return value
}) {
    is FirebaseResult.Success -> println("New balance: ${result.data}")
    is FirebaseResult.Failure -> println("Transaction failed")
}
```

### Transaction Operations

```kotlin
transaction.get(documentRef)                    // Read
transaction.set(documentRef, data)              // Create/Replace
transaction.set(documentRef, data, SetOptions.Merge) // Merge
transaction.update(documentRef, data)           // Update
transaction.delete(documentRef)                 // Delete
```

## Batch Writes

Atomic write operations (no reads).

```kotlin
val batch = db.batch()

// Set
batch.set(
    db.collection("users").document("user1"),
    mapOf("name" to "User 1")
)

// Update
batch.update(
    db.collection("users").document("user2"),
    mapOf("active" to true)
)

// Delete
batch.delete(db.collection("users").document("user3"))

// Commit all operations
when (val result = batch.commit()) {
    is FirebaseResult.Success -> println("Batch committed!")
    is FirebaseResult.Failure -> println("Batch failed")
}
```

## DocumentSnapshot

```kotlin
val snapshot: DocumentSnapshot = ...

// Properties
snapshot.id              // Document ID
snapshot.reference       // DocumentReference
snapshot.exists          // Document exists
snapshot.data            // Map<String, Any?>
snapshot.metadata        // SnapshotMetadata

// Typed getters
snapshot.getString("name")
snapshot.getLong("age")
snapshot.getDouble("price")
snapshot.getBoolean("active")
snapshot.getTimestamp("createdAt")
snapshot.getGeoPoint("location")
snapshot.getDocumentReference("authorRef")

// Generic getter
snapshot.get("field")
```

## QuerySnapshot

```kotlin
val querySnapshot: QuerySnapshot = ...

querySnapshot.documents      // List<DocumentSnapshot>
querySnapshot.isEmpty        // Boolean
querySnapshot.size           // Int
querySnapshot.metadata       // SnapshotMetadata
```

## Settings

```kotlin
// Configure Firestore settings
val settings = FirestoreSettings(
    persistenceEnabled = true,
    cacheSizeBytes = 100 * 1024 * 1024 // 100 MB
)
db.settings = settings

// Read current settings
val currentSettings = db.settings
```

## Emulator Support

```kotlin
// Connect to Firestore Emulator
db.useEmulator("localhost", 8080)
```

## Error Handling

```kotlin
when (val result = userDoc.get()) {
    is FirebaseResult.Success -> { /* success */ }
    is FirebaseResult.Failure -> {
        when (result.exception) {
            is FirestoreException.NotFound -> "Document not found"
            is FirestoreException.PermissionDenied -> "Permission denied"
            is FirestoreException.AlreadyExists -> "Document already exists"
            is FirestoreException.Unavailable -> "Service unavailable"
            is FirestoreException.Cancelled -> "Operation cancelled"
            is FirestoreException.InvalidArgument -> "Invalid argument"
            is FirestoreException.DeadlineExceeded -> "Timeout"
            is FirestoreException.ResourceExhausted -> "Quota exceeded"
            is FirestoreException.FailedPrecondition -> "Precondition failed"
            is FirestoreException.Aborted -> "Operation aborted"
            is FirestoreException.OutOfRange -> "Value out of range"
            is FirestoreException.Unimplemented -> "Not implemented"
            is FirestoreException.Internal -> "Internal error"
            is FirestoreException.DataLoss -> "Data loss"
            is FirestoreException.Unauthenticated -> "Not authenticated"
            else -> "Unknown error"
        }
    }
}
```

## Data Types

Supported Firestore data types:

| Kotlin Type | Firestore Type |
|-------------|----------------|
| `String` | String |
| `Long`, `Int` | Number (integer) |
| `Double`, `Float` | Number (floating) |
| `Boolean` | Boolean |
| `Map<String, Any?>` | Map |
| `List<Any?>` | Array |
| `null` | Null |
| `Timestamp` | Timestamp |
| `GeoPoint` | GeoPoint |
| `DocumentReference` | Reference |
| `ByteArray` | Bytes |
