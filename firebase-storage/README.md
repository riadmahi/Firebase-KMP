# Firebase Storage

Cloud Storage for KFire - store and serve user-generated content.

## Installation

```kotlin
// build.gradle.kts
commonMain.dependencies {
    implementation("com.riadmahi.firebase:firebase-core:1.0.0")
    implementation("com.riadmahi.firebase:firebase-storage:1.0.0")
}
```

## Usage

### Get Storage Instance

```kotlin
import com.riadmahi.firebase.storage.FirebaseStorage

val storage = FirebaseStorage.getInstance()
```

### Create a Reference

```kotlin
// Root reference
val storageRef = storage.reference

// Child reference
val imagesRef = storageRef.child("images")
val mountainsRef = imagesRef.child("mountains.jpg")

// From URL
val gsRef = storage.getReferenceFromUrl("gs://bucket/images/mountains.jpg")
```

### Upload Files

```kotlin
val mountainsRef = storage.reference.child("images/mountains.jpg")

// Upload from bytes
val imageBytes: ByteArray = // ... your image data
val result = mountainsRef.putBytes(imageBytes)

when (result) {
    is FirebaseResult.Success -> {
        println("Upload complete: ${result.data.bytesTransferred} bytes")
    }
    is FirebaseResult.Failure -> println("Upload failed: ${result.exception}")
}

// Upload with metadata
val metadata = StorageMetadata(
    contentType = "image/jpeg",
    customMetadata = mapOf("author" to "user123")
)
mountainsRef.putBytes(imageBytes, metadata)
```

### Download Files

```kotlin
val mountainsRef = storage.reference.child("images/mountains.jpg")

// Download as bytes (max 10MB)
val result = mountainsRef.getBytes(10 * 1024 * 1024)
when (result) {
    is FirebaseResult.Success -> {
        val bytes = result.data
        // Use the bytes
    }
    is FirebaseResult.Failure -> println("Download failed: ${result.exception}")
}

// Get download URL
val urlResult = mountainsRef.getDownloadUrl()
when (urlResult) {
    is FirebaseResult.Success -> println("URL: ${urlResult.data}")
    is FirebaseResult.Failure -> println("Error: ${urlResult.exception}")
}
```

### File Metadata

```kotlin
val mountainsRef = storage.reference.child("images/mountains.jpg")

// Get metadata
val result = mountainsRef.getMetadata()
when (result) {
    is FirebaseResult.Success -> {
        val metadata = result.data
        println("Name: ${metadata.name}")
        println("Size: ${metadata.size}")
        println("Type: ${metadata.contentType}")
    }
    is FirebaseResult.Failure -> println("Error: ${result.exception}")
}

// Update metadata
val newMetadata = StorageMetadata(
    contentType = "image/jpeg",
    customMetadata = mapOf("updated" to "true")
)
mountainsRef.updateMetadata(newMetadata)
```

### List Files

```kotlin
val imagesRef = storage.reference.child("images")

// List first 100 items
val result = imagesRef.list(100)
when (result) {
    is FirebaseResult.Success -> {
        val listResult = result.data
        listResult.items.forEach { item ->
            println("File: ${item.name}")
        }
        listResult.prefixes.forEach { prefix ->
            println("Folder: ${prefix.name}")
        }
    }
    is FirebaseResult.Failure -> println("Error: ${result.exception}")
}

// List all
val allResult = imagesRef.listAll()
```

### Delete Files

```kotlin
val mountainsRef = storage.reference.child("images/mountains.jpg")

val result = mountainsRef.delete()
when (result) {
    is FirebaseResult.Success -> println("File deleted")
    is FirebaseResult.Failure -> println("Error: ${result.exception}")
}
```

### Reference Properties

```kotlin
val mountainsRef = storage.reference.child("images/mountains.jpg")

mountainsRef.name    // "mountains.jpg"
mountainsRef.path    // "images/mountains.jpg"
mountainsRef.bucket  // "my-bucket.appspot.com"
mountainsRef.parent  // Reference to "images/"
mountainsRef.root    // Reference to root
```

## API Reference

| Method | Description |
|--------|-------------|
| `reference` | Get root reference |
| `getReference(path)` | Get reference by path |
| `getReferenceFromUrl(url)` | Get reference from URL |
| `child(path)` | Get child reference |
| `putBytes(bytes)` | Upload data |
| `getBytes(maxSize)` | Download data |
| `getDownloadUrl()` | Get download URL |
| `getMetadata()` | Get file metadata |
| `updateMetadata()` | Update metadata |
| `delete()` | Delete file |
| `list(maxResults)` | List files |
| `listAll()` | List all files |

## See Also

- [Cloud Storage Documentation](https://firebase.google.com/docs/storage)
