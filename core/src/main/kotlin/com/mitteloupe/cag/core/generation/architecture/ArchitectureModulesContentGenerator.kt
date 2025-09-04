package com.mitteloupe.cag.core.generation.architecture

import com.mitteloupe.cag.core.GenerationException
import com.mitteloupe.cag.core.content.architecture.buildArchitectureDomainGradleScript
import com.mitteloupe.cag.core.content.architecture.buildArchitectureInstrumentationTestGradleScript
import com.mitteloupe.cag.core.content.architecture.buildArchitecturePresentationGradleScript
import com.mitteloupe.cag.core.content.architecture.buildArchitecturePresentationTestGradleScript
import com.mitteloupe.cag.core.content.architecture.buildArchitectureUiGradleScript
import com.mitteloupe.cag.core.generation.GradleFileCreator
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
        catalogUpdater.updateVersionCatalogIfPresent(
            projectRootDir = architectureRoot.parentFile,
            enableCompose = enableCompose,
            enableKtlint = enableKtlint,
            enableDetekt = enableDetekt
        )

        gradleFileCreator.writeGradleFileIfMissing(
            featureRoot = architectureRoot,
            layer = "domain",
            content = buildArchitectureDomainGradleScript(catalogUpdater, enableKtlint, enableDetekt)
        )
        gradleFileCreator.writeGradleFileIfMissing(
            featureRoot = architectureRoot,
            layer = "presentation",
            content = buildArchitecturePresentationGradleScript(catalogUpdater, enableKtlint, enableDetekt)
        )
        gradleFileCreator.writeGradleFileIfMissing(
            featureRoot = architectureRoot,
            layer = "ui",
            content = buildArchitectureUiGradleScript(architecturePackageName, catalogUpdater, enableKtlint, enableDetekt)
        )
        gradleFileCreator.writeGradleFileIfMissing(
            featureRoot = architectureRoot,
            layer = "presentation-test",
            content = buildArchitecturePresentationTestGradleScript(catalogUpdater, enableKtlint, enableDetekt)
        )
        gradleFileCreator.writeGradleFileIfMissing(
            featureRoot = architectureRoot,
            layer = "instrumentation-test",
            content = buildArchitectureInstrumentationTestGradleScript(architecturePackageName, catalogUpdater, enableKtlint, enableDetekt)
        )

        val domainRoot = File(architectureRoot, "domain")
        domainModuleCreator.generateDomainContent(domainRoot, architecturePackageName, packageSegments + "domain")
        val presentationRoot = File(architectureRoot, "presentation")
        presentationModuleCreator.generatePresentationContent(presentationRoot, architecturePackageName, packageSegments + "presentation")
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
