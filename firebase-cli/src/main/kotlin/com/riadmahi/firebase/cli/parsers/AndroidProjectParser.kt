package com.riadmahi.firebase.cli.parsers

import java.nio.file.Path
import kotlin.io.path.*

class AndroidProjectParser(private val projectRoot: Path) {

    fun detectPackageName(): String? {
        // Try multiple locations for Android package name (build.gradle.kts files)
        val gradlePaths = listOf(
            "demo/androidApp/build.gradle.kts",
            "demo/composeApp/build.gradle.kts",
            "androidApp/build.gradle.kts",
            "composeApp/build.gradle.kts",
            "app/build.gradle.kts"
        )

        for (gradlePath in gradlePaths) {
            val gradleFile = projectRoot.resolve(gradlePath)
            if (gradleFile.exists()) {
                extractApplicationIdFromGradle(gradleFile)?.let { return it }
                extractNamespaceFromGradle(gradleFile)?.let { return it }
            }
        }

        // Check AndroidManifest.xml as fallback
        val manifestPaths = listOf(
            "demo/androidApp/src/main/AndroidManifest.xml",
            "demo/composeApp/src/androidMain/AndroidManifest.xml",
            "androidApp/src/main/AndroidManifest.xml",
            "composeApp/src/androidMain/AndroidManifest.xml",
            "app/src/main/AndroidManifest.xml"
        )

        for (manifestPath in manifestPaths) {
            val manifest = projectRoot.resolve(manifestPath)
            if (manifest.exists()) {
                extractPackageFromManifest(manifest)?.let { return it }
            }
        }

        return null
    }

    private fun extractNamespaceFromGradle(gradleFile: Path): String? {
        val content = gradleFile.readText()
        val namespaceRegex = Regex("""namespace\s*=\s*["']([^"']+)["']""")
        return namespaceRegex.find(content)?.groupValues?.get(1)
    }

    private fun extractApplicationIdFromGradle(gradleFile: Path): String? {
        val content = gradleFile.readText()
        val appIdRegex = Regex("""applicationId\s*=\s*["']([^"']+)["']""")
        return appIdRegex.find(content)?.groupValues?.get(1)
    }

    private fun extractPackageFromManifest(manifestFile: Path): String? {
        val content = manifestFile.readText()
        val packageRegex = Regex("""package\s*=\s*["']([^"']+)["']""")
        return packageRegex.find(content)?.groupValues?.get(1)
    }
}
