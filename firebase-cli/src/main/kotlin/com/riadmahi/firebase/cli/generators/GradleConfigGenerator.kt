package com.riadmahi.firebase.cli.generators

import java.nio.file.Path
import kotlin.io.path.*

/**
 * Generator for modifying Gradle configuration files to add Firebase support.
 */
class GradleConfigGenerator(private val projectRoot: Path) {

    companion object {
        private const val GOOGLE_SERVICES_VERSION = "4.4.2"
        private const val CRASHLYTICS_VERSION = "3.0.4"
        private const val GOOGLE_SERVICES_PLUGIN_ID = "com.google.gms.google-services"
        private const val CRASHLYTICS_PLUGIN_ID = "com.google.firebase.crashlytics"
    }

    /**
     * Configuration options for Firebase modules.
     */
    data class FirebaseModules(
        val core: Boolean = true,
        val auth: Boolean = false,
        val firestore: Boolean = false,
        val storage: Boolean = false,
        val messaging: Boolean = false,
        val analytics: Boolean = false,
        val remoteConfig: Boolean = false,
        val crashlytics: Boolean = false
    )

    /**
     * Configures all Gradle files for Firebase.
     * Returns a list of modified files.
     */
    fun configureGradle(modules: FirebaseModules = FirebaseModules()): List<String> {
        val modifiedFiles = mutableListOf<String>()

        // 1. Update libs.versions.toml
        if (updateVersionCatalog(modules)) {
            modifiedFiles.add("gradle/libs.versions.toml")
        }

        // 2. Update root build.gradle.kts
        if (updateRootBuildGradle(modules)) {
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
     * Updates libs.versions.toml to add google-services and crashlytics plugins.
     */
    private fun updateVersionCatalog(modules: FirebaseModules): Boolean {
        val versionCatalogPath = projectRoot.resolve("gradle/libs.versions.toml")
        if (!versionCatalogPath.exists()) return false

        var content = versionCatalogPath.readText()
        var modified = false

        // Add google-services if not present
        if (!content.contains("googleServices") && !content.contains("google-services")) {
            val versionsSection = "[versions]"
            if (content.contains(versionsSection)) {
                content = content.replace(
                    versionsSection,
                    "$versionsSection\ngoogleServices = \"$GOOGLE_SERVICES_VERSION\""
                )
            }

            val pluginEntry = "googleServices = { id = \"$GOOGLE_SERVICES_PLUGIN_ID\", version.ref = \"googleServices\" }"
            if (!content.contains(pluginEntry)) {
                content = content.trimEnd() + "\n$pluginEntry\n"
            }
            modified = true
        }

        // Add crashlytics plugin if crashlytics module is selected and not present
        if (modules.crashlytics && !content.contains("crashlyticsPlugin") && !content.contains(CRASHLYTICS_PLUGIN_ID)) {
            // Add version
            val versionsSection = "[versions]"
            if (content.contains(versionsSection) && !content.contains("crashlyticsPlugin")) {
                content = content.replace(
                    versionsSection,
                    "$versionsSection\ncrashlyticsPlugin = \"$CRASHLYTICS_VERSION\""
                )
            }

            // Add plugin
            val pluginEntry = "crashlyticsPlugin = { id = \"$CRASHLYTICS_PLUGIN_ID\", version.ref = \"crashlyticsPlugin\" }"
            if (!content.contains(pluginEntry)) {
                content = content.trimEnd() + "\n$pluginEntry\n"
            }
            modified = true
        }

        if (modified) {
            versionCatalogPath.writeText(content)
        }
        return modified
    }

    /**
     * Updates root build.gradle.kts to add google-services and crashlytics plugins.
     */
    private fun updateRootBuildGradle(modules: FirebaseModules): Boolean {
        val rootBuildFile = projectRoot.resolve("build.gradle.kts")
        if (!rootBuildFile.exists()) return false

        var content = rootBuildFile.readText()
        var modified = false

        val pluginsBlockRegex = Regex("""(plugins\s*\{[^}]*)(})""", RegexOption.DOT_MATCHES_ALL)
        val match = pluginsBlockRegex.find(content)

        if (match != null) {
            var pluginsContent = match.groupValues[1]
            val closingBrace = match.groupValues[2]

            // Add google-services if not present
            if (!pluginsContent.contains("googleServices")) {
                pluginsContent = pluginsContent.trimEnd() +
                    "\n    alias(libs.plugins.googleServices) apply false\n"
                modified = true
            }

            // Add crashlytics plugin if crashlytics module is selected and not present
            if (modules.crashlytics && !pluginsContent.contains("crashlyticsPlugin")) {
                pluginsContent = pluginsContent.trimEnd() +
                    "\n    alias(libs.plugins.crashlyticsPlugin) apply false\n"
                modified = true
            }

            if (modified) {
                content = content.replace(match.value, pluginsContent + closingBrace)
                rootBuildFile.writeText(content)
            }
        }

        return modified
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
     * Updates the app's build.gradle.kts to apply google-services, crashlytics plugin and add Firebase dependencies.
     */
    private fun updateAppBuildGradle(buildFile: Path, modules: FirebaseModules): Boolean {
        var content = buildFile.readText()
        var modified = false

        // 1. Add plugins if not present
        val pluginsBlockRegex = Regex("""(plugins\s*\{[^}]*)(})""", RegexOption.DOT_MATCHES_ALL)
        val pluginsMatch = pluginsBlockRegex.find(content)

        if (pluginsMatch != null) {
            var pluginsContent = pluginsMatch.groupValues[1]
            val closingBrace = pluginsMatch.groupValues[2]
            var pluginsModified = false

            // Add google-services plugin
            if (!pluginsContent.contains("googleServices")) {
                pluginsContent = pluginsContent.trimEnd() +
                    "\n    alias(libs.plugins.googleServices)\n"
                pluginsModified = true
            }

            // Add crashlytics plugin if crashlytics module is selected
            if (modules.crashlytics && !pluginsContent.contains("crashlyticsPlugin")) {
                pluginsContent = pluginsContent.trimEnd() +
                    "\n    alias(libs.plugins.crashlyticsPlugin)\n"
                pluginsModified = true
            }

            if (pluginsModified) {
                content = content.replace(pluginsMatch.value, pluginsContent + closingBrace)
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
        if (modules.messaging) deps.add("firebase-messaging")
        if (modules.analytics) deps.add("firebase-analytics")
        if (modules.remoteConfig) deps.add("firebase-remoteconfig")
        if (modules.crashlytics) deps.add("firebase-crashlytics")
        return deps
    }
}
