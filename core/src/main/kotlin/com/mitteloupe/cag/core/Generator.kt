package com.mitteloupe.cag.core

import java.io.File

private const val ERROR_PREFIX = "Error: "

interface Generator {
    fun generateFeature(request: GenerateFeatureRequest): String
}

class DefaultGenerator : Generator {
    override fun generateFeature(request: GenerateFeatureRequest): String {
        val featurePackageName = request.featurePackageName?.trim()
        if (featurePackageName.isNullOrEmpty()) {
            return "${ERROR_PREFIX}Feature package name is missing."
        }

        val pathSegments = featurePackageName.split('.').filter { it.isNotBlank() }
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
            writeDomainUseCaseFile(
                featureRoot = featureRoot,
                projectNamespace = request.projectNamespace,
                featurePackageName = featurePackageName
            )?.let { return it }
            populatePresentationModule(featureRoot, featureNameLowerCase)?.let { return it }
            populateDataModule(featureRoot, featureNameLowerCase)?.let { return it }
            populateUiModule(featureRoot, featurePackageName, featureNameLowerCase)?.let { return it }
            updateProjectSettingsIfPresent(request.destinationRootDir, featureNameLowerCase)?.let { return it }
        }

        return if (allCreated) {
            "Success!"
        } else {
            "${ERROR_PREFIX}Failed to create directories for package '$featurePackageName'."
        }
    }

    private fun updateVersionCatalogIfPresent(projectRootDir: File): String? {
        val catalogFile = File(projectRootDir, "gradle/libs.versions.toml")
        if (!catalogFile.exists()) {
            return null
        }

        val originalText =
            runCatching { catalogFile.readText() }
                .getOrElse { return "${ERROR_PREFIX}Failed to read version catalog: ${it.message}" }

        val updatedText = updateCatalogText(originalText)
        if (updatedText == originalText) {
            return null
        }

        return runCatching { catalogFile.writeText(updatedText) }
            .exceptionOrNull()
            ?.let { "${ERROR_PREFIX}Failed to update version catalog: ${it.message}" }
    }

    private fun updateCatalogText(catalog: String): String =
        VersionCatalogUpdater(catalog).apply {
            ensureSectionEntries(
                header = "versions",
                requirements = versionCatalogVersionRequirements(),
                insertPositionIfMissing = InsertPosition.START
            )

            ensureSectionEntries(
                header = "plugins",
                requirements = versionCatalogPluginRequirements(),
                insertPositionIfMissing = InsertPosition.END
            )
        }.asString()

    private fun versionCatalogVersionRequirements(): List<SectionRequirement> =
        listOf(
            SectionRequirement("^\\s*compileSdk\\s*=".toRegex(), "compileSdk = \"35\""),
            SectionRequirement("^\\s*minSdk\\s*=".toRegex(), "minSdk = \"24\""),
            SectionRequirement("^\\s*androidGradlePlugin\\s*=".toRegex(), "androidGradlePlugin = \"8.7.3\"")
        )

    private fun versionCatalogPluginRequirements(): List<SectionRequirement> =
        listOf(
            SectionRequirement(
                "^\\s*kotlin-jvm\\s*=".toRegex(),
                "kotlin-jvm = { id = \"org.jetbrains.kotlin.jvm\", version.ref = \"kotlin\" }"
            ),
            SectionRequirement(
                "^\\s*kotlin-android\\s*=".toRegex(),
                "kotlin-android = { id = \"org.jetbrains.kotlin.android\", version.ref = \"kotlin\" }"
            ),
            SectionRequirement(
                "^\\s*android-library\\s*=".toRegex(),
                "android-library = { id = \"com.android.library\", version.ref = \"androidGradlePlugin\" }"
            )
        )

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

    private fun updateProjectSettingsIfPresent(
        startDirectory: File,
        featureNameLowerCase: String
    ): String? {
        val projectRoot =
            DirectoryFinder()
                .findDirectory(startDirectory) { currentDirectory ->
                    File(currentDirectory, "settings.gradle.kts").exists() ||
                        File(currentDirectory, "settings.gradle").exists()
                }
                ?: return null

        val ktsFile = File(projectRoot, "settings.gradle.kts")
        val groovyFile = File(projectRoot, "settings.gradle")

        val settingsFile =
            when {
                ktsFile.exists() -> ktsFile
                groovyFile.exists() -> groovyFile
                else -> null
            }
                ?: return null

        return updateSettingsFile(settingsFile, featureNameLowerCase)
    }

    private fun updateSettingsFile(
        settingsFile: File,
        featureNameLowerCase: String
    ): String? {
        val original =
            runCatching { settingsFile.readText() }
                .getOrElse {
                    return "${ERROR_PREFIX}Failed to read ${settingsFile.name}: ${it.message}"
                }

        val modulePaths =
            listOf("ui", "presentation", "domain", "data")
                .map { layer -> ":features:$featureNameLowerCase:$layer" }

        val missingIncludes =
            modulePaths.filterNot { path ->
                original.contains("include(\"$path\")") || original.contains("include '$path'")
            }

        if (missingIncludes.isEmpty()) return null

        val toAppend =
            buildString {
                if (!original.endsWith("\n")) {
                    append('\n')
                }
                missingIncludes.forEach { path ->
                    append("include(\"$path\")\n")
                }
            }

        val updated =
            original + toAppend

        return runCatching { settingsFile.writeText(updated) }
            .exceptionOrNull()
            ?.let {
                "${ERROR_PREFIX}Failed to update ${settingsFile.name}: ${it.message}"
            }
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

    private fun writeDomainUseCaseFile(
        featureRoot: File,
        projectNamespace: String,
        featurePackageName: String
    ): String? {
        val domainSourceRoot = File(featureRoot, "domain/src/main/java")
        val packageSegments =
            featurePackageName.split('.')
                .filter { it.isNotBlank() }
        val basePackageDirectory = buildPackageDirectory(domainSourceRoot, packageSegments)
        val useCaseDirectory = File(File(basePackageDirectory, "domain"), "usecase")
        if (!useCaseDirectory.exists()) {
            val created = runCatching { useCaseDirectory.mkdirs() }.getOrElse { false }
            if (!created) {
                return "${ERROR_PREFIX}Failed to create domain use case directory."
            }
        }

        val useCaseFile = File(useCaseDirectory, "PerformActionUseCase.kt")
        if (!useCaseFile.exists()) {
            val content =
                buildDomainUseCaseKotlinFile(
                    projectNamespace = projectNamespace,
                    featurePackageName = featurePackageName
                )
            runCatching { useCaseFile.writeText(content) }
                .onFailure {
                    return "${ERROR_PREFIX}Failed to create domain use case file: ${it.message}"
                }
        }
        return null
    }

    private fun buildDomainUseCaseKotlinFile(
        projectNamespace: String,
        featurePackageName: String
    ): String =
        """package $featurePackageName.domain.usecase

import ${projectNamespace}architecture.domain.usecase.UseCase
import $featurePackageName.domain.repository.PerformExampleRepository

class PerformActionUseCase(
    private val performExampleRepository: PerformExampleRepository
) : UseCase<Unit, Unit>(
    coroutineContextProvider
) {
    override fun execute(input: Unit, onResult: (Unit) -> Unit) {
        onResult(Unit)
    }
}
"""
}
