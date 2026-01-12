package com.riadmahi.firebase.cli.generators

import java.nio.file.Path
import kotlin.io.path.*

class IosConfigGenerator(private val projectRoot: Path) {

    fun writeGoogleServiceInfoPlist(content: String) {
        // Find the appropriate location for GoogleService-Info.plist
        val possibleLocations = listOf(
            "demo/iosApp/iosApp/GoogleService-Info.plist",
            "demo/iosApp/GoogleService-Info.plist",
            "iosApp/iosApp/GoogleService-Info.plist",
            "iosApp/GoogleService-Info.plist"
        )

        val targetPath = possibleLocations
            .map { projectRoot.resolve(it) }
            .firstOrNull { it.parent.exists() }
            ?: projectRoot.resolve("iosApp/iosApp/GoogleService-Info.plist")

        // Create parent directories if needed
        targetPath.parent.createDirectories()

        // Write the file
        targetPath.writeText(content)
    }
}
