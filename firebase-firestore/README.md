# Firebase Firestore

Cloud Firestore for KFire - a flexible, scalable NoSQL database.

## Installation

```kotlin
// build.gradle.kts
commonMain.dependencies {
    implementation("com.riadmahi.firebase:firebase-core:1.0.0")
    implementation("com.riadmahi.firebase:firebase-firestore:1.0.0")
}
```

## Usage

### Get Firestore Instance

```kotlin
import com.riadmahi.firebase.firestore.FirebaseFirestore

val db = FirebaseFirestore.getInstance()
```

### Add a Document

```kotlin
val city = mapOf(
    "name" to "Los Angeles",
    "state" to "CA",
    "country" to "USA",
    "capital" to false,
    "population" to 3900000
)

// Auto-generated ID
val result = db.collection("cities").add(city)
when (result) {
    is FirebaseResult.Success -> println("Added with ID: ${result.data.id}")
    is FirebaseResult.Failure -> println("Error: ${result.exception}")
}

// Specific ID
db.collection("cities").document("LA").set(city)
```

### Get a Document

```kotlin
val result = db.collection("cities").document("LA").get()
when (result) {
    is FirebaseResult.Success -> {
        val snapshot = result.data
        if (snapshot.exists) {
            val name: String? = snapshot.get("name")
            val population: Long? = snapshot.get("population")
            println("$name: $population")
        }
    }
    is FirebaseResult.Failure -> println("Error: ${result.exception}")
}
```

### Query Documents

```kotlin
// Get all cities in California
val query = db.collection("cities")
    .whereEqualTo("state", "CA")
    .orderBy("population", Direction.DESCENDING)
    .limit(10)

when (val result = query.get()) {
    is FirebaseResult.Success -> {
        result.data.documents.forEach { doc ->
            println("${doc.id}: ${doc.get<String>("name")}")
        }
    }
    is FirebaseResult.Failure -> println("Error: ${result.exception}")
}
```

### Query Operators

```kotlin
db.collection("cities")
    .whereEqualTo("capital", true)
    .whereGreaterThan("population", 1000000)
    .whereIn("country", listOf("USA", "Japan"))
    .whereArrayContains("regions", "west_coast")
```

### Real-time Updates

```kotlin
// Listen to a document
db.collection("cities").document("LA")
    .snapshots()
    .collect { snapshot ->
        println("Current data: ${snapshot.data()}")
    }

// Listen to a query
db.collection("cities")
    .whereEqualTo("state", "CA")
    .snapshots()
    .collect { snapshot ->
        snapshot.documentChanges.forEach { change ->
            when (change.type) {
                DocumentChangeType.ADDED -> println("New city: ${change.document.id}")
                DocumentChangeType.MODIFIED -> println("Modified: ${change.document.id}")
                DocumentChangeType.REMOVED -> println("Removed: ${change.document.id}")
            }
        }
    }
```

### Update a Document

```kotlin
db.collection("cities").document("LA").update(
    mapOf(
        "population" to 4000000,
        "lastUpdated" to FieldValue.serverTimestamp()
    )
)

// Field operations
db.collection("cities").document("LA").update(
    mapOf(
        "population" to FieldValue.increment(50000),
        "regions" to FieldValue.arrayUnion("downtown")
    )
)
```

### Delete a Document

```kotlin
db.collection("cities").document("LA").delete()
```

### Transactions

```kotlin
db.runTransaction { transaction ->
    val cityRef = db.collection("cities").document("LA")
    val snapshot = transaction.get(cityRef)
    val population = snapshot.get<Long>("population") ?: 0

    transaction.update(cityRef, mapOf("population" to population + 1))
    population + 1
}
```

### Batch Writes

```kotlin
val batch = db.batch()

batch.set(db.collection("cities").document("NYC"), mapOf("name" to "New York"))
batch.update(db.collection("cities").document("LA"), mapOf("capital" to false))
batch.delete(db.collection("cities").document("OLD"))

batch.commit()
```

### Emulator

```kotlin
db.useEmulator("10.0.2.2", 8080)  // Android
db.useEmulator("localhost", 8080) // iOS
```

## API Reference

| Method | Description |
|--------|-------------|
| `collection(path)` | Get a collection reference |
| `document(path)` | Get a document reference |
| `collectionGroup(id)` | Query across subcollections |
| `runTransaction()` | Execute atomic operations |
| `batch()` | Create a batch writer |

## See Also

- [Firestore Documentation](https://firebase.google.com/docs/firestore)
