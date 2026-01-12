package com.riadmahi.firebase.cli.generators

import java.nio.file.Path
import kotlin.io.path.*

/**
 * Generator for modifying Gradle configuration files to add Firebase support.
 */
class GradleConfigGenerator(private val projectRoot: Path) {

    companion object {
        private const val GOOGLE_SERVICES_VERSION = "4.4.2"
        private const val GOOGLE_SERVICES_PLUGIN_ID = "com.google.gms.google-services"
    }

    /**
     * Configuration options for Firebase modules.
     */
    data class FirebaseModules(
        val core: Boolean = true,
        val auth: Boolean = false,
        val firestore: Boolean = false,
        val storage: Boolean = false
    )

    /**
     * Configures all Gradle files for Firebase.
     * Returns a list of modified files.
     */
    fun configureGradle(modules: FirebaseModules = FirebaseModules()): List<String> {
        val modifiedFiles = mutableListOf<String>()

        // 1. Update libs.versions.toml
        if (updateVersionCatalog()) {
            modifiedFiles.add("gradle/libs.versions.toml")
        }

        // 2. Update root build.gradle.kts
        if (updateRootBuildGradle()) {
            modifiedFiles.add("build.gradle.kts")
        }

        // 3. Update app build.gradle.kts
        val appBuildFile = findAppBuildFile()
        if (appBuildFile != null && updateAppBuildGradle(appBuildFile, modules)) {
            modifiedFiles.add(appBuildFile.relativeTo(projectRoot).toString())
        }

        return modifiedFiles
    }

    /**
     * Updates libs.versions.toml to add google-services plugin.
     */
    private fun updateVersionCatalog(): Boolean {
        val versionCatalogPath = projectRoot.resolve("gradle/libs.versions.toml")
        if (!versionCatalogPath.exists()) return false

        var content = versionCatalogPath.readText()

        // Check if already configured
        if (content.contains("googleServices") || content.contains("google-services")) {
            return false
        }

        // Add version
        val versionsSection = "[versions]"
        if (content.contains(versionsSection)) {
            content = content.replace(
                versionsSection,
                "$versionsSection\ngoogleServices = \"$GOOGLE_SERVICES_VERSION\""
            )
        }

        // Add plugin
        val pluginsSection = "[plugins]"
        if (content.contains(pluginsSection)) {
            // Find the end of the plugins section or end of file
            val pluginEntry = "googleServices = { id = \"$GOOGLE_SERVICES_PLUGIN_ID\", version.ref = \"googleServices\" }"

            // Append at the end of the file (plugins section is usually last)
            if (!content.trimEnd().endsWith(pluginEntry)) {
                content = content.trimEnd() + "\n$pluginEntry\n"
            }
        }

        versionCatalogPath.writeText(content)
        return true
    }

    /**
     * Updates root build.gradle.kts to add google-services plugin.
     */
    private fun updateRootBuildGradle(): Boolean {
        val rootBuildFile = projectRoot.resolve("build.gradle.kts")
        if (!rootBuildFile.exists()) return false

        var content = rootBuildFile.readText()

        // Check if already configured
        if (content.contains("googleServices") && content.contains("apply false")) {
            return false
        }

        // Find the plugins block and add google-services
        val pluginsBlockRegex = Regex("""(plugins\s*\{[^}]*)(})""", RegexOption.DOT_MATCHES_ALL)
        val match = pluginsBlockRegex.find(content)

        if (match != null) {
            val pluginsContent = match.groupValues[1]
            val closingBrace = match.groupValues[2]

            // Check if not already present
            if (!pluginsContent.contains("googleServices")) {
                val newPluginsContent = pluginsContent.trimEnd() +
                    "\n    alias(libs.plugins.googleServices) apply false\n"
                content = content.replace(match.value, newPluginsContent + closingBrace)
                rootBuildFile.writeText(content)
                return true
            }
        }

        return false
    }

    /**
     * Finds the app module's build.gradle.kts file.
     */
    private fun findAppBuildFile(): Path? {
        val possiblePaths = listOf(
            "demo/composeApp/build.gradle.kts",
            "composeApp/build.gradle.kts",
            "app/build.gradle.kts",
            "androidApp/build.gradle.kts"
        )

        return possiblePaths
            .map { projectRoot.resolve(it) }
            .firstOrNull { it.exists() }
    }

    /**
     * Updates the app's build.gradle.kts to apply google-services and add Firebase dependencies.
     */
    private fun updateAppBuildGradle(buildFile: Path, modules: FirebaseModules): Boolean {
        var content = buildFile.readText()
        var modified = false

        // 1. Add google-services plugin if not present
        if (!content.contains("googleServices")) {
            val pluginsBlockRegex = Regex("""(plugins\s*\{[^}]*)(})""", RegexOption.DOT_MATCHES_ALL)
            val match = pluginsBlockRegex.find(content)

            if (match != null) {
                val pluginsContent = match.groupValues[1]
                val closingBrace = match.groupValues[2]
                val newPluginsContent = pluginsContent.trimEnd() +
                    "\n    alias(libs.plugins.googleServices)\n"
                content = content.replace(match.value, newPluginsContent + closingBrace)
                modified = true
            }
        }

        // 2. Add Firebase dependencies to commonMain
        val firebaseDeps = buildFirebaseDependencies(modules)
        if (firebaseDeps.isNotEmpty() && !content.contains("firebase-core")) {
            val commonMainDepsRegex = Regex(
                """(commonMain\.dependencies\s*\{[^}]*)(})""",
                RegexOption.DOT_MATCHES_ALL
            )
            val match = commonMainDepsRegex.find(content)

            if (match != null) {
                val depsContent = match.groupValues[1]
                val closingBrace = match.groupValues[2]
                val newDepsContent = depsContent.trimEnd() + "\n\n" +
                    "            // Firebase KMP\n" +
                    firebaseDeps.joinToString("\n") { "            implementation(project(\":$it\"))" } +
                    "\n        "
                content = content.replace(match.value, newDepsContent + closingBrace)
                modified = true
            }
        }

        // 3. Add coroutines dependency if not present
        if (!content.contains("kotlinx.coroutines.core") && !content.contains("coroutines-core")) {
            val commonMainDepsRegex = Regex(
                """(commonMain\.dependencies\s*\{[^}]*)(})""",
                RegexOption.DOT_MATCHES_ALL
            )
            val match = commonMainDepsRegex.find(content)

            if (match != null) {
                val depsContent = match.groupValues[1]
                val closingBrace = match.groupValues[2]

                // Check if coroutines is already in the deps
                if (!depsContent.contains("coroutines")) {
                    val newDepsContent = depsContent.trimEnd() +
                        "\n            implementation(libs.kotlinx.coroutines.core)\n        "
                    content = content.replace(match.value, newDepsContent + closingBrace)
                    modified = true
                }
            }
        }

        if (modified) {
            buildFile.writeText(content)
        }

        return modified
    }

    /**
     * Builds the list of Firebase module dependencies.
     */
    private fun buildFirebaseDependencies(modules: FirebaseModules): List<String> {
        val deps = mutableListOf<String>()
        if (modules.core) deps.add("firebase-core")
        if (modules.auth) deps.add("firebase-auth")
        if (modules.firestore) deps.add("firebase-firestore")
        if (modules.storage) deps.add("firebase-storage")
        return deps
    }
}
