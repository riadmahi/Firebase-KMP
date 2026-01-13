package com.riadmahi.firebase.cli.parsers

import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.readText

/**
 * Represents a parsed pbxproj document with key identifiers.
 */
data class PbxprojDocument(
    val content: String,
    val rootObjectId: String,
    val projectId: String,
    val mainTargetId: String?,
    val frameworksBuildPhaseId: String?,
    val hasSwiftPackages: Boolean,
    val existingPackageRefs: List<String>
)

/**
 * Parser for Xcode project.pbxproj files.
 * Extracts key identifiers needed for SPM injection.
 */
class PbxprojParser(private val pbxprojPath: Path) {

    companion object {
        private val ROOT_OBJECT_REGEX = Regex("""rootObject\s*=\s*([A-F0-9]{24})""")
        private val PROJECT_SECTION_REGEX = Regex(
            """/\* Begin PBXProject section \*/\s*([A-F0-9]{24})\s*/\*[^*]*\*/\s*=\s*\{""",
            RegexOption.MULTILINE
        )
        private val NATIVE_TARGET_REGEX = Regex(
            """/\* Begin PBXNativeTarget section \*/\s*([A-F0-9]{24})\s*/\*[^*]*\*/\s*=\s*\{""",
            RegexOption.MULTILINE
        )
        private val FRAMEWORKS_BUILD_PHASE_REGEX = Regex(
            """/\* Begin PBXFrameworksBuildPhase section \*/\s*([A-F0-9]{24})\s*/\*[^*]*\*/\s*=\s*\{""",
            RegexOption.MULTILINE
        )
        private val PACKAGE_REFERENCE_SECTION_REGEX = Regex(
            """/\* Begin XCRemoteSwiftPackageReference section \*/"""
        )
        private val EXISTING_PACKAGE_REFS_REGEX = Regex(
            """([A-F0-9]{24})\s*/\*\s*XCRemoteSwiftPackageReference"""
        )
    }

    /**
     * Check if the pbxproj file exists.
     */
    fun exists(): Boolean = pbxprojPath.exists()

    /**
     * Parse the pbxproj file and extract key identifiers.
     */
    fun parse(): PbxprojDocument? {
        if (!exists()) return null

        val content = pbxprojPath.readText()

        val rootObjectId = ROOT_OBJECT_REGEX.find(content)?.groupValues?.get(1) ?: return null
        val projectId = PROJECT_SECTION_REGEX.find(content)?.groupValues?.get(1) ?: rootObjectId
        val mainTargetId = NATIVE_TARGET_REGEX.find(content)?.groupValues?.get(1)
        val frameworksBuildPhaseId = FRAMEWORKS_BUILD_PHASE_REGEX.find(content)?.groupValues?.get(1)
        val hasSwiftPackages = PACKAGE_REFERENCE_SECTION_REGEX.containsMatchIn(content)
        val existingPackageRefs = EXISTING_PACKAGE_REFS_REGEX.findAll(content)
            .map { it.groupValues[1] }
            .toList()

        return PbxprojDocument(
            content = content,
            rootObjectId = rootObjectId,
            projectId = projectId,
            mainTargetId = mainTargetId,
            frameworksBuildPhaseId = frameworksBuildPhaseId,
            hasSwiftPackages = hasSwiftPackages,
            existingPackageRefs = existingPackageRefs
        )
    }

    /**
     * Check if the project already has Swift Package Manager dependencies.
     */
    fun hasSwiftPackages(): Boolean {
        if (!exists()) return false
        val content = pbxprojPath.readText()
        return PACKAGE_REFERENCE_SECTION_REGEX.containsMatchIn(content)
    }
}
