package com.riadmahi.firebase.cli.parsers

import java.nio.file.Path
import kotlin.io.path.*

/**
 * Represents the dependency manager used in the iOS project.
 */
enum class IosDependencyManager {
    NONE,
    COCOAPODS,
    SPM,
    BOTH
}

class IosProjectParser(private val projectRoot: Path) {

    /**
     * Detect which dependency manager is currently used in the iOS project.
     */
    fun detectDependencyManager(): IosDependencyManager {
        val hasPodfile = findPodfile() != null
        val hasSpm = hasSpmDependencies()

        return when {
            hasPodfile && hasSpm -> IosDependencyManager.BOTH
            hasPodfile -> IosDependencyManager.COCOAPODS
            hasSpm -> IosDependencyManager.SPM
            else -> IosDependencyManager.NONE
        }
    }

    /**
     * Find the Podfile in the iOS app directory.
     */
    private fun findPodfile(): Path? {
        val possibleLocations = listOf(
            "demo/iosApp/Podfile",
            "iosApp/Podfile"
        )

        return possibleLocations
            .map { projectRoot.resolve(it) }
            .firstOrNull { it.exists() }
    }

    /**
     * Check if the project has SPM dependencies in the pbxproj.
     */
    private fun hasSpmDependencies(): Boolean {
        val pbxprojPaths = listOf(
            "demo/iosApp/iosApp.xcodeproj/project.pbxproj",
            "iosApp/iosApp.xcodeproj/project.pbxproj",
            "iosApp.xcodeproj/project.pbxproj"
        )

        for (pbxprojPath in pbxprojPaths) {
            val pbxproj = projectRoot.resolve(pbxprojPath)
            if (pbxproj.exists()) {
                val parser = PbxprojParser(pbxproj)
                if (parser.hasSwiftPackages()) {
                    return true
                }
            }
        }
        return false
    }

    fun detectBundleId(): String? {
        // Try multiple locations for iOS bundle ID

        // 1. Check iosApp directory for xcconfig
        val xcconfigPaths = listOf(
            "demo/iosApp/Configuration/Config.xcconfig",
            "demo/iosApp/Config.xcconfig",
            "iosApp/Configuration/Config.xcconfig",
            "iosApp/Config.xcconfig"
        )

        for (xcconfigPath in xcconfigPaths) {
            val xcconfig = projectRoot.resolve(xcconfigPath)
            if (xcconfig.exists()) {
                extractBundleIdFromXcconfig(xcconfig)?.let { return it }
            }
        }

        // 2. Check project.pbxproj
        val pbxprojPaths = listOf(
            "demo/iosApp/iosApp.xcodeproj/project.pbxproj",
            "iosApp/iosApp.xcodeproj/project.pbxproj",
            "iosApp.xcodeproj/project.pbxproj"
        )

        for (pbxprojPath in pbxprojPaths) {
            val pbxproj = projectRoot.resolve(pbxprojPath)
            if (pbxproj.exists()) {
                extractBundleIdFromPbxproj(pbxproj)?.let { return it }
            }
        }

        // 3. Check Info.plist
        val infoPlistPaths = listOf(
            "demo/iosApp/iosApp/Info.plist",
            "demo/iosApp/Info.plist",
            "iosApp/iosApp/Info.plist",
            "iosApp/Info.plist"
        )

        for (infoPlistPath in infoPlistPaths) {
            val infoPlist = projectRoot.resolve(infoPlistPath)
            if (infoPlist.exists()) {
                extractBundleIdFromInfoPlist(infoPlist)?.let { return it }
            }
        }

        return null
    }

    private fun extractBundleIdFromXcconfig(xcconfigFile: Path): String? {
        val content = xcconfigFile.readText()
        val bundleIdRegex = Regex("""BUNDLE_ID\s*=\s*(.+)""")
        val productBundleRegex = Regex("""PRODUCT_BUNDLE_IDENTIFIER\s*=\s*(.+)""")

        bundleIdRegex.find(content)?.groupValues?.get(1)?.trim()?.let {
            if (isValidBundleId(it)) return it
        }
        productBundleRegex.find(content)?.groupValues?.get(1)?.trim()?.let {
            if (isValidBundleId(it)) return it
        }

        return null
    }

    private fun extractBundleIdFromPbxproj(pbxprojFile: Path): String? {
        val content = pbxprojFile.readText()
        val bundleIdRegex = Regex("""PRODUCT_BUNDLE_IDENTIFIER\s*=\s*["']?([^;"'\n]+)["']?""")

        // Find all matches and return the first valid one (without Xcode variables)
        bundleIdRegex.findAll(content).forEach { match ->
            val bundleId = match.groupValues[1].trim()
            if (isValidBundleId(bundleId)) {
                return bundleId
            }
        }
        return null
    }

    // Check if bundle ID is valid (no unresolved Xcode variables)
    private fun isValidBundleId(bundleId: String): Boolean {
        // Must not contain Xcode variables like $(VAR) or ${VAR}
        if (bundleId.contains("$(") || bundleId.contains("\${")) {
            return false
        }
        // Must look like a valid bundle ID (reverse domain notation)
        if (!bundleId.matches(Regex("""^[a-zA-Z][a-zA-Z0-9.-]*(\.[a-zA-Z][a-zA-Z0-9-]*)+$"""))) {
            return false
        }
        return true
    }

    private fun extractBundleIdFromInfoPlist(infoPlistFile: Path): String? {
        val content = infoPlistFile.readText()

        // Look for CFBundleIdentifier
        val bundleIdRegex = Regex("""<key>CFBundleIdentifier</key>\s*<string>([^<]+)</string>""")
        val match = bundleIdRegex.find(content)

        // If it's a variable like $(PRODUCT_BUNDLE_IDENTIFIER), return null
        val bundleId = match?.groupValues?.get(1)
        if (bundleId != null && !bundleId.contains("$")) {
            return bundleId
        }

        return null
    }
}
