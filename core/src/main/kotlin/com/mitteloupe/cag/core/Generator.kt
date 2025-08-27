package com.mitteloupe.cag.core

import java.io.File

interface Generator {
    fun generateFeature(request: GenerateFeatureRequest): String
}

private const val ERROR_PREFIX = "Error: "

class DefaultGenerator : Generator {
    override fun generateFeature(request: GenerateFeatureRequest): String {
        val packageName = request.featurePackageName?.trim()
        if (packageName.isNullOrEmpty()) {
            return "${ERROR_PREFIX}Feature package name is missing."
        }

        val pathSegments = packageName.split('.').filter { it.isNotBlank() }
        if (pathSegments.isEmpty()) {
            return "${ERROR_PREFIX}Feature package name is invalid."
        }

        val featureNameLowerCase = request.featureName.lowercase()
        updateVersionCatalogIfPresent(request.destinationRootDir)?.let { return it }
        val featureRoot = File(request.destinationRootDir, "features/$featureNameLowerCase")

        if (featureRoot.exists()) {
            return ERROR_PREFIX +
                if (featureRoot.isDirectory) {
                    "The feature directory already exists."
                } else {
                    "A file with the feature name exists where the feature directory should be created."
                }
        }

        val layers = listOf("ui", "presentation", "domain", "data")

        val allCreated =
            layers.map { layerName ->
                val layerSourceRoot = File(featureRoot, "$layerName/src/main/java")
                val destinationDirectory = buildPackageDirectory(layerSourceRoot, pathSegments)
                if (destinationDirectory.exists()) {
                    destinationDirectory.isDirectory
                } else {
                    destinationDirectory.mkdirs()
                }
            }.all { it }

        if (allCreated) {
            populateDomainModule(featureRoot)?.let { return it }
            populatePresentationModule(featureRoot, featureNameLowerCase)?.let { return it }
            populateDataModule(featureRoot, featureNameLowerCase)?.let { return it }
            populateUiModule(featureRoot, packageName, featureNameLowerCase)?.let { return it }
        }

        return if (allCreated) {
            "Success!"
        } else {
            "${ERROR_PREFIX}Failed to create directories for package '$packageName'."
        }
    }

    private fun updateVersionCatalogIfPresent(projectRootDir: File): String? {
        val catalogFile = File(projectRootDir, "gradle/libs.versions.toml")
        if (!catalogFile.exists()) return null

        val originalText =
            runCatching { catalogFile.readText() }
                .getOrElse { return "${ERROR_PREFIX}Failed to read version catalog: ${it.message}" }

        val updatedText = updateCatalogText(originalText)
        if (updatedText == originalText) return null

        return runCatching { catalogFile.writeText(updatedText) }
            .exceptionOrNull()
            ?.let { "${ERROR_PREFIX}Failed to update version catalog: ${it.message}" }
    }

    private fun updateCatalogText(catalog: String): String {
        val lines = catalog.split('\n').toMutableList()

        fun findSectionBounds(header: String): Pair<Int, Int>? {
            val startIndex = lines.indexOfFirst { it.trim() == "[$header]" }
            if (startIndex == -1) {
                return null
            }
            var endIndexExclusive = lines.size
            for (i in startIndex + 1 until lines.size) {
                val trimmed = lines[i].trim()
                if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
                    endIndexExclusive = i
                    break
                }
            }
            return startIndex to endIndexExclusive
        }

        fun hasKeyInRange(
            keyRegex: Regex,
            range: IntRange
        ): Boolean = range.any { idx -> keyRegex.containsMatchIn(lines[idx]) }

        fun insertAt(
            index: Int,
            newLines: List<String>
        ) {
            lines.addAll(index, newLines)
        }

        val versionsBounds = findSectionBounds("versions")
        val needAddVersionsSection = versionsBounds == null
        val versionsRange: IntRange =
            if (versionsBounds == null) {
                IntRange(0, -1)
            } else {
                val (start, end) = versionsBounds
                (start + 1) until end
            }

        val versionsToAdd =
            buildList {
                if (needAddVersionsSection) {
                    add("[versions]")
                }
                val hasCompileSdk =
                    !needAddVersionsSection && hasKeyInRange(Regex("^\\s*compileSdk\\s*="), versionsRange)
                if (!hasCompileSdk) {
                    add("compileSdk = \"35\"")
                }
                val hasMinSdk =
                    !needAddVersionsSection && hasKeyInRange(Regex("^\\s*minSdk\\s*="), versionsRange)
                if (!hasMinSdk) {
                    add("minSdk = \"24\"")
                }
                val hasAgpVersion =
                    !needAddVersionsSection && hasKeyInRange(Regex("^\\s*androidGradlePlugin\\s*="), versionsRange)
                if (!hasAgpVersion) {
                    add("androidGradlePlugin = \"8.7.3\"")
                }
            }

        if (versionsToAdd.isNotEmpty()) {
            if (needAddVersionsSection) {
                insertAt(0, versionsToAdd + "")
            } else {
                val (_, end) = versionsBounds
                insertAt(end, listOf("") + versionsToAdd)
            }
        }

        val pluginsBounds = findSectionBounds("plugins")
        val needAddPluginsSection = pluginsBounds == null
        val pluginsRange: IntRange =
            if (pluginsBounds == null) {
                IntRange(0, -1)
            } else {
                val (start, end) = pluginsBounds
                (start + 1) until end
            }

        val pluginsToAdd =
            buildList {
                if (needAddPluginsSection) {
                    add("[plugins]")
                }
                val hasKotlinJvm = !needAddPluginsSection && hasKeyInRange(Regex("^\\s*kotlin-jvm\\s*="), pluginsRange)
                if (!hasKotlinJvm) {
                    add("kotlin-jvm = { id = \"org.jetbrains.kotlin.jvm\", version.ref = \"kotlin\" }")
                }
                val hasKotlinAndroid =
                    !needAddPluginsSection && hasKeyInRange(Regex("^\\s*kotlin-android\\s*="), pluginsRange)
                if (!hasKotlinAndroid) {
                    add("kotlin-android = { id = \"org.jetbrains.kotlin.android\", version.ref = \"kotlin\" }")
                }
                val hasAndroidLibrary =
                    !needAddPluginsSection && hasKeyInRange(Regex("^\\s*android-library\\s*="), pluginsRange)
                if (!hasAndroidLibrary) {
                    add("android-library = { id = \"com.android.library\", version.ref = \"androidGradlePlugin\" }")
                }
            }

        if (pluginsToAdd.isNotEmpty()) {
            if (needAddPluginsSection) {
                if (lines.isNotEmpty() && lines.last().isNotEmpty()) lines.add("")
                lines.addAll(pluginsToAdd)
            } else {
                val (_, end) = pluginsBounds
                insertAt(end, listOf("") + pluginsToAdd)
            }
        }

        return lines.joinToString("\n")
    }

    private fun populateDomainModule(featureRoot: File): String? =
        writeGradleFileIfMissing(
            featureRoot = featureRoot,
            layer = "domain",
            content = buildDomainGradleScript()
        )

    private fun populateDataModule(
        featureRoot: File,
        featureNameLowerCase: String
    ): String? =
        writeGradleFileIfMissing(
            featureRoot = featureRoot,
            layer = "data",
            content = buildDataGradleScript(featureNameLowerCase)
        )

    private fun populatePresentationModule(
        featureRoot: File,
        featureNameLowerCase: String
    ): String? =
        writeGradleFileIfMissing(
            featureRoot = featureRoot,
            layer = "presentation",
            content = buildPresentationGradleScript(featureNameLowerCase)
        )

    private fun populateUiModule(
        featureRoot: File,
        featurePackageName: String,
        featureNameLowerCase: String
    ): String? =
        writeGradleFileIfMissing(
            featureRoot = featureRoot,
            layer = "ui",
            content = buildUiGradleScript(featurePackageName, featureNameLowerCase)
        )

    private fun buildPackageDirectory(
        root: File,
        packageSegments: List<String>
    ): File = packageSegments.fold(root) { parent, segment -> File(parent, segment) }

    private fun writeGradleFileIfMissing(
        featureRoot: File,
        layer: String,
        content: String
    ): String? {
        val moduleDirectory = File(featureRoot, layer)
        val buildGradleFile = File(moduleDirectory, "build.gradle.kts")
        if (!buildGradleFile.exists()) {
            runCatching { buildGradleFile.writeText(content) }
                .onFailure { return "${ERROR_PREFIX}Failed to create $layer/build.gradle.kts: ${it.message}" }
        }
        return null
    }

    private fun buildDataGradleScript(featureNameLowerCase: String): String =
        """plugins {
    id("project-java-library")
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(projects.features.$featureNameLowerCase.domain)
    implementation(projects.architecture.domain)

    implementation(projects.datasource.architecture)
    implementation(projects.datasource.source)
}
"""

    private fun buildPresentationGradleScript(featureNameLowerCase: String): String =
        """plugins {
    id("project-java-library")
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(projects.features.$featureNameLowerCase.domain)
    implementation(projects.architecture.presentation)
    implementation(projects.architecture.domain)
}
"""

    private fun buildDomainGradleScript(): String =
        """plugins {
    id("project-java-library")
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(projects.architecture.domain)
}
"""

    private fun buildUiGradleScript(
        featurePackageName: String,
        featureNameLowerCase: String
    ): String =
        """plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "$featurePackageName.ui"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(projects.features.$featureNameLowerCase.presentation)
    implementation(projects.architecture.ui)
    implementation(projects.architecture.presentation)
}
"""
}
