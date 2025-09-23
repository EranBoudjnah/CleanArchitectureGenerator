package com.mitteloupe.cag.core.generation.versioncatalog

import com.mitteloupe.cag.core.generation.versioncatalog.SectionEntryRequirement.LibraryRequirement
import com.mitteloupe.cag.core.generation.versioncatalog.SectionEntryRequirement.PluginRequirement
import com.mitteloupe.cag.core.generation.versioncatalog.SectionEntryRequirement.VersionRequirement

data class DependencyConfiguration(
    val versions: List<VersionRequirement> = emptyList(),
    val libraries: List<LibraryRequirement> = emptyList(),
    val plugins: List<PluginRequirement> = emptyList()
)

object VersionCatalogConstants {
    val KOTLIN_VERSION = VersionRequirement(key = "kotlin", version = "2.2.10")
    val COMPILE_SDK_VERSION = VersionRequirement(key = "compileSdk", version = "35")
    val MIN_SDK_VERSION = VersionRequirement(key = "minSdk", version = "24")
    val TARGET_SDK_VERSION = VersionRequirement(key = "targetSdk", version = "35")

    val ANDROID_GRADLE_PLUGIN_VERSION = VersionRequirement(key = "androidGradlePlugin", version = "8.12.2")

    val COMPOSE_BOM_VERSION = VersionRequirement(key = "composeBom", version = "2025.08.01")
    val COMPOSE_NAVIGATION_VERSION = VersionRequirement(key = "composeNavigation", version = "2.9.3")
    val COMPOSE_COMPILER_VERSION = VersionRequirement(key = "composeCompiler", version = "1.5.8")

    val JUNIT4_VERSION = VersionRequirement(key = "junit4", version = "4.13.2")
    val KSP_VERSION = VersionRequirement(key = "ksp", version = "2.2.10-2.0.2")

    val KTLINT_VERSION = VersionRequirement(key = "ktlint", version = "13.1.0")
    val DETEKT_VERSION = VersionRequirement(key = "detekt", version = "1.23.6")

    val KOTLIN_VERSIONS =
        listOf(
            KOTLIN_VERSION,
            KSP_VERSION
        )

    val ANDROID_VERSIONS =
        listOf(
            COMPILE_SDK_VERSION,
            MIN_SDK_VERSION,
            TARGET_SDK_VERSION,
            ANDROID_GRADLE_PLUGIN_VERSION
        )

    val COMPOSE_VERSIONS =
        listOf(
            COMPOSE_BOM_VERSION,
            COMPOSE_NAVIGATION_VERSION,
            COMPOSE_COMPILER_VERSION
        )

    val TESTING_VERSIONS = listOf(JUNIT4_VERSION)

    val KTLINT_VERSIONS = listOf(KTLINT_VERSION)

    val DETEKT_VERSIONS = listOf(DETEKT_VERSION)
}

object LibraryConstants {
    val ANDROIDX_CORE_KTX =
        LibraryRequirement(
            key = "androidx-core-ktx",
            module = "androidx.core:core-ktx",
            versionLiteral = "1.12.0"
        )

    val ANDROIDX_LIFECYCLE_RUNTIME_KTX =
        LibraryRequirement(
            key = "androidx-lifecycle-runtime-ktx",
            module = "androidx.lifecycle:lifecycle-runtime-ktx",
            versionLiteral = "2.7.0"
        )

    val KOTLINX_COROUTINES_CORE =
        LibraryRequirement(
            key = "kotlinx-coroutines-core",
            module = "org.jetbrains.kotlinx:kotlinx-coroutines-core",
            versionLiteral = "1.7.3"
        )

    val COMPOSE_BOM =
        LibraryRequirement(
            key = "compose-bom",
            module = "androidx.compose:compose-bom",
            versionRefKey = "composeBom"
        )

    val COMPOSE_UI =
        LibraryRequirement(
            key = "compose-ui",
            module = "androidx.compose.ui:ui"
        )

    val COMPOSE_UI_GRAPHICS =
        LibraryRequirement(
            key = "compose-ui-graphics",
            module = "androidx.compose.ui:ui-graphics"
        )

    val COMPOSE_UI_TOOLING_PREVIEW =
        LibraryRequirement(
            key = "compose-ui-tooling-preview",
            module = "androidx.compose.ui:ui-tooling-preview"
        )

    val COMPOSE_MATERIAL3 =
        LibraryRequirement(
            key = "compose-material3",
            module = "androidx.compose.material3:material3"
        )

    val COMPOSE_NAVIGATION =
        LibraryRequirement(
            key = "compose-navigation",
            module = "androidx.navigation:navigation-compose",
            versionRefKey = "composeNavigation"
        )

    val TEST_JUNIT =
        LibraryRequirement(
            key = "test-junit",
            module = "junit:junit",
            versionRefKey = "junit4"
        )

    val TEST_ANDROIDX_JUNIT =
        LibraryRequirement(
            key = "test-androidx-junit",
            module = "androidx.test.ext:junit",
            versionLiteral = "1.1.5"
        )

    val TEST_ANDROIDX_ESPRESSO_CORE =
        LibraryRequirement(
            key = "test-androidx-espresso-core",
            module = "androidx.test.espresso:espresso-core",
            versionLiteral = "3.5.1"
        )

    val TEST_ANDROID_HILT =
        LibraryRequirement(
            key = "test-android-hilt",
            module = "com.google.dagger:hilt-android-testing",
            versionLiteral = "2.48"
        )

    val TEST_ANDROID_UI_AUTOMATOR =
        LibraryRequirement(
            key = "test-android-uiautomator",
            module = "androidx.test.uiautomator:uiautomator",
            versionLiteral = "2.2.0"
        )

    val TEST_ANDROID_MOCKWEBSERVER =
        LibraryRequirement(
            key = "test-android-mockwebserver",
            module = "com.squareup.okhttp3:mockwebserver",
            versionLiteral = "4.12.0"
        )

    val TEST_ANDROIDX_RULES =
        LibraryRequirement(
            key = "test-androidx-rules",
            module = "androidx.test:rules",
            versionLiteral = "1.5.0"
        )

    val TEST_COMPOSE_UI_JUNIT4 =
        LibraryRequirement(
            key = "test-compose-ui-junit4",
            module = "androidx.compose.ui:ui-test-junit4"
        )

    val MATERIAL =
        LibraryRequirement(
            key = "material",
            module = "com.google.android.material:material",
            versionLiteral = "1.11.0"
        )

    val OKHTTP3 =
        LibraryRequirement(
            key = "okhttp3",
            module = "com.squareup.okhttp3:okhttp",
            versionLiteral = "4.12.0"
        )

    val ANDROIDX_APPCOMPAT =
        LibraryRequirement(
            key = "androidx-appcompat",
            module = "androidx.appcompat:appcompat",
            versionLiteral = "1.6.1"
        )

    val ANDROIDX_RECYCLERVIEW =
        LibraryRequirement(
            key = "androidx-recyclerview",
            module = "androidx.recyclerview:recyclerview",
            versionLiteral = "1.3.2"
        )

    val ANDROIDX_FRAGMENT_KTX =
        LibraryRequirement(
            key = "androidx-fragment-ktx",
            module = "androidx.fragment:fragment-ktx",
            versionLiteral = "1.6.2"
        )

    val ANDROIDX_NAVIGATION_FRAGMENT_KTX =
        LibraryRequirement(
            key = "androidx-navigation-fragment-ktx",
            module = "androidx.navigation:navigation-fragment-ktx",
            versionLiteral = "2.7.6"
        )

    val ANDROIDX_UI_TOOLING =
        LibraryRequirement(
            key = "compose-ui-tooling",
            module = "androidx.compose.ui:ui-tooling"
        )

    val ANDROIDX_UI_TEST_MANIFEST =
        LibraryRequirement(
            key = "compose-ui-test-manifest",
            module = "androidx.compose.ui:ui-test-manifest"
        )

    val ANDROIDX_ACTIVITY_COMPOSE =
        LibraryRequirement(
            key = "androidx-activity-compose",
            module = "androidx.activity:activity-compose",
            versionLiteral = "1.8.2"
        )

    val ANDROIDX_CONSTRAINTLAYOUT =
        LibraryRequirement(
            key = "androidx-constraintlayout",
            module = "androidx.constraintlayout:constraintlayout",
            versionLiteral = "2.1.4"
        )

    val KTOR_CLIENT_CORE =
        LibraryRequirement(
            key = "ktor-client-core",
            module = "io.ktor:ktor-client-core",
            versionLiteral = "3.0.3"
        )

    val KTOR_CLIENT_OKHTTP =
        LibraryRequirement(
            key = "ktor-client-okhttp",
            module = "io.ktor:ktor-client-okhttp",
            versionLiteral = "3.0.3"
        )

    val RETROFIT =
        LibraryRequirement(
            key = "retrofit",
            module = "com.squareup.retrofit2:retrofit",
            versionLiteral = "2.11.0"
        )

    val OKHTTP3_LOGGING_INTERCEPTOR =
        LibraryRequirement(
            key = "okhttp3-logging-interceptor",
            module = "com.squareup.okhttp3:logging-interceptor",
            versionLiteral = "4.12.0"
        )

    val CORE_ANDROID_LIBRARIES =
        listOf(
            ANDROIDX_CORE_KTX,
            ANDROIDX_LIFECYCLE_RUNTIME_KTX,
            ANDROIDX_APPCOMPAT,
            KOTLINX_COROUTINES_CORE,
            MATERIAL,
            OKHTTP3
        )

    val VIEW_LIBRARIES =
        listOf(
            ANDROIDX_RECYCLERVIEW,
            ANDROIDX_FRAGMENT_KTX,
            ANDROIDX_NAVIGATION_FRAGMENT_KTX,
            ANDROIDX_CONSTRAINTLAYOUT
        )

    val COMPOSE_LIBRARIES =
        listOf(
            COMPOSE_BOM,
            COMPOSE_UI,
            COMPOSE_UI_GRAPHICS,
            COMPOSE_UI_TOOLING_PREVIEW,
            COMPOSE_MATERIAL3,
            COMPOSE_NAVIGATION,
            ANDROIDX_UI_TOOLING,
            ANDROIDX_UI_TEST_MANIFEST,
            ANDROIDX_ACTIVITY_COMPOSE
        )

    val TESTING_LIBRARIES =
        listOf(
            TEST_JUNIT,
            TEST_ANDROIDX_JUNIT,
            TEST_ANDROIDX_ESPRESSO_CORE,
            TEST_ANDROID_HILT,
            TEST_ANDROID_UI_AUTOMATOR,
            TEST_ANDROID_MOCKWEBSERVER,
            TEST_ANDROIDX_RULES
        )

    val COMPOSE_TESTING_LIBRARIES =
        listOf(
            TEST_COMPOSE_UI_JUNIT4
        )

    val NETWORK_LIBRARIES =
        listOf(
            KTOR_CLIENT_CORE,
            KTOR_CLIENT_OKHTTP,
            RETROFIT,
            OKHTTP3_LOGGING_INTERCEPTOR
        )
}

object PluginConstants {
    val KOTLIN_JVM =
        PluginRequirement(
            key = "kotlin-jvm",
            id = "org.jetbrains.kotlin.jvm",
            versionRefKey = "kotlin"
        )

    val KOTLIN_ANDROID =
        PluginRequirement(
            key = "kotlin-android",
            id = "org.jetbrains.kotlin.android",
            versionRefKey = "kotlin"
        )

    val KSP =
        PluginRequirement(
            key = "ksp",
            id = "com.google.devtools.ksp",
            versionRefKey = "ksp"
        )

    val COMPOSE_COMPILER =
        PluginRequirement(
            key = "compose-compiler",
            id = "org.jetbrains.kotlin.plugin.compose",
            versionRefKey = "kotlin"
        )

    val ANDROID_APPLICATION =
        PluginRequirement(
            key = "android-application",
            id = "com.android.application",
            versionRefKey = "androidGradlePlugin"
        )

    val ANDROID_LIBRARY =
        PluginRequirement(
            key = "android-library",
            id = "com.android.library",
            versionRefKey = "androidGradlePlugin"
        )

    val KTLINT =
        PluginRequirement(
            key = "ktlint",
            id = "org.jlleitschuh.gradle.ktlint",
            versionRefKey = "ktlint"
        )

    val DETEKT =
        PluginRequirement(
            key = "detekt",
            id = "io.gitlab.arturbosch.detekt",
            versionRefKey = "detekt"
        )

    val KOTLIN_PLUGINS =
        listOf(
            KOTLIN_JVM,
            KOTLIN_ANDROID,
            KSP
        )

    val ANDROID_PLUGINS =
        listOf(
            ANDROID_APPLICATION,
            ANDROID_LIBRARY
        )
}
