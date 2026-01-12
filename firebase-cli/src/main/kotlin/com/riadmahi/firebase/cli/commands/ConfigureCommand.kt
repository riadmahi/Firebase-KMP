package com.riadmahi.firebase.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.path
import com.riadmahi.firebase.cli.firebase.FirebaseManagementApi
import com.riadmahi.firebase.cli.firebase.FirebaseToolsBridge
import com.riadmahi.firebase.cli.generators.AndroidConfigGenerator
import com.riadmahi.firebase.cli.generators.GradleConfigGenerator
import com.riadmahi.firebase.cli.generators.IosConfigGenerator
import com.riadmahi.firebase.cli.parsers.AndroidProjectParser
import com.riadmahi.firebase.cli.parsers.IosProjectParser
import com.riadmahi.firebase.cli.ui.KFireTerminal
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.nio.file.Path
import kotlin.io.path.Path

class ConfigureCommand : CliktCommand(name = "configure") {
    override fun help(context: Context): String = "Configure Firebase for your KMP project"

    private val projectPath by option(
        "--path", "-p"
    ).path(mustExist = true).default(Path(".")).help("Path to the KMP project root")

    private val firebaseProject by option(
        "--project"
    ).help("Firebase project ID")

    private val androidPackage by option(
        "--android-package"
    ).help("Android package name (auto-detected if not specified)")

    private val iosBundleId by option(
        "--ios-bundle-id"
    ).help("iOS bundle identifier (auto-detected if not specified)")

    private val platforms by option(
        "--platforms"
    ).split(",").default(listOf("android", "ios")).help("Platforms to configure (comma-separated: android,ios)")

    private val modules by option(
        "--modules", "-m"
    ).split(",").default(listOf("core")).help("Firebase modules to add (comma-separated: core,auth,firestore)")

    private val yes by option(
        "--yes", "-y"
    ).flag(default = false).help("Accept all defaults and run non-interactively")

    override fun run() = runBlocking {
        KFireTerminal.logo()
        KFireTerminal.header("Firebase Configuration")

        // Step 1: Detect project
        KFireTerminal.step(1, "Detecting project structure...")
        KFireTerminal.blank()

        val projectInfo = detectProject()

        if (projectInfo.androidPackage != null) {
            KFireTerminal.success("Android: ${KFireTerminal.kotlin(projectInfo.androidPackage)}")
        } else {
            KFireTerminal.warning("Android: Not detected")
        }

        if (projectInfo.iosBundleId != null) {
            KFireTerminal.success("iOS: ${KFireTerminal.kotlin(projectInfo.iosBundleId)}")
        } else {
            KFireTerminal.warning("iOS: Not detected")
        }

        KFireTerminal.blank()

        // Step 2: Check Firebase authentication
        KFireTerminal.step(2, "Checking Firebase authentication...")

        val bridge = FirebaseToolsBridge()
        if (!bridge.isLoggedIn()) {
            KFireTerminal.blank()
            KFireTerminal.error("Not logged in to Firebase")
            KFireTerminal.blank()
            KFireTerminal.info("Please run: ${KFireTerminal.fire("kfire login")}")
            KFireTerminal.blank()
            return@runBlocking
        }

        val token = bridge.getAccessToken()
        if (token == null) {
            KFireTerminal.blank()
            KFireTerminal.error("Could not get Firebase access token")
            KFireTerminal.info("Please run: ${KFireTerminal.fire("firebase login")}")
            KFireTerminal.blank()
            return@runBlocking
        }

        KFireTerminal.success("Authenticated")
        KFireTerminal.blank()

        val api = FirebaseManagementApi(token)

        // Step 3: Select Firebase project
        KFireTerminal.step(3, "Loading Firebase projects...")
        KFireTerminal.blank()

        val projects = try {
            api.listProjects()
        } catch (e: Exception) {
            KFireTerminal.error("Failed to load projects: ${e.message}")
            return@runBlocking
        }

        if (projects.isEmpty()) {
            KFireTerminal.error("No Firebase projects found")
            KFireTerminal.blank()
            KFireTerminal.info("Create a project at: ${KFireTerminal.muted("https://console.firebase.google.com")}")
            return@runBlocking
        }

        val selectedProject = firebaseProject?.let { id ->
            projects.find { it.projectId == id }
                ?: run {
                    KFireTerminal.error("Project '$id' not found")
                    return@runBlocking
                }
        } ?: run {
            if (yes && projects.size == 1) {
                projects.first()
            } else {
                val index = KFireTerminal.select(
                    "Select Firebase project",
                    projects.map { "${it.displayName} ${KFireTerminal.muted("(${it.projectId})")}" }
                )
                projects[index]
            }
        }

        KFireTerminal.blank()
        KFireTerminal.success("Using project: ${KFireTerminal.fire(selectedProject.displayName)}")
        KFireTerminal.muted("    Project ID: ${selectedProject.projectId}")
        KFireTerminal.blank()

        // Step 4: Select modules
        KFireTerminal.step(4, "Select Firebase modules")
        KFireTerminal.blank()

        val availableModules = listOf("core", "auth", "firestore")
        val selectedModules = if (yes) {
            modules.toSet()
        } else {
            val defaults = modules.mapNotNull { mod ->
                availableModules.indexOf(mod).takeIf { it >= 0 }
            }.toSet()
            val indices = KFireTerminal.multiSelect(
                "Which modules do you need?",
                listOf(
                    "core ${KFireTerminal.muted("(required)")}",
                    "auth ${KFireTerminal.muted("(authentication)")}",
                    "firestore ${KFireTerminal.muted("(database)")}"
                ),
                defaults.ifEmpty { setOf(0) }
            )
            indices.map { availableModules[it] }.toSet() + "core" // core is always required
        }

        KFireTerminal.blank()
        KFireTerminal.success("Selected: ${selectedModules.joinToString(", ") { KFireTerminal.kotlin(it) }}")
        KFireTerminal.blank()

        // Step 5: Configure platforms
        KFireTerminal.step(5, "Configuring platforms...")
        KFireTerminal.blank()

        var configuredCount = 0

        // Configure Android
        if ("android" in platforms && projectInfo.androidPackage != null) {
            KFireTerminal.info("Configuring Android...")
            try {
                delay(300) // Small delay for visual feedback
                val app = api.getOrCreateAndroidApp(selectedProject.projectId, projectInfo.androidPackage)
                val config = api.getAndroidAppConfig(selectedProject.projectId, app.appId)

                val generator = AndroidConfigGenerator(projectPath)
                generator.writeGoogleServicesJson(config)
                KFireTerminal.success("Generated ${KFireTerminal.muted("google-services.json")}")
                configuredCount++
            } catch (e: Exception) {
                KFireTerminal.error("Android configuration failed: ${e.message}")
            }
        }

        // Configure iOS
        if ("ios" in platforms && projectInfo.iosBundleId != null) {
            KFireTerminal.info("Configuring iOS...")
            try {
                delay(300)
                val app = api.getOrCreateIosApp(selectedProject.projectId, projectInfo.iosBundleId)
                val config = api.getIosAppConfig(selectedProject.projectId, app.appId)

                val generator = IosConfigGenerator(projectPath)
                generator.writeGoogleServiceInfoPlist(config)
                KFireTerminal.success("Generated ${KFireTerminal.muted("GoogleService-Info.plist")}")
                configuredCount++
            } catch (e: Exception) {
                KFireTerminal.error("iOS configuration failed: ${e.message}")
            }
        }

        KFireTerminal.blank()

        // Step 6: Configure Gradle
        KFireTerminal.step(6, "Updating Gradle configuration...")
        KFireTerminal.blank()

        try {
            val gradleGenerator = GradleConfigGenerator(projectPath)
            val firebaseModules = GradleConfigGenerator.FirebaseModules(
                core = "core" in selectedModules,
                auth = "auth" in selectedModules,
                firestore = "firestore" in selectedModules
            )

            val modifiedFiles = gradleGenerator.configureGradle(firebaseModules)
            if (modifiedFiles.isNotEmpty()) {
                modifiedFiles.forEach { file ->
                    KFireTerminal.success("Updated ${KFireTerminal.muted(file)}")
                }
            } else {
                KFireTerminal.info("Gradle files already configured")
            }
        } catch (e: Exception) {
            KFireTerminal.warning("Could not update Gradle files: ${e.message}")
        }

        KFireTerminal.blank()
        KFireTerminal.divider()

        // Summary
        if (configuredCount > 0) {
            KFireTerminal.done("Firebase configured successfully!")

            KFireTerminal.table(
                listOf("Platform", "Status", "Config File"),
                listOfNotNull(
                    if ("android" in platforms && projectInfo.androidPackage != null)
                        listOf("Android", KFireTerminal.success("✓"), "google-services.json") else null,
                    if ("ios" in platforms && projectInfo.iosBundleId != null)
                        listOf("iOS", KFireTerminal.success("✓"), "GoogleService-Info.plist") else null
                )
            )

            KFireTerminal.nextSteps(listOf(
                "Sync your project with Gradle",
                "Initialize Firebase in your app: ${KFireTerminal.kotlin("FirebaseApp.initialize()")}",
                if ("auth" in selectedModules) "Use ${KFireTerminal.kotlin("FirebaseAuth.getInstance()")} for authentication" else null,
                if ("firestore" in selectedModules) "Use ${KFireTerminal.kotlin("FirebaseFirestore.getInstance()")} for database" else null
            ).filterNotNull())
        } else {
            KFireTerminal.warning("No platforms were configured")
            KFireTerminal.info("Make sure your project has Android and/or iOS targets")
        }
    }

    private fun detectProject(): ProjectInfo {
        val androidParser = AndroidProjectParser(projectPath)
        val iosParser = IosProjectParser(projectPath)

        return ProjectInfo(
            androidPackage = androidPackage ?: androidParser.detectPackageName(),
            iosBundleId = iosBundleId ?: iosParser.detectBundleId()
        )
    }
}

data class ProjectInfo(
    val androidPackage: String?,
    val iosBundleId: String?
)
