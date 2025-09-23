package com.mitteloupe.cag.core.content

import com.mitteloupe.cag.core.generation.versioncatalog.SectionEntryRequirement.LibraryRequirement
import com.mitteloupe.cag.core.generation.versioncatalog.SectionEntryRequirement.PluginRequirement
import com.mitteloupe.cag.core.generation.versioncatalog.VersionCatalogReader
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class FeatureGradleScriptBuildersTest {
    private lateinit var catalog: VersionCatalogReader

    @Before
    fun setUp() {
        catalog = FakeVersionCatalogReader()
    }

    @Test
    fun `buildDomainGradleScript placeholder`() {
        // Given
        val expected = """plugins {
    id("project-java-library")
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.detekt)
}

ktlint {
    version.set("1.7.1")
    android.set(true)
}

detekt {
    config.setFrom("${'$'}projectDir/../../../detekt.yml")
}

dependencies {
    implementation(projects.architecture.domain)
}
"""

        // When
        val actualContent = buildDomainGradleScript(catalog)

        // Then
        assertEquals(expected, actualContent)
    }

    @Test
    fun `buildPresentationGradleScript placeholder`() {
        val expected = """plugins {
    id("project-java-library")
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.detekt)
}

ktlint {
    version.set("1.7.1")
    android.set(true)
}

detekt {
    config.setFrom("${'$'}projectDir/../../../detekt.yml")
}

dependencies {
    implementation(projects.features.samplefeature.domain)
    implementation(projects.architecture.presentation)
    implementation(projects.architecture.domain)
}
"""

        // When
        val actualContent = buildPresentationGradleScript(featureNameLowerCase = "samplefeature", catalog = catalog)

        // Then
        assertEquals(expected, actualContent)
    }

    @Test
    fun `buildDataGradleScript placeholder`() {
        // Given
        val expected = """plugins {
    id("project-java-library")
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.detekt)
}

ktlint {
    version.set("1.7.1")
    android.set(true)
}

detekt {
    config.setFrom("${'$'}projectDir/../../../detekt.yml")
}

dependencies {
    implementation(projects.features.samplefeature.domain)
    implementation(projects.architecture.domain)

    implementation(projects.datasource.source)
}
"""

        // When
        val actualContent = buildDataGradleScript(featureNameLowerCase = "samplefeature", catalog = catalog)

        // Then
        assertEquals(expected, actualContent)
    }

    @Test
    fun `buildUiGradleScript placeholder`() {
        // Given
        val expected = """plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)

    alias(libs.plugins.ktlint)
    alias(libs.plugins.detekt)
}

android {
    namespace = "com.example.samplefeature.ui"
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

    buildFeatures {
        compose = true
    }
}

ktlint {
    version.set("1.7.1")
    android.set(true)
}

detekt {
    config.setFrom("${'$'}projectDir/../../../detekt.yml")
}

dependencies {
    implementation(projects.features.samplefeature.presentation)
    implementation(projects.architecture.ui)
    implementation(projects.architecture.presentation)

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling)
    implementation(libs.compose.navigation)
    implementation(libs.compose.ui.tooling.preview)
}
"""

        // When
        val actualContent =
            buildUiGradleScript(
                featurePackageName = "com.example.samplefeature",
                featureNameLowerCase = "samplefeature",
                enableCompose = true,
                catalog = catalog
            )

        // Then
        assertEquals(expected, actualContent)
    }

    private class FakeVersionCatalogReader : VersionCatalogReader {
        override fun getResolvedPluginAliasFor(requirement: PluginRequirement): String = requirement.key

        override fun isPluginAvailable(requirement: PluginRequirement): Boolean = true

        override fun getResolvedLibraryAliasForModule(requirement: LibraryRequirement): String = requirement.key
    }
}
