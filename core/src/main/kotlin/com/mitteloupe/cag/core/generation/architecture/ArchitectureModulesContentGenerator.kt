package com.mitteloupe.cag.core.generation.architecture

import com.mitteloupe.cag.core.GenerationException
import com.mitteloupe.cag.core.content.architecture.buildArchitectureDomainGradleScript
import com.mitteloupe.cag.core.content.architecture.buildArchitectureInstrumentationTestGradleScript
import com.mitteloupe.cag.core.content.architecture.buildArchitecturePresentationGradleScript
import com.mitteloupe.cag.core.content.architecture.buildArchitecturePresentationTestGradleScript
import com.mitteloupe.cag.core.content.architecture.buildArchitectureUiGradleScript
import com.mitteloupe.cag.core.generation.GradleFileCreator
import com.mitteloupe.cag.core.generation.versioncatalog.DependencyConfiguration
import com.mitteloupe.cag.core.generation.versioncatalog.LibraryConstants
import com.mitteloupe.cag.core.generation.versioncatalog.PluginConstants
import com.mitteloupe.cag.core.generation.versioncatalog.VersionCatalogConstants
import com.mitteloupe.cag.core.generation.versioncatalog.VersionCatalogUpdater
import com.mitteloupe.cag.core.kotlinpackage.buildPackageDirectory
import com.mitteloupe.cag.core.kotlinpackage.toSegments
import java.io.File

class ArchitectureModulesContentGenerator(
    private val domainModuleCreator: DomainModuleCreator = DomainModuleCreator(),
    private val instrumentationTestModuleCreator: InstrumentationTestModuleCreator = InstrumentationTestModuleCreator(),
    private val presentationModuleCreator: PresentationModuleCreator = PresentationModuleCreator(),
    private val presentationTestModuleCreator: PresentationTestModuleCreator = PresentationTestModuleCreator(),
    private val uiModuleCreator: UiModuleCreator = UiModuleCreator(),
    private val gradleFileCreator: GradleFileCreator = GradleFileCreator()
) {
    fun generate(
        architectureRoot: File,
        architecturePackageName: String,
        enableCompose: Boolean,
        enableKtlint: Boolean = false,
        enableDetekt: Boolean = false
    ) {
        val packageSegments = architecturePackageName.toSegments()
        if (packageSegments.isEmpty()) {
            throw GenerationException("Architecture package name is invalid.")
        }

        val layers = listOf("domain", "presentation", "ui", "presentation-test", "instrumentation-test")
        val allCreated =
            layers.all { layerName ->
                val layerSourceRoot = File(architectureRoot, "$layerName/src/main/java")
                val destinationDirectory = buildPackageDirectory(layerSourceRoot, packageSegments)
                if (destinationDirectory.exists()) {
                    destinationDirectory.isDirectory
                } else {
                    destinationDirectory.mkdirs()
                }
            }

        if (!allCreated) {
            throw GenerationException("Failed to create directories for architecture package '$architecturePackageName'.")
        }

        val catalogUpdater = VersionCatalogUpdater()
        val versions =
            VersionCatalogConstants.BASIC_VERSIONS + VersionCatalogConstants.ANDROID_VERSIONS +
                if (enableCompose) {
                    VersionCatalogConstants.COMPOSE_VERSIONS
                } else {
                    emptyList()
                } +
                if (enableKtlint) {
                    VersionCatalogConstants.KTLINT_VERSIONS
                } else {
                    emptyList()
                } +
                if (enableDetekt) {
                    VersionCatalogConstants.DETEKT_VERSIONS
                } else {
                    emptyList()
                }
        val libraries =
            LibraryConstants.CORE_ANDROID_LIBRARIES +
                if (enableCompose) {
                    LibraryConstants.COMPOSE_LIBRARIES + LibraryConstants.COMPOSE_TESTING_LIBRARIES
                } else {
                    LibraryConstants.VIEW_LIBRARIES
                } +
                LibraryConstants.TESTING_LIBRARIES +
                LibraryConstants.NETWORK_LIBRARIES
        val plugins =
            PluginConstants.KOTLIN_PLUGINS + PluginConstants.ANDROID_PLUGINS +
                if (enableCompose) {
                    PluginConstants.COMPOSE_PLUGINS
                } else {
                    emptyList()
                } +
                if (enableKtlint) {
                    PluginConstants.CODE_QUALITY_PLUGINS.filter { it.id == "org.jlleitschuh.gradle.ktlint" }
                } else {
                    emptyList()
                } +
                if (enableDetekt) {
                    PluginConstants.CODE_QUALITY_PLUGINS.filter {
                        it.id == "io.gitlab.arturbosch.detekt"
                    }
                } else {
                    emptyList()
                }
        val dependencyConfiguration =
            DependencyConfiguration(
                versions = versions,
                libraries = libraries,
                plugins = plugins
            )
        catalogUpdater.updateVersionCatalogIfPresent(
            projectRootDir = architectureRoot.parentFile,
            dependencyConfiguration = dependencyConfiguration
        )

        gradleFileCreator.writeGradleFileIfMissing(
            featureRoot = architectureRoot,
            layer = "domain",
            content = buildArchitectureDomainGradleScript(catalogUpdater)
        )
        gradleFileCreator.writeGradleFileIfMissing(
            featureRoot = architectureRoot,
            layer = "presentation",
            content = buildArchitecturePresentationGradleScript(catalogUpdater)
        )
        gradleFileCreator.writeGradleFileIfMissing(
            featureRoot = architectureRoot,
            layer = "ui",
            content = buildArchitectureUiGradleScript(architecturePackageName, catalogUpdater)
        )
        gradleFileCreator.writeGradleFileIfMissing(
            featureRoot = architectureRoot,
            layer = "presentation-test",
            content = buildArchitecturePresentationTestGradleScript(catalogUpdater)
        )
        gradleFileCreator.writeGradleFileIfMissing(
            featureRoot = architectureRoot,
            layer = "instrumentation-test",
            content = buildArchitectureInstrumentationTestGradleScript(architecturePackageName, catalogUpdater)
        )

        val domainRoot = File(architectureRoot, "domain")
        domainModuleCreator.generateDomainContent(
            architectureRoot = domainRoot,
            moduleNamespace = architecturePackageName,
            architecturePackageNameSegments = packageSegments + "domain"
        )

        val presentationRoot = File(architectureRoot, "presentation")
        presentationModuleCreator.generatePresentationContent(
            architectureRoot = presentationRoot,
            architecturePackageName = architecturePackageName,
            architecturePackageNameSegments = packageSegments + "presentation"
        )

        val presentationTestRoot = File(architectureRoot, "presentation-test")
        presentationTestModuleCreator.generatePresentationTestContent(
            presentationTestRoot,
            architecturePackageName,
            packageSegments + "presentation"
        )
        val uiRoot = File(architectureRoot, "ui")
        uiModuleCreator.generateUiContent(uiRoot, architecturePackageName, packageSegments + "ui")
        val instrumentationTestRoot = File(architectureRoot, "instrumentation-test")
        instrumentationTestModuleCreator.generateInstrumentationTestContent(
            instrumentationTestRoot,
            architecturePackageName.substringBeforeLast('.'),
            architecturePackageName.substringBeforeLast('.').toSegments() + "test"
        )
    }
}
