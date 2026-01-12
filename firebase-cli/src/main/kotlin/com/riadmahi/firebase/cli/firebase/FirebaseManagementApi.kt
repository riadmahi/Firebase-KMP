package com.riadmahi.firebase.cli.firebase

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.util.Base64

class FirebaseManagementApi(private val accessToken: String) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(json)
        }
    }

    private val baseUrl = "https://firebase.googleapis.com/v1beta1"

    suspend fun listProjects(): List<FirebaseProject> {
        val response = client.get("$baseUrl/projects") {
            bearerAuth(accessToken)
        }
        return response.body<ProjectsResponse>().results
    }

    // ═══════════════════════════════════════════════════════════════════
    // ANDROID APPS
    // ═══════════════════════════════════════════════════════════════════

    suspend fun getOrCreateAndroidApp(projectId: String, packageName: String): AndroidApp {
        val apps = listAndroidApps(projectId)
        return apps.find { it.packageName == packageName }
            ?: createAndroidApp(projectId, packageName)
    }

    suspend fun listAndroidApps(projectId: String): List<AndroidApp> {
        val response = client.get("$baseUrl/projects/$projectId/androidApps") {
            bearerAuth(accessToken)
        }
        return response.body<AndroidAppsResponse>().apps ?: emptyList()
    }

    suspend fun createAndroidApp(projectId: String, packageName: String): AndroidApp {
        val response = client.post("$baseUrl/projects/$projectId/androidApps") {
            bearerAuth(accessToken)
            contentType(ContentType.Application.Json)
            setBody(CreateAndroidAppRequest(packageName))
        }

        val responseText = response.bodyAsText()

        // Try to parse response
        val jsonElement = json.parseToJsonElement(responseText)
        val jsonObj = jsonElement.jsonObject

        // Check if this is an error response
        if (jsonObj.containsKey("error")) {
            val errorMsg = jsonObj["error"]?.jsonObject?.get("message")?.jsonPrimitive?.content
                ?: "Unknown error"
            throw Exception(errorMsg)
        }

        // Check if the response contains appId directly (immediate result)
        if (jsonObj.containsKey("appId")) {
            return AndroidApp(
                name = jsonObj["name"]?.jsonPrimitive?.content ?: "",
                appId = jsonObj["appId"]?.jsonPrimitive?.content ?: throw Exception("Missing appId"),
                packageName = jsonObj["packageName"]?.jsonPrimitive?.content ?: throw Exception("Missing packageName"),
                displayName = jsonObj["displayName"]?.jsonPrimitive?.content
            )
        }

        // It's an Operation - wait for it
        val operation = json.decodeFromJsonElement<Operation>(jsonElement)
        return waitForAndroidAppOperation(operation)
    }

    private suspend fun waitForAndroidAppOperation(operation: Operation): AndroidApp {
        // Check if already done
        if (operation.done == true) {
            val response = operation.response ?: throw Exception("Operation completed without response")
            return AndroidApp(
                name = response["name"]?.jsonPrimitive?.content ?: "",
                appId = response["appId"]?.jsonPrimitive?.content ?: throw Exception("Missing appId"),
                packageName = response["packageName"]?.jsonPrimitive?.content ?: throw Exception("Missing packageName"),
                displayName = response["displayName"]?.jsonPrimitive?.content
            )
        }

        // Check for operation error
        if (operation.error != null) {
            throw Exception(operation.error.message ?: "Operation failed")
        }

        val opName = operation.name ?: throw Exception("Operation has no name to poll")

        var currentOp = operation
        repeat(30) { // Max 30 attempts (30 seconds)
            delay(1000) // Wait 1 second

            // Poll operation status - use v1 API for operations
            val pollResponse = client.get("https://firebase.googleapis.com/v1/$opName") {
                bearerAuth(accessToken)
            }
            currentOp = pollResponse.body()

            if (currentOp.done == true) {
                if (currentOp.error != null) {
                    throw Exception(currentOp.error.message ?: "Operation failed")
                }
                val response = currentOp.response ?: throw Exception("Operation completed without response")
                return AndroidApp(
                    name = response["name"]?.jsonPrimitive?.content ?: "",
                    appId = response["appId"]?.jsonPrimitive?.content ?: throw Exception("Missing appId"),
                    packageName = response["packageName"]?.jsonPrimitive?.content ?: throw Exception("Missing packageName"),
                    displayName = response["displayName"]?.jsonPrimitive?.content
                )
            }
        }
        throw Exception("Operation timed out")
    }

    suspend fun getAndroidAppConfig(projectId: String, appId: String): String {
        val response = client.get("$baseUrl/projects/$projectId/androidApps/$appId/config") {
            bearerAuth(accessToken)
        }
        val configResponse = response.body<AppConfigResponse>()
        return String(Base64.getDecoder().decode(configResponse.configFileContents))
    }

    // ═══════════════════════════════════════════════════════════════════
    // IOS APPS
    // ═══════════════════════════════════════════════════════════════════

    suspend fun getOrCreateIosApp(projectId: String, bundleId: String): IosApp {
        val apps = listIosApps(projectId)
        return apps.find { it.bundleId == bundleId }
            ?: createIosApp(projectId, bundleId)
    }

    suspend fun listIosApps(projectId: String): List<IosApp> {
        val response = client.get("$baseUrl/projects/$projectId/iosApps") {
            bearerAuth(accessToken)
        }
        return response.body<IosAppsResponse>().apps ?: emptyList()
    }

    suspend fun createIosApp(projectId: String, bundleId: String): IosApp {
        val response = client.post("$baseUrl/projects/$projectId/iosApps") {
            bearerAuth(accessToken)
            contentType(ContentType.Application.Json)
            setBody(CreateIosAppRequest(bundleId))
        }

        val responseText = response.bodyAsText()

        // Try to parse as Operation first
        val jsonElement = json.parseToJsonElement(responseText)
        val jsonObj = jsonElement.jsonObject

        // Check if this is an error response
        if (jsonObj.containsKey("error")) {
            val errorMsg = jsonObj["error"]?.jsonObject?.get("message")?.jsonPrimitive?.content
                ?: "Unknown error"
            throw Exception(errorMsg)
        }

        // Check if the response contains appId directly (immediate result)
        if (jsonObj.containsKey("appId")) {
            return IosApp(
                name = jsonObj["name"]?.jsonPrimitive?.content ?: "",
                appId = jsonObj["appId"]?.jsonPrimitive?.content ?: throw Exception("Missing appId"),
                bundleId = jsonObj["bundleId"]?.jsonPrimitive?.content ?: throw Exception("Missing bundleId"),
                displayName = jsonObj["displayName"]?.jsonPrimitive?.content
            )
        }

        // It's an Operation - wait for it
        val operation = json.decodeFromJsonElement<Operation>(jsonElement)
        return waitForIosAppOperation(operation)
    }

    private suspend fun waitForIosAppOperation(operation: Operation): IosApp {
        // Check if already done
        if (operation.done == true) {
            val response = operation.response ?: throw Exception("Operation completed without response")
            return IosApp(
                name = response["name"]?.jsonPrimitive?.content ?: "",
                appId = response["appId"]?.jsonPrimitive?.content ?: throw Exception("Missing appId"),
                bundleId = response["bundleId"]?.jsonPrimitive?.content ?: throw Exception("Missing bundleId"),
                displayName = response["displayName"]?.jsonPrimitive?.content
            )
        }

        // Check for operation error
        if (operation.error != null) {
            throw Exception(operation.error.message ?: "Operation failed")
        }

        val opName = operation.name ?: throw Exception("Operation has no name to poll")

        var currentOp = operation
        repeat(30) { // Max 30 attempts (30 seconds)
            delay(1000) // Wait 1 second

            // Poll operation status - use v1 API for operations
            val pollResponse = client.get("https://firebase.googleapis.com/v1/$opName") {
                bearerAuth(accessToken)
            }
            currentOp = pollResponse.body()

            if (currentOp.done == true) {
                if (currentOp.error != null) {
                    throw Exception(currentOp.error.message ?: "Operation failed")
                }
                val response = currentOp.response ?: throw Exception("Operation completed without response")
                return IosApp(
                    name = response["name"]?.jsonPrimitive?.content ?: "",
                    appId = response["appId"]?.jsonPrimitive?.content ?: throw Exception("Missing appId"),
                    bundleId = response["bundleId"]?.jsonPrimitive?.content ?: throw Exception("Missing bundleId"),
                    displayName = response["displayName"]?.jsonPrimitive?.content
                )
            }
        }
        throw Exception("Operation timed out")
    }

    suspend fun getIosAppConfig(projectId: String, appId: String): String {
        val response = client.get("$baseUrl/projects/$projectId/iosApps/$appId/config") {
            bearerAuth(accessToken)
        }
        val configResponse = response.body<AppConfigResponse>()
        return String(Base64.getDecoder().decode(configResponse.configFileContents))
    }
}

// ═══════════════════════════════════════════════════════════════════
// DATA CLASSES
// ═══════════════════════════════════════════════════════════════════

@Serializable
data class Operation(
    val name: String? = null,
    val done: Boolean? = null,
    val response: JsonObject? = null,
    val error: OperationError? = null
)

@Serializable
data class OperationError(
    val code: Int? = null,
    val message: String? = null
)

@Serializable
data class FirebaseProject(
    val projectId: String,
    val projectNumber: String = "",
    val displayName: String,
    val resources: ProjectResources? = null
)

@Serializable
data class ProjectResources(
    val hostingSite: String? = null,
    val storageBucket: String? = null,
    val locationId: String? = null
)

@Serializable
data class ProjectsResponse(
    val results: List<FirebaseProject> = emptyList()
)

@Serializable
data class AndroidApp(
    val name: String = "",
    val appId: String,
    val packageName: String,
    val displayName: String? = null
)

@Serializable
data class AndroidAppsResponse(
    val apps: List<AndroidApp>? = null
)

@Serializable
data class CreateAndroidAppRequest(
    val packageName: String
)

@Serializable
data class IosApp(
    val name: String = "",
    val appId: String,
    val bundleId: String,
    val displayName: String? = null
)

@Serializable
data class IosAppsResponse(
    val apps: List<IosApp>? = null
)

@Serializable
data class CreateIosAppRequest(
    val bundleId: String
)

@Serializable
data class AppConfigResponse(
    val configFilename: String = "",
    val configFileContents: String
)
