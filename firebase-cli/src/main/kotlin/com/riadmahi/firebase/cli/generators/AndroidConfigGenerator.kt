package com.riadmahi.firebase.cli.generators

import java.nio.file.Path
import kotlin.io.path.*

class AndroidConfigGenerator(private val projectRoot: Path) {

    fun writeGoogleServicesJson(content: String) {
        // Find the appropriate location for google-services.json
        val possibleLocations = listOf(
            "demo/androidApp/google-services.json",
            "demo/composeApp/google-services.json",
            "androidApp/google-services.json",
            "composeApp/google-services.json",
            "app/google-services.json"
        )

        val targetPath = possibleLocations
            .map { projectRoot.resolve(it) }
            .firstOrNull { it.parent.exists() }
            ?: projectRoot.resolve("app/google-services.json")

        // Create parent directories if needed
        targetPath.parent.createDirectories()

        // Write the file
        targetPath.writeText(content)
    }
}
