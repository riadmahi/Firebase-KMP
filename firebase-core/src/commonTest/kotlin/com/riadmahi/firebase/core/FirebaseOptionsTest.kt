package com.riadmahi.firebase.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertFailsWith

/**
 * Tests for FirebaseOptions to validate API consistency with Firebase.
 *
 * Reference: https://firebase.google.com/docs/reference/kotlin/com/google/firebase/FirebaseOptions
 *
 * Firebase requires:
 * - apiKey (Web API key from Firebase console)
 * - applicationId (App ID - different for Android/iOS)
 * - projectId (Firebase project ID)
 *
 * Optional fields:
 * - storageBucket (for Cloud Storage)
 * - gcmSenderId (for Cloud Messaging)
 * - databaseUrl (for Realtime Database)
 */
class FirebaseOptionsTest {

    // region Required fields - matching Firebase requirements

    @Test
    fun `FirebaseOptions should require apiKey applicationId and projectId`() {
        val options = FirebaseOptions(
            apiKey = "AIzaSyTest123",
            applicationId = "1:123456789:android:abc123",
            projectId = "my-project"
        )

        assertEquals("AIzaSyTest123", options.apiKey)
        assertEquals("1:123456789:android:abc123", options.applicationId)
        assertEquals("my-project", options.projectId)
    }

    @Test
    fun `FirebaseOptions optional fields should default to null`() {
        val options = FirebaseOptions(
            apiKey = "AIzaSyTest123",
            applicationId = "1:123456789:android:abc123",
            projectId = "my-project"
        )

        assertNull(options.storageBucket)
        assertNull(options.gcmSenderId)
        assertNull(options.databaseUrl)
    }

    // endregion

    // region Optional fields - matching Firebase optional configuration

    @Test
    fun `FirebaseOptions should accept storageBucket for Cloud Storage`() {
        val options = FirebaseOptions(
            apiKey = "AIzaSyTest123",
            applicationId = "1:123456789:android:abc123",
            projectId = "my-project",
            storageBucket = "my-project.appspot.com"
        )

        assertEquals("my-project.appspot.com", options.storageBucket)
    }

    @Test
    fun `FirebaseOptions should accept gcmSenderId for Cloud Messaging`() {
        val options = FirebaseOptions(
            apiKey = "AIzaSyTest123",
            applicationId = "1:123456789:android:abc123",
            projectId = "my-project",
            gcmSenderId = "123456789"
        )

        assertEquals("123456789", options.gcmSenderId)
    }

    @Test
    fun `FirebaseOptions should accept databaseUrl for Realtime Database`() {
        val options = FirebaseOptions(
            apiKey = "AIzaSyTest123",
            applicationId = "1:123456789:android:abc123",
            projectId = "my-project",
            databaseUrl = "https://my-project.firebaseio.com"
        )

        assertEquals("https://my-project.firebaseio.com", options.databaseUrl)
    }

    @Test
    fun `FirebaseOptions should accept all optional fields`() {
        val options = FirebaseOptions(
            apiKey = "AIzaSyTest123",
            applicationId = "1:123456789:android:abc123",
            projectId = "my-project",
            storageBucket = "my-project.appspot.com",
            gcmSenderId = "123456789",
            databaseUrl = "https://my-project.firebaseio.com"
        )

        assertEquals("my-project.appspot.com", options.storageBucket)
        assertEquals("123456789", options.gcmSenderId)
        assertEquals("https://my-project.firebaseio.com", options.databaseUrl)
    }

    // endregion

    // region Builder pattern - matching Firebase's Builder API

    @Test
    fun `Builder should create FirebaseOptions with required fields`() {
        val options = FirebaseOptions.Builder()
            .setApiKey("AIzaSyTest123")
            .setApplicationId("1:123456789:android:abc123")
            .setProjectId("my-project")
            .build()

        assertEquals("AIzaSyTest123", options.apiKey)
        assertEquals("1:123456789:android:abc123", options.applicationId)
        assertEquals("my-project", options.projectId)
    }

    @Test
    fun `Builder should create FirebaseOptions with all fields`() {
        val options = FirebaseOptions.Builder()
            .setApiKey("AIzaSyTest123")
            .setApplicationId("1:123456789:android:abc123")
            .setProjectId("my-project")
            .setStorageBucket("my-project.appspot.com")
            .setGcmSenderId("123456789")
            .setDatabaseUrl("https://my-project.firebaseio.com")
            .build()

        assertEquals("AIzaSyTest123", options.apiKey)
        assertEquals("1:123456789:android:abc123", options.applicationId)
        assertEquals("my-project", options.projectId)
        assertEquals("my-project.appspot.com", options.storageBucket)
        assertEquals("123456789", options.gcmSenderId)
        assertEquals("https://my-project.firebaseio.com", options.databaseUrl)
    }

    @Test
    fun `Builder should throw when apiKey is blank`() {
        assertFailsWith<IllegalArgumentException> {
            FirebaseOptions.Builder()
                .setApplicationId("1:123456789:android:abc123")
                .setProjectId("my-project")
                .build()
        }
    }

    @Test
    fun `Builder should throw when applicationId is blank`() {
        assertFailsWith<IllegalArgumentException> {
            FirebaseOptions.Builder()
                .setApiKey("AIzaSyTest123")
                .setProjectId("my-project")
                .build()
        }
    }

    @Test
    fun `Builder should throw when projectId is blank`() {
        assertFailsWith<IllegalArgumentException> {
            FirebaseOptions.Builder()
                .setApiKey("AIzaSyTest123")
                .setApplicationId("1:123456789:android:abc123")
                .build()
        }
    }

    @Test
    fun `Builder setters should be chainable`() {
        val builder = FirebaseOptions.Builder()

        val result = builder
            .setApiKey("key")
            .setApplicationId("app")
            .setProjectId("project")

        assertEquals(builder, result)
    }

    // endregion

    // region DSL builder - Kotlin-idiomatic API

    @Test
    fun `firebaseOptions DSL should create FirebaseOptions`() {
        val options = firebaseOptions {
            apiKey = "AIzaSyTest123"
            applicationId = "1:123456789:android:abc123"
            projectId = "my-project"
        }

        assertEquals("AIzaSyTest123", options.apiKey)
        assertEquals("1:123456789:android:abc123", options.applicationId)
        assertEquals("my-project", options.projectId)
    }

    @Test
    fun `firebaseOptions DSL should support all fields`() {
        val options = firebaseOptions {
            apiKey = "AIzaSyTest123"
            applicationId = "1:123456789:android:abc123"
            projectId = "my-project"
            storageBucket = "my-project.appspot.com"
            gcmSenderId = "123456789"
            databaseUrl = "https://my-project.firebaseio.com"
        }

        assertEquals("my-project.appspot.com", options.storageBucket)
        assertEquals("123456789", options.gcmSenderId)
        assertEquals("https://my-project.firebaseio.com", options.databaseUrl)
    }

    @Test
    fun `firebaseOptions DSL should throw when required fields missing`() {
        assertFailsWith<IllegalArgumentException> {
            firebaseOptions {
                apiKey = "AIzaSyTest123"
                // missing applicationId and projectId
            }
        }
    }

    // endregion

    // region Data class behavior

    @Test
    fun `FirebaseOptions should support equality`() {
        val options1 = FirebaseOptions(
            apiKey = "key",
            applicationId = "app",
            projectId = "project"
        )
        val options2 = FirebaseOptions(
            apiKey = "key",
            applicationId = "app",
            projectId = "project"
        )

        assertEquals(options1, options2)
    }

    @Test
    fun `FirebaseOptions should support copy`() {
        val original = FirebaseOptions(
            apiKey = "key",
            applicationId = "app",
            projectId = "project"
        )

        val copy = original.copy(storageBucket = "bucket")

        assertEquals("key", copy.apiKey)
        assertEquals("bucket", copy.storageBucket)
    }

    // endregion
}
