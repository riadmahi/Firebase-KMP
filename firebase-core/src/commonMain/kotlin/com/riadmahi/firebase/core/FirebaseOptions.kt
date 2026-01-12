package com.riadmahi.firebase.core

import kotlinx.serialization.Serializable

/**
 * Configuration options for initializing a Firebase application.
 */
@Serializable
data class FirebaseOptions(
    val apiKey: String,
    val applicationId: String,
    val projectId: String,
    val storageBucket: String? = null,
    val gcmSenderId: String? = null,
    val databaseUrl: String? = null
) {
    class Builder {
        var apiKey: String = ""
        var applicationId: String = ""
        var projectId: String = ""
        var storageBucket: String? = null
        var gcmSenderId: String? = null
        var databaseUrl: String? = null

        fun setApiKey(apiKey: String) = apply { this.apiKey = apiKey }
        fun setApplicationId(applicationId: String) = apply { this.applicationId = applicationId }
        fun setProjectId(projectId: String) = apply { this.projectId = projectId }
        fun setStorageBucket(storageBucket: String?) = apply { this.storageBucket = storageBucket }
        fun setGcmSenderId(gcmSenderId: String?) = apply { this.gcmSenderId = gcmSenderId }
        fun setDatabaseUrl(databaseUrl: String?) = apply { this.databaseUrl = databaseUrl }

        fun build(): FirebaseOptions {
            require(apiKey.isNotBlank()) { "API key must not be blank" }
            require(applicationId.isNotBlank()) { "Application ID must not be blank" }
            require(projectId.isNotBlank()) { "Project ID must not be blank" }
            return FirebaseOptions(
                apiKey = apiKey,
                applicationId = applicationId,
                projectId = projectId,
                storageBucket = storageBucket,
                gcmSenderId = gcmSenderId,
                databaseUrl = databaseUrl
            )
        }
    }
}

fun firebaseOptions(block: FirebaseOptions.Builder.() -> Unit): FirebaseOptions {
    return FirebaseOptions.Builder().apply(block).build()
}
