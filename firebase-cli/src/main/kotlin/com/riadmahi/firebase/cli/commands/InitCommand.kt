package com.riadmahi.firebase.cli.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.path
import com.riadmahi.firebase.cli.firebase.FirebaseManagementApi
import com.riadmahi.firebase.cli.firebase.FirebaseProject
import com.riadmahi.firebase.cli.firebase.FirebaseToolsBridge
import com.riadmahi.firebase.cli.generators.AndroidConfigGenerator
import com.riadmahi.firebase.cli.generators.GradleConfigGenerator
import com.riadmahi.firebase.cli.generators.IosConfigGenerator
import com.riadmahi.firebase.cli.parsers.AndroidProjectParser
import com.riadmahi.firebase.cli.parsers.IosProjectParser
import com.riadmahi.firebase.cli.ui.KFireTerminal
import com.riadmahi.firebase.cli.ui.KFireTerminal.WizardStep
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.writeText
import kotlin.io.path.createDirectories

class InitCommand : CliktCommand(name = "init") {
    override fun help(context: Context): String = "Initialize Firebase for a new KMP project"

    private val projectPath by option(
        "--path", "-p"
    ).path(mustExist = true).default(Path(".")).help("Path to the KMP project root")

    // Wizard steps
    private val wizardSteps = listOf(
        WizardStep(1, "Pre-flight Checks", "Verifying environment"),
        WizardStep(2, "Firebase Project", "Connect to Firebase"),
        WizardStep(3, "Select Modules", "Choose Firebase services"),
        WizardStep(4, "Platform Setup", "Configure Android & iOS"),
        WizardStep(5, "Finalize", "Apply configuration")
    )

    override fun run() = runBlocking {
        // ═══════════════════════════════════════════════════════════════════
        // WELCOME
        // ═══════════════════════════════════════════════════════════════════

        KFireTerminal.logo()
        KFireTerminal.welcomeBanner()

        // Show initial wizard state
        KFireTerminal.stepIndicator(wizardSteps, 0)

        delay(500)

        // ═══════════════════════════════════════════════════════════════════
        // STEP 1: PRE-FLIGHT CHECKS
        // ═══════════════════════════════════════════════════════════════════

        KFireTerminal.currentStep(1, 5, "Pre-flight Checks")

        val bridge = FirebaseToolsBridge()

        // Check Firebase Tools
        val spinner1 = KFireTerminal.Spinner("Checking firebase-tools installation...")
        spinner1.start()
        delay(400)

        val hasFirebaseTools = try {
            bridge.isInstalled()
        } catch (e: Exception) {
            false
        }

        if (!hasFirebaseTools) {
            spinner1.error("firebase-tools not found")
            KFireTerminal.blank()
            KFireTerminal.box("Installation Required", listOf(
                "Firebase CLI is required to continue.",
                "",
                "Install with npm:",
                "  ${KFireTerminal.kotlin("npm install -g firebase-tools")}",
                "",
                "Then run ${KFireTerminal.fire("kfire init")} again."
            ), KFireTerminal.BoxStyle.ERROR)
            return@runBlocking
        }
        spinner1.success("firebase-tools installed")

        // Check Authentication
        val spinner2 = KFireTerminal.Spinner("Checking Firebase authentication...")
        spinner2.start()
        delay(400)

        val isLoggedIn = bridge.isLoggedIn()

        if (!isLoggedIn) {
            spinner2.error("Not authenticated")
            KFireTerminal.blank()

            if (KFireTerminal.confirm("Would you like to login now?")) {
                KFireTerminal.blank()
                KFireTerminal.info("Opening browser for authentication...")
                try {
                    bridge.login()
                    KFireTerminal.success("Authentication successful!")
                } catch (e: Exception) {
                    KFireTerminal.error("Authentication failed: ${e.message}")
                    return@runBlocking
                }
            } else {
                KFireTerminal.info("Run ${KFireTerminal.fire("firebase login")} when ready")
                return@runBlocking
            }
        } else {
            spinner2.success("Authenticated with Firebase")
        }

        // Scan Project
        val spinner3 = KFireTerminal.Spinner("Scanning project structure...")
        spinner3.start()
        delay(600)

        val androidParser = AndroidProjectParser(projectPath)
        val iosParser = IosProjectParser(projectPath)

        var detectedAndroid = androidParser.detectPackageName()
        var detectedIos = iosParser.detectBundleId()

        spinner3.success("Project scanned")

        // Show detected platforms
        KFireTerminal.blank()
        if (detectedAndroid != null || detectedIos != null) {
            KFireTerminal.box("Detected Platforms", buildList {
                detectedAndroid?.let {
                    add("${KFireTerminal.success("✓")} Android: ${KFireTerminal.kotlin(it)}")
                } ?: add("${KFireTerminal.muted("○")} Android: ${KFireTerminal.muted("not detected")}")

                detectedIos?.let {
                    add("${KFireTerminal.success("✓")} iOS: ${KFireTerminal.kotlin(it)}")
                } ?: add("${KFireTerminal.muted("○")} iOS: ${KFireTerminal.muted("not detected")}")
            }, KFireTerminal.BoxStyle.SUCCESS)
        } else {
            KFireTerminal.warning("No KMP targets detected automatically")
        }

        // Allow manual entry for missing platforms
        if (detectedAndroid == null) {
            KFireTerminal.blank()
            val manualAndroid = KFireTerminal.prompt("Android package name (leave empty to skip)", null)
            if (manualAndroid.isNotBlank()) {
                detectedAndroid = manualAndroid
            }
        }

        if (detectedIos == null) {
            val manualIos = KFireTerminal.prompt("iOS bundle ID (leave empty to skip)", null)
            if (manualIos.isNotBlank()) {
                detectedIos = manualIos
            }
        }

        if (detectedAndroid == null && detectedIos == null) {
            KFireTerminal.blank()
            KFireTerminal.error("No platforms configured. Cannot continue.")
            return@runBlocking
        }

        // ═══════════════════════════════════════════════════════════════════
        // STEP 2: FIREBASE PROJECT
        // ═══════════════════════════════════════════════════════════════════

        KFireTerminal.currentStep(2, 5, "Connect to Firebase")

        val spinnerToken = KFireTerminal.Spinner("Fetching access token...")
        spinnerToken.start()

        val token = bridge.getAccessToken()
        if (token == null) {
            spinnerToken.error("Could not get access token")
            KFireTerminal.blank()
            KFireTerminal.box("Authentication Error", listOf(
                "Failed to retrieve Firebase access token.",
                "",
                "Try logging in again:",
                "  ${KFireTerminal.kotlin("firebase login --reauth")}"
            ), KFireTerminal.BoxStyle.ERROR)
            return@runBlocking
        }
        spinnerToken.success("Access token retrieved")

        val api = FirebaseManagementApi(token)

        val spinnerProjects = KFireTerminal.Spinner("Loading your Firebase projects...")
        spinnerProjects.start()

        val projects = try {
            api.listProjects()
        } catch (e: Exception) {
            spinnerProjects.error("Failed to load projects")
            KFireTerminal.error("Error: ${e.message}")
            return@runBlocking
        }

        spinnerProjects.success("Found ${projects.size} project(s)")

        val selectedProject: FirebaseProject = if (projects.isEmpty()) {
            KFireTerminal.blank()
            KFireTerminal.box("No Projects Found", listOf(
                "You don't have any Firebase projects yet.",
                "",
                "Create one at:",
                "  ${KFireTerminal.muted("https://console.firebase.google.com")}",
                "",
                "Then run ${KFireTerminal.fire("kfire init")} again."
            ), KFireTerminal.BoxStyle.INFO)
            return@runBlocking
        } else {
            KFireTerminal.blank()
            val projectOptions = projects.map { p ->
                "${p.displayName} ${KFireTerminal.muted("(${p.projectId})")}"
            }
            val index = KFireTerminal.select("Select your Firebase project", projectOptions)
            projects[index]
        }

        KFireTerminal.blank()
        KFireTerminal.success("Selected: ${KFireTerminal.fire(selectedProject.displayName)}")

        // ═══════════════════════════════════════════════════════════════════
        // STEP 3: SELECT MODULES
        // ═══════════════════════════════════════════════════════════════════

        KFireTerminal.currentStep(3, 5, "Choose Firebase Services")

        val moduleOptions = listOf(
            "Authentication ${KFireTerminal.muted("— Sign-in with Email, Google, Apple, Phone")}",
            "Firestore ${KFireTerminal.muted("— NoSQL cloud database with offline sync")}",
            "Storage ${KFireTerminal.muted("— File uploads, downloads, and cloud storage")}"
        )

        val selectedIndices = KFireTerminal.multiSelect(
            "Which Firebase services do you need?",
            moduleOptions,
            setOf(0, 1) // Default: select Auth and Firestore
        )

        val useAuth = 0 in selectedIndices
        val useFirestore = 1 in selectedIndices
        val useStorage = 2 in selectedIndices

        KFireTerminal.blank()
        KFireTerminal.box("Selected Modules", buildList {
            add("${KFireTerminal.success("✓")} Core ${KFireTerminal.muted("(always included)")}")
            if (useAuth) add("${KFireTerminal.success("✓")} Authentication")
            if (useFirestore) add("${KFireTerminal.success("✓")} Firestore")
            if (useStorage) add("${KFireTerminal.success("✓")} Storage")
        }, KFireTerminal.BoxStyle.SUCCESS)

        // ═══════════════════════════════════════════════════════════════════
        // STEP 4: PLATFORM SETUP
        // ═══════════════════════════════════════════════════════════════════

        KFireTerminal.currentStep(4, 5, "Configure Platforms")

        var successCount = 0

        // Android Configuration
        if (detectedAndroid != null) {
            val spinnerAndroid = KFireTerminal.Spinner("Configuring Android...")
            spinnerAndroid.start()

            try {
                spinnerAndroid.updateStatus("Registering Android app...")
                val app = api.getOrCreateAndroidApp(selectedProject.projectId, detectedAndroid)

                spinnerAndroid.updateStatus("Downloading google-services.json...")
                val config = api.getAndroidAppConfig(selectedProject.projectId, app.appId)

                spinnerAndroid.updateStatus("Writing configuration file...")
                val generator = AndroidConfigGenerator(projectPath)
                generator.writeGoogleServicesJson(config)

                spinnerAndroid.success("Android configured ${KFireTerminal.muted("→ google-services.json")}")
                successCount++
            } catch (e: Exception) {
                spinnerAndroid.error("Android setup failed: ${e.message}")
            }
        }

        // iOS Configuration
        if (detectedIos != null) {
            val spinnerIos = KFireTerminal.Spinner("Configuring iOS...")
            spinnerIos.start()

            try {
                spinnerIos.updateStatus("Registering iOS app...")
                val app = api.getOrCreateIosApp(selectedProject.projectId, detectedIos)

                spinnerIos.updateStatus("Downloading GoogleService-Info.plist...")
                val config = api.getIosAppConfig(selectedProject.projectId, app.appId)

                spinnerIos.updateStatus("Writing configuration file...")
                val generator = IosConfigGenerator(projectPath)
                generator.writeGoogleServiceInfoPlist(config)

                spinnerIos.success("iOS configured ${KFireTerminal.muted("→ GoogleService-Info.plist")}")
                successCount++
            } catch (e: Exception) {
                spinnerIos.error("iOS setup failed: ${e.message}")
            }
        }

        // ═══════════════════════════════════════════════════════════════════
        // STEP 5: FINALIZE
        // ═══════════════════════════════════════════════════════════════════

        KFireTerminal.currentStep(5, 5, "Applying Configuration")

        // Update Gradle
        val spinnerGradle = KFireTerminal.Spinner("Updating Gradle configuration...")
        spinnerGradle.start()

        try {
            val gradleGenerator = GradleConfigGenerator(projectPath)
            val modules = GradleConfigGenerator.FirebaseModules(
                core = true,
                auth = useAuth,
                firestore = useFirestore,
                storage = useStorage
            )

            val modified = gradleGenerator.configureGradle(modules)
            if (modified.isNotEmpty()) {
                spinnerGradle.success("Gradle updated ${KFireTerminal.muted("→ ${modified.joinToString(", ")}")}")
            } else {
                spinnerGradle.success("Dependencies already configured")
            }
        } catch (e: Exception) {
            spinnerGradle.error("Gradle configuration failed")
            KFireTerminal.warning("You may need to add dependencies manually")
        }

        // Generate sample code
        val spinnerSample = KFireTerminal.Spinner("Generating sample code...")
        spinnerSample.start()
        delay(300)

        generateSampleCode(useAuth, useFirestore, useStorage)
        spinnerSample.success("Sample code created ${KFireTerminal.muted("→ FirebaseInit.kt")}")

        // ═══════════════════════════════════════════════════════════════════
        // COMPLETION
        // ═══════════════════════════════════════════════════════════════════

        KFireTerminal.blank()

        if (successCount > 0) {
            KFireTerminal.successScreen(
                "Setup Complete!",
                "Firebase is ready to use in your KMP project."
            )

            // Summary table
            KFireTerminal.table(
                listOf("Component", "Status"),
                listOfNotNull(
                    listOf("Firebase Project", KFireTerminal.fire(selectedProject.displayName)),
                    if (detectedAndroid != null) listOf("Android", KFireTerminal.success("✓ Configured")) else null,
                    if (detectedIos != null) listOf("iOS", KFireTerminal.success("✓ Configured")) else null,
                    listOf("Core Module", KFireTerminal.success("✓ Added")),
                    if (useAuth) listOf("Auth Module", KFireTerminal.success("✓ Added")) else null,
                    if (useFirestore) listOf("Firestore Module", KFireTerminal.success("✓ Added")) else null,
                    if (useStorage) listOf("Storage Module", KFireTerminal.success("✓ Added")) else null
                )
            )

            KFireTerminal.nextSteps(listOf(
                "Sync your Gradle project ${KFireTerminal.muted("(click 'Sync Now' in IDE)")}",
                "Check ${KFireTerminal.kotlin("FirebaseInit.kt")} for initialization code",
                "Start building your app! ${KFireTerminal.fire("🔥")}"
            ))

            // Quick reference code block
            KFireTerminal.codeBlock("Quick Start", buildString {
                appendLine("// Initialize Firebase (call once at app startup)")
                appendLine("FirebaseInit.initialize()")
                if (useAuth) {
                    appendLine()
                    appendLine("// Get Auth instance")
                    appendLine("val auth = FirebaseInit.auth")
                }
                if (useFirestore) {
                    appendLine()
                    appendLine("// Get Firestore instance")
                    appendLine("val db = FirebaseInit.firestore")
                }
                if (useStorage) {
                    appendLine()
                    appendLine("// Get Storage instance")
                    appendLine("val storage = FirebaseInit.storage")
                }
            }.trim())

        } else {
            KFireTerminal.errorScreen(
                "Setup Incomplete",
                "Please check the errors above and try again."
            )
        }
    }

    private fun generateSampleCode(useAuth: Boolean, useFirestore: Boolean, useStorage: Boolean) {
        // Find the correct commonMain location based on project structure
        val possibleLocations = listOf(
            "demo/shared/src/commonMain/kotlin/firebase",
            "shared/src/commonMain/kotlin/firebase",
            "composeApp/src/commonMain/kotlin/firebase",
            "src/commonMain/kotlin/firebase"
        )

        val sampleDir = possibleLocations
            .map { projectPath.resolve(it) }
            .firstOrNull { it.parent.parent.parent.exists() } // Check if src/commonMain/kotlin exists
            ?: projectPath.resolve("src/commonMain/kotlin/firebase")

        if (!sampleDir.exists()) {
            sampleDir.createDirectories()
        }

        val imports = buildString {
            appendLine("package firebase")
            appendLine()
            appendLine("import com.riadmahi.firebase.core.FirebaseApp")
            if (useAuth) {
                appendLine("import com.riadmahi.firebase.auth.FirebaseAuth")
            }
            if (useFirestore) {
                appendLine("import com.riadmahi.firebase.firestore.FirebaseFirestore")
            }
            if (useStorage) {
                appendLine("import com.riadmahi.firebase.storage.FirebaseStorage")
            }
            appendLine()
        }

        val code = buildString {
            append(imports)
            appendLine("/**")
            appendLine(" * Firebase initialization for your KMP project.")
            appendLine(" * Call [initialize] early in your app lifecycle.")
            appendLine(" *")
            appendLine(" * Generated by KFire CLI")
            appendLine(" */")
            appendLine("object FirebaseInit {")
            appendLine()
            appendLine("    private var initialized = false")
            appendLine()
            appendLine("    /**")
            appendLine("     * Initialize Firebase. Call this once at app startup.")
            appendLine("     *")
            appendLine("     * - Android: Call in Application.onCreate() or MainActivity.onCreate()")
            appendLine("     * - iOS: Call in AppDelegate or SwiftUI App init")
            appendLine("     */")
            appendLine("    fun initialize() {")
            appendLine("        if (initialized) return")
            appendLine("        ")
            appendLine("        // Initialize Firebase with default configuration")
            appendLine("        // (uses google-services.json on Android, GoogleService-Info.plist on iOS)")
            appendLine("        FirebaseApp.initialize()")
            appendLine("        ")
            appendLine("        initialized = true")
            appendLine("    }")
            appendLine()

            if (useAuth) {
                appendLine("    /**")
                appendLine("     * Get the Firebase Auth instance.")
                appendLine("     */")
                appendLine("    val auth: FirebaseAuth")
                appendLine("        get() {")
                appendLine("            check(initialized) { \"Call initialize() first\" }")
                appendLine("            return FirebaseAuth.getInstance()")
                appendLine("        }")
                appendLine()
            }

            if (useFirestore) {
                appendLine("    /**")
                appendLine("     * Get the Firestore instance.")
                appendLine("     */")
                appendLine("    val firestore: FirebaseFirestore")
                appendLine("        get() {")
                appendLine("            check(initialized) { \"Call initialize() first\" }")
                appendLine("            return FirebaseFirestore.getInstance()")
                appendLine("        }")
                appendLine()
            }

            if (useStorage) {
                appendLine("    /**")
                appendLine("     * Get the Firebase Storage instance.")
                appendLine("     */")
                appendLine("    val storage: FirebaseStorage")
                appendLine("        get() {")
                appendLine("            check(initialized) { \"Call initialize() first\" }")
                appendLine("            return FirebaseStorage.getInstance()")
                appendLine("        }")
                appendLine()
            }

            appendLine("}")
        }

        val filePath = sampleDir.resolve("FirebaseInit.kt")
        filePath.writeText(code)
    }
}
