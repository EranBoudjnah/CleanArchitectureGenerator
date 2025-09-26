package com.mitteloupe.cag.core.generation.versioncatalog

import com.mitteloupe.cag.core.generation.versioncatalog.SectionEntryRequirement.LibraryRequirement
import com.mitteloupe.cag.core.generation.versioncatalog.SectionEntryRequirement.PluginRequirement
import com.mitteloupe.cag.core.generation.versioncatalog.SectionEntryRequirement.VersionRequirement
import com.mitteloupe.cag.core.generation.versioncatalog.VersionCatalogConstants.ANDROIDX_ACTIVITY_COMPOSE_VERSION
import com.mitteloupe.cag.core.generation.versioncatalog.VersionCatalogConstants.ANDROIDX_APPCOMPAT_VERSION
import com.mitteloupe.cag.core.generation.versioncatalog.VersionCatalogConstants.ANDROIDX_CONSTRAINT_LAYOUT_VERSION
import com.mitteloupe.cag.core.generation.versioncatalog.VersionCatalogConstants.ANDROIDX_CORE_KTX_VERSION
import com.mitteloupe.cag.core.generation.versioncatalog.VersionCatalogConstants.ANDROIDX_FRAGMENT_KTX_VERSION
import com.mitteloupe.cag.core.generation.versioncatalog.VersionCatalogConstants.ANDROIDX_LIFECYCLE_RUNTIME_KTX_VERSION
import com.mitteloupe.cag.core.generation.versioncatalog.VersionCatalogConstants.ANDROIDX_NAVIGATION_FRAGMENT_KTX_VERSION
import com.mitteloupe.cag.core.generation.versioncatalog.VersionCatalogConstants.ANDROIDX_RECYCLER_VIEW_VERSION
import com.mitteloupe.cag.core.generation.versioncatalog.VersionCatalogConstants.ANDROID_GRADLE_PLUGIN_VERSION
import com.mitteloupe.cag.core.generation.versioncatalog.VersionCatalogConstants.COMPOSE_BOM_VERSION
import com.mitteloupe.cag.core.generation.versioncatalog.VersionCatalogConstants.COMPOSE_NAVIGATION_VERSION
import com.mitteloupe.cag.core.generation.versioncatalog.VersionCatalogConstants.DETEKT_VERSION
import com.mitteloupe.cag.core.generation.versioncatalog.VersionCatalogConstants.JUNIT4_VERSION
import com.mitteloupe.cag.core.generation.versioncatalog.VersionCatalogConstants.KOTLINX_COROUTINES_VERSION
import com.mitteloupe.cag.core.generation.versioncatalog.VersionCatalogConstants.KOTLIN_VERSION
import com.mitteloupe.cag.core.generation.versioncatalog.VersionCatalogConstants.KSP_VERSION
import com.mitteloupe.cag.core.generation.versioncatalog.VersionCatalogConstants.KTLINT_VERSION
import com.mitteloupe.cag.core.generation.versioncatalog.VersionCatalogConstants.KTOR_VERSION
import com.mitteloupe.cag.core.generation.versioncatalog.VersionCatalogConstants.MATERIAL_VERSION
import com.mitteloupe.cag.core.generation.versioncatalog.VersionCatalogConstants.OKHTTP3_LOGGING_INTERCEPTOR_VERSION
import com.mitteloupe.cag.core.generation.versioncatalog.VersionCatalogConstants.OKHTTP3_VERSION
import com.mitteloupe.cag.core.generation.versioncatalog.VersionCatalogConstants.RETROFIT_VERSION
import com.mitteloupe.cag.core.generation.versioncatalog.VersionCatalogConstants.TEST_ANDROIDX_ESPRESSO_CORE_VERSION
import com.mitteloupe.cag.core.generation.versioncatalog.VersionCatalogConstants.TEST_ANDROIDX_JUNIT_VERSION
import com.mitteloupe.cag.core.generation.versioncatalog.VersionCatalogConstants.TEST_ANDROIDX_RULES_VERSION
import com.mitteloupe.cag.core.generation.versioncatalog.VersionCatalogConstants.TEST_ANDROID_HILT_VERSION
import com.mitteloupe.cag.core.generation.versioncatalog.VersionCatalogConstants.TEST_ANDROID_MOCKWEBSERVER_VERSION
import com.mitteloupe.cag.core.generation.versioncatalog.VersionCatalogConstants.TEST_ANDROID_UI_AUTOMATOR_VERSION
import com.mitteloupe.cag.core.generation.versioncatalog.VersionCatalogConstants.TEST_MOCKITO_ANDROID_VERSION
import com.mitteloupe.cag.core.generation.versioncatalog.VersionCatalogConstants.TEST_MOCKITO_CORE_VERSION
import com.mitteloupe.cag.core.generation.versioncatalog.VersionCatalogConstants.TEST_MOCKITO_KOTLIN_VERSION

data class DependencyConfiguration(
    val versions: List<VersionRequirement>,
    val libraries: List<LibraryRequirement>,
    val plugins: List<PluginRequirement>
)

object VersionCatalogConstants {
    internal val KOTLIN_VERSION = VersionRequirement(key = "kotlin", version = "2.2.10")
    internal val COMPILE_SDK_VERSION = VersionRequirement(key = "compileSdk", version = "35")
    internal val MIN_SDK_VERSION = VersionRequirement(key = "minSdk", version = "24")
    internal val TARGET_SDK_VERSION = VersionRequirement(key = "targetSdk", version = "35")

    internal val ANDROID_GRADLE_PLUGIN_VERSION = VersionRequirement(key = "androidGradlePlugin", version = "8.12.2")

    internal val COMPOSE_BOM_VERSION = VersionRequirement(key = "composeBom", version = "2025.08.01")
    internal val COMPOSE_NAVIGATION_VERSION = VersionRequirement(key = "composeNavigation", version = "2.9.3")

    internal val JUNIT4_VERSION = VersionRequirement(key = "junit4", version = "4.13.2")
    internal val KSP_VERSION = VersionRequirement(key = "ksp", version = "2.2.10-2.0.2")

    internal val KTLINT_VERSION = VersionRequirement(key = "ktlint", version = "13.1.0")
    internal val DETEKT_VERSION = VersionRequirement(key = "detekt", version = "1.23.6")

    internal val ANDROIDX_CORE_KTX_VERSION = VersionRequirement(key = "androidxCoreKtx", version = "1.12.0")
    internal val ANDROIDX_LIFECYCLE_RUNTIME_KTX_VERSION = VersionRequirement(key = "androidxLifecycleRuntimeKtx", version = "2.7.0")
    internal val KOTLINX_COROUTINES_VERSION = VersionRequirement(key = "kotlinxCoroutines", version = "1.7.3")
    internal val MATERIAL_VERSION = VersionRequirement(key = "material", version = "1.11.0")
    internal val OKHTTP3_VERSION = VersionRequirement(key = "okhttp3", version = "4.12.0")
    internal val ANDROIDX_APPCOMPAT_VERSION = VersionRequirement(key = "androidxAppcompat", version = "1.6.1")

    internal val ANDROIDX_RECYCLER_VIEW_VERSION = VersionRequirement(key = "androidxRecyclerView", version = "1.3.2")
    internal val ANDROIDX_FRAGMENT_KTX_VERSION = VersionRequirement(key = "androidxFragmentKtx", version = "1.6.2")
    internal val ANDROIDX_NAVIGATION_FRAGMENT_KTX_VERSION = VersionRequirement(key = "androidxNavigationFragmentKtx", version = "2.7.6")
    internal val ANDROIDX_CONSTRAINT_LAYOUT_VERSION = VersionRequirement(key = "androidxConstraintLayout", version = "2.1.4")

    internal val ANDROIDX_ACTIVITY_COMPOSE_VERSION = VersionRequirement(key = "androidxActivityCompose", version = "1.8.2")

    internal val KTOR_VERSION = VersionRequirement(key = "ktor", version = "3.0.3")
    internal val RETROFIT_VERSION = VersionRequirement(key = "retrofit", version = "2.11.0")
    internal val OKHTTP3_LOGGING_INTERCEPTOR_VERSION = VersionRequirement(key = "okhttp3LoggingInterceptor", version = "4.12.0")

    internal val TEST_MOCKITO_CORE_VERSION = VersionRequirement(key = "mockitoCore", version = "5.20.0")
    internal val TEST_MOCKITO_KOTLIN_VERSION = VersionRequirement(key = "mockitoKotlin", version = "6.0.0")
    internal val TEST_MOCKITO_ANDROID_VERSION = VersionRequirement(key = "mockitoAndroid", version = "2.28.6")

    internal val TEST_ANDROIDX_JUNIT_VERSION = VersionRequirement(key = "androidxJunit", version = "1.1.5")
    internal val TEST_ANDROIDX_ESPRESSO_CORE_VERSION = VersionRequirement(key = "androidxEspressoCore", version = "3.5.1")
    internal val TEST_ANDROID_HILT_VERSION = VersionRequirement(key = "androidHilt", version = "2.48")
    internal val TEST_ANDROID_UI_AUTOMATOR_VERSION = VersionRequirement(key = "androidxUiautomator", version = "2.2.0")
    internal val TEST_ANDROID_MOCKWEBSERVER_VERSION = VersionRequirement(key = "mockwebserver", version = "4.12.0")
    internal val TEST_ANDROIDX_RULES_VERSION = VersionRequirement(key = "androidxTestRules", version = "1.5.0")

    val ANDROID_VERSIONS =
        listOf(
            COMPILE_SDK_VERSION,
            MIN_SDK_VERSION,
            TARGET_SDK_VERSION,
            ANDROID_GRADLE_PLUGIN_VERSION
        )
}

object LibraryConstants {
    val ANDROIDX_CORE_KTX =
        LibraryRequirement(
            key = "androidx-core-ktx",
            module = "androidx.core:core-ktx",
            version = ANDROIDX_CORE_KTX_VERSION
        )

    val ANDROIDX_LIFECYCLE_RUNTIME_KTX =
        LibraryRequirement(
            key = "androidx-lifecycle-runtime-ktx",
            module = "androidx.lifecycle:lifecycle-runtime-ktx",
            version = ANDROIDX_LIFECYCLE_RUNTIME_KTX_VERSION
        )

    val KOTLINX_COROUTINES_CORE =
        LibraryRequirement(
            key = "kotlinx-coroutines-core",
            module = "org.jetbrains.kotlinx:kotlinx-coroutines-core",
            version = KOTLINX_COROUTINES_VERSION
        )

    val TEST_KOTLINX_COROUTINES =
        LibraryRequirement(
            key = "test-kotlinx-coroutines",
            module = "org.jetbrains.kotlinx:kotlinx-coroutines-test",
            version = KOTLINX_COROUTINES_VERSION
        )

    val COMPOSE_BOM =
        LibraryRequirement(
            key = "compose-bom",
            module = "androidx.compose:compose-bom",
            version = COMPOSE_BOM_VERSION
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
            version = COMPOSE_NAVIGATION_VERSION
        )

    val TEST_JUNIT =
        LibraryRequirement(
            key = "test-junit",
            module = "junit:junit",
            version = JUNIT4_VERSION
        )

    val TEST_ANDROIDX_JUNIT =
        LibraryRequirement(
            key = "test-androidx-junit",
            module = "androidx.test.ext:junit",
            version = TEST_ANDROIDX_JUNIT_VERSION
        )

    val TEST_ANDROIDX_ESPRESSO_CORE =
        LibraryRequirement(
            key = "test-androidx-espresso-core",
            module = "androidx.test.espresso:espresso-core",
            version = TEST_ANDROIDX_ESPRESSO_CORE_VERSION
        )

    val TEST_ANDROID_HILT =
        LibraryRequirement(
            key = "test-android-hilt",
            module = "com.google.dagger:hilt-android-testing",
            version = TEST_ANDROID_HILT_VERSION
        )

    val TEST_ANDROID_UI_AUTOMATOR =
        LibraryRequirement(
            key = "test-android-uiautomator",
            module = "androidx.test.uiautomator:uiautomator",
            version = TEST_ANDROID_UI_AUTOMATOR_VERSION
        )

    val TEST_ANDROID_MOCKWEBSERVER =
        LibraryRequirement(
            key = "test-android-mockwebserver",
            module = "com.squareup.okhttp3:mockwebserver",
            version = TEST_ANDROID_MOCKWEBSERVER_VERSION
        )

    val TEST_ANDROIDX_RULES =
        LibraryRequirement(
            key = "test-androidx-rules",
            module = "androidx.test:rules",
            version = TEST_ANDROIDX_RULES_VERSION
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
            version = MATERIAL_VERSION
        )

    val OKHTTP3 =
        LibraryRequirement(
            key = "okhttp3",
            module = "com.squareup.okhttp3:okhttp",
            version = OKHTTP3_VERSION
        )

    val ANDROIDX_APPCOMPAT =
        LibraryRequirement(
            key = "androidx-appcompat",
            module = "androidx.appcompat:appcompat",
            version = ANDROIDX_APPCOMPAT_VERSION
        )

    val ANDROIDX_RECYCLERVIEW =
        LibraryRequirement(
            key = "androidx-recyclerview",
            module = "androidx.recyclerview:recyclerview",
            version = ANDROIDX_RECYCLER_VIEW_VERSION
        )

    val ANDROIDX_FRAGMENT_KTX =
        LibraryRequirement(
            key = "androidx-fragment-ktx",
            module = "androidx.fragment:fragment-ktx",
            version = ANDROIDX_FRAGMENT_KTX_VERSION
        )

    val ANDROIDX_NAVIGATION_FRAGMENT_KTX =
        LibraryRequirement(
            key = "androidx-navigation-fragment-ktx",
            module = "androidx.navigation:navigation-fragment-ktx",
            version = ANDROIDX_NAVIGATION_FRAGMENT_KTX_VERSION
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
            version = ANDROIDX_ACTIVITY_COMPOSE_VERSION
        )

    val ANDROIDX_CONSTRAINTLAYOUT =
        LibraryRequirement(
            key = "androidx-constraintlayout",
            module = "androidx.constraintlayout:constraintlayout",
            version = ANDROIDX_CONSTRAINT_LAYOUT_VERSION
        )

    val KTOR_CLIENT_CORE =
        LibraryRequirement(
            key = "ktor-client-core",
            module = "io.ktor:ktor-client-core",
            version = KTOR_VERSION
        )

    val KTOR_CLIENT_OKHTTP =
        LibraryRequirement(
            key = "ktor-client-okhttp",
            module = "io.ktor:ktor-client-okhttp",
            version = KTOR_VERSION
        )

    val RETROFIT =
        LibraryRequirement(
            key = "retrofit",
            module = "com.squareup.retrofit2:retrofit",
            version = RETROFIT_VERSION
        )

    val OKHTTP3_LOGGING_INTERCEPTOR =
        LibraryRequirement(
            key = "okhttp3-logging-interceptor",
            module = "com.squareup.okhttp3:logging-interceptor",
            version = OKHTTP3_LOGGING_INTERCEPTOR_VERSION
        )

    val TEST_MOCKITO_CORE =
        LibraryRequirement(
            key = "test-mockito-core",
            module = "org.mockito:mockito-core",
            version = TEST_MOCKITO_CORE_VERSION
        )

    val TEST_MOCKITO_KOTLIN =
        LibraryRequirement(
            key = "test-mockito-kotlin",
            module = "org.mockito.kotlin:mockito-kotlin",
            version = TEST_MOCKITO_KOTLIN_VERSION
        )

    val TEST_MOCKITO_ANDROID =
        LibraryRequirement(
            key = "test-mockito-android",
            module = "com.linkedin.dexmaker:dexmaker-mockito-inline",
            version = TEST_MOCKITO_ANDROID_VERSION
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

    val TEST_MOCKITO_LIBRARIES =
        listOf(
            TEST_MOCKITO_CORE,
            TEST_MOCKITO_KOTLIN
        )
}

object PluginConstants {
    val KOTLIN_JVM =
        PluginRequirement(
            key = "kotlin-jvm",
            id = "org.jetbrains.kotlin.jvm",
            version = KOTLIN_VERSION
        )

    val KOTLIN_ANDROID =
        PluginRequirement(
            key = "kotlin-android",
            id = "org.jetbrains.kotlin.android",
            version = KOTLIN_VERSION
        )

    val KSP =
        PluginRequirement(
            key = "ksp",
            id = "com.google.devtools.ksp",
            version = KSP_VERSION
        )

    val COMPOSE_COMPILER =
        PluginRequirement(
            key = "compose-compiler",
            id = "org.jetbrains.kotlin.plugin.compose",
            version = KOTLIN_VERSION
        )

    val ANDROID_APPLICATION =
        PluginRequirement(
            key = "android-application",
            id = "com.android.application",
            version = ANDROID_GRADLE_PLUGIN_VERSION
        )

    val ANDROID_LIBRARY =
        PluginRequirement(
            key = "android-library",
            id = "com.android.library",
            version = ANDROID_GRADLE_PLUGIN_VERSION
        )

    val KTLINT =
        PluginRequirement(
            key = "ktlint",
            id = "org.jlleitschuh.gradle.ktlint",
            version = KTLINT_VERSION
        )

    val DETEKT =
        PluginRequirement(
            key = "detekt",
            id = "io.gitlab.arturbosch.detekt",
            version = DETEKT_VERSION
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
