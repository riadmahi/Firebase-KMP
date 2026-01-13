package com.riadmahi.firebase.cli.generators

import com.riadmahi.firebase.cli.parsers.PbxprojDocument

/**
 * Represents a Swift Package to be added to the Xcode project.
 */
data class SwiftPackage(
    val repositoryURL: String,
    val version: String,
    val products: List<String>
)

/**
 * Modifier for Xcode project.pbxproj files.
 * Injects Swift Package Manager dependencies.
 */
class PbxprojModifier(private val document: PbxprojDocument) {

    companion object {
        private val CHARS = "0123456789ABCDEF"

        /**
         * Generate a unique 24-character hex ID for pbxproj entries.
         */
        fun generateId(): String = (1..24).map { CHARS.random() }.joinToString("")
    }

    /**
     * Add Swift Package dependencies to the pbxproj.
     * Returns the modified content string.
     */
    fun addSwiftPackages(packages: List<SwiftPackage>): String {
        if (packages.isEmpty()) return document.content

        // Check if Firebase packages already exist
        if (document.content.contains("firebase-ios-sdk")) {
            return document.content // Already configured, skip
        }

        val targetId = document.mainTargetId
            ?: throw IllegalStateException("No native target found in pbxproj")
        val frameworksPhaseId = document.frameworksBuildPhaseId
            ?: throw IllegalStateException("No frameworks build phase found in pbxproj")

        // Generate IDs for all packages and products
        val packageRefIds = packages.map { generateId() }
        val productDependencies = packages.flatMapIndexed { pkgIndex, pkg ->
            pkg.products.map { product ->
                Triple(generateId(), product, packageRefIds[pkgIndex])
            }
        }
        val buildFileIds = productDependencies.map { Triple(generateId(), it.first, it.second) }

        var content = document.content

        // 1. Add XCRemoteSwiftPackageReference section
        content = addPackageReferenceSection(content, packages, packageRefIds)

        // 2. Add XCSwiftPackageProductDependency section
        content = addProductDependencySection(content, productDependencies)

        // 3. Add PBXBuildFile entries for SPM products
        content = addBuildFileEntries(content, buildFileIds)

        // 4. Modify PBXFrameworksBuildPhase to include SPM products
        content = modifyFrameworksBuildPhase(content, frameworksPhaseId, buildFileIds)

        // 5. Add packageReferences to PBXProject
        content = addPackageReferencesToProject(content, document.projectId, packageRefIds)

        // 6. Add packageProductDependencies to PBXNativeTarget
        content = addProductDependenciesToTarget(content, targetId, productDependencies.map { it.first })

        return content
    }

    private fun addPackageReferenceSection(
        content: String,
        packages: List<SwiftPackage>,
        packageRefIds: List<String>
    ): String {
        val section = buildString {
            appendLine("/* Begin XCRemoteSwiftPackageReference section */")
            packages.forEachIndexed { index, pkg ->
                val repoName = pkg.repositoryURL.substringAfterLast("/").removeSuffix(".git")
                appendLine("\t\t${packageRefIds[index]} /* XCRemoteSwiftPackageReference \"$repoName\" */ = {")
                appendLine("\t\t\tisa = XCRemoteSwiftPackageReference;")
                appendLine("\t\t\trepositoryURL = \"${pkg.repositoryURL}\";")
                appendLine("\t\t\trequirement = {")
                appendLine("\t\t\t\tkind = upToNextMajorVersion;")
                appendLine("\t\t\t\tminimumVersion = ${pkg.version};")
                appendLine("\t\t\t};")
                appendLine("\t\t};")
            }
            append("/* End XCRemoteSwiftPackageReference section */")
        }

        // Insert before the closing of objects section (before rootObject line)
        return content.replace(
            "\trootObject = ",
            "$section\n\t};\n\trootObject = "
        ).replace("\t};\n$section", section)
    }

    private fun addProductDependencySection(
        content: String,
        productDependencies: List<Triple<String, String, String>>
    ): String {
        val section = buildString {
            appendLine("/* Begin XCSwiftPackageProductDependency section */")
            productDependencies.forEach { (id, productName, packageRefId) ->
                appendLine("\t\t$id /* $productName */ = {")
                appendLine("\t\t\tisa = XCSwiftPackageProductDependency;")
                appendLine("\t\t\tpackage = $packageRefId /* XCRemoteSwiftPackageReference */;")
                appendLine("\t\t\tproductName = $productName;")
                appendLine("\t\t};")
            }
            append("/* End XCSwiftPackageProductDependency section */")
        }

        // Insert after XCRemoteSwiftPackageReference section
        val insertPoint = "/* End XCRemoteSwiftPackageReference section */"
        return content.replace(
            insertPoint,
            "$insertPoint\n\n$section"
        )
    }

    private fun addBuildFileEntries(
        content: String,
        buildFileIds: List<Triple<String, String, String>>
    ): String {
        val entries = buildFileIds.joinToString("\n") { (buildFileId, productDepId, productName) ->
            "\t\t$buildFileId /* $productName in Frameworks */ = {isa = PBXBuildFile; productRef = $productDepId /* $productName */; };"
        }

        // Insert at the end of PBXBuildFile section
        return content.replace(
            "/* End PBXBuildFile section */",
            "$entries\n/* End PBXBuildFile section */"
        )
    }

    private fun modifyFrameworksBuildPhase(
        content: String,
        frameworksPhaseId: String,
        buildFileIds: List<Triple<String, String, String>>
    ): String {
        // Find the frameworks build phase and add entries to files array
        val filesRegex = Regex(
            """($frameworksPhaseId\s*/\*[^*]*\*/\s*=\s*\{[^}]*files\s*=\s*\()([^)]*)\)""",
            RegexOption.DOT_MATCHES_ALL
        )

        val newEntries = buildFileIds.joinToString(",\n\t\t\t\t") { (buildFileId, _, productName) ->
            "$buildFileId /* $productName in Frameworks */"
        }

        return filesRegex.replace(content) { matchResult ->
            val prefix = matchResult.groupValues[1]
            // Clean existing files: remove trailing commas and whitespace
            val existingFiles = matchResult.groupValues[2].trim().trimEnd(',').trim()

            if (existingFiles.isEmpty()) {
                "$prefix\n\t\t\t\t$newEntries,\n\t\t\t)"
            } else {
                "$prefix$existingFiles,\n\t\t\t\t$newEntries,\n\t\t\t)"
            }
        }
    }

    private fun addPackageReferencesToProject(
        content: String,
        projectId: String,
        packageRefIds: List<String>
    ): String {
        // Find the targets line in PBXProject section and insert packageReferences before it
        val packageRefsArray = packageRefIds.joinToString(",\n\t\t\t\t") { id ->
            "$id /* XCRemoteSwiftPackageReference */"
        }

        // Look for targets = (...) in the PBXProject section and insert packageReferences before it
        val targetsRegex = Regex(
            """(\t\t\tprojectRoot = "";)\n(\t\t\ttargets = \()""",
            RegexOption.MULTILINE
        )

        return targetsRegex.replace(content) { matchResult ->
            val projectRoot = matchResult.groupValues[1]
            val targetsStart = matchResult.groupValues[2]

            "$projectRoot\n\t\t\tpackageReferences = (\n\t\t\t\t$packageRefsArray,\n\t\t\t);\n$targetsStart"
        }
    }

    private fun addProductDependenciesToTarget(
        content: String,
        targetId: String,
        productDepIds: List<String>
    ): String {
        // Find the PBXNativeTarget section and add packageProductDependencies
        val targetRegex = Regex(
            """($targetId\s*/\*[^*]*\*/\s*=\s*\{[^}]*)(productType\s*=\s*"[^"]*";)""",
            RegexOption.DOT_MATCHES_ALL
        )

        val depsArray = productDepIds.joinToString(",\n\t\t\t\t") { id ->
            "$id /* SwiftPackageProductDependency */"
        }

        return targetRegex.replace(content) { matchResult ->
            val prefix = matchResult.groupValues[1]
            val productType = matchResult.groupValues[2]

            "${prefix}packageProductDependencies = (\n\t\t\t\t$depsArray,\n\t\t\t);\n\t\t\t$productType"
        }
    }
}
