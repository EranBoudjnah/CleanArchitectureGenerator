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
import com.mitteloupe.cag.core.generation.versioncatalog.VersionCatalogConstants.OKHTTP3_VERSION
import com.mitteloupe.cag.core.generation.versioncatalog.VersionCatalogConstants.RETROFIT_VERSION
import com.mitteloupe.cag.core.generation.versioncatalog.VersionCatalogConstants.TEST_ANDROIDX_ESPRESSO_CORE_VERSION
import com.mitteloupe.cag.core.generation.versioncatalog.VersionCatalogConstants.TEST_ANDROIDX_JUNIT_VERSION
import com.mitteloupe.cag.core.generation.versioncatalog.VersionCatalogConstants.TEST_ANDROIDX_RULES_VERSION
import com.mitteloupe.cag.core.generation.versioncatalog.VersionCatalogConstants.TEST_ANDROID_HILT_VERSION
import com.mitteloupe.cag.core.generation.versioncatalog.VersionCatalogConstants.TEST_ANDROID_UI_AUTOMATOR_VERSION
import com.mitteloupe.cag.core.generation.versioncatalog.VersionCatalogConstants.TEST_MOCKITO_ANDROID_VERSION
import com.mitteloupe.cag.core.generation.versioncatalog.VersionCatalogConstants.TEST_MOCKITO_CORE_VERSION
import com.mitteloupe.cag.core.generation.versioncatalog.VersionCatalogConstants.TEST_MOCKITO_KOTLIN_VERSION
import com.mitteloupe.cag.core.generation.versioncatalog.VersionCatalogSettingsAccessor.getVersionForKey

data class DependencyConfiguration(
    val versions: List<VersionRequirement>,
    val libraries: List<LibraryRequirement>,
    val plugins: List<PluginRequirement>
)

object VersionCatalogConstants {
    internal val KOTLIN_VERSION = VersionRequirement(key = "kotlin", version = "2.2.10")
    internal val COMPILE_SDK_VERSION = VersionRequirement(key = "compileSdk", version = "36")
    internal val MIN_SDK_VERSION = VersionRequirement(key = "minSdk", version = "24")
    internal val TARGET_SDK_VERSION = VersionRequirement(key = "targetSdk", version = "36")

    internal val ANDROID_GRADLE_PLUGIN_VERSION = VersionRequirement(key = "androidGradlePlugin", version = "8.12.2")

    internal val COMPOSE_BOM_VERSION: VersionRequirement
        get() = VersionRequirement(key = "composeBom", version = getVersionForKey("composeBom", "2025.08.01"))
    internal val COMPOSE_NAVIGATION_VERSION: VersionRequirement
        get() = VersionRequirement(key = "composeNavigation", version = getVersionForKey("composeNavigation", "2.9.3"))

    internal val JUNIT4_VERSION: VersionRequirement
        get() = VersionRequirement(key = "junit4", version = getVersionForKey("junit4", "4.13.2"))
    internal val KSP_VERSION: VersionRequirement
        get() = VersionRequirement(key = "ksp", version = getVersionForKey("ksp", "2.2.10-2.0.2"))

    internal val KTLINT_VERSION: VersionRequirement
        get() = VersionRequirement(key = "ktlint", version = getVersionForKey("ktlint", "13.1.0"))
    internal val DETEKT_VERSION: VersionRequirement
        get() = VersionRequirement(key = "detekt", version = getVersionForKey("detekt", "1.23.6"))

    internal val ANDROIDX_CORE_KTX_VERSION: VersionRequirement
        get() = VersionRequirement(key = "androidxCoreKtx", version = getVersionForKey("androidxCoreKtx", "1.12.0"))
    internal val ANDROIDX_LIFECYCLE_RUNTIME_KTX_VERSION: VersionRequirement
        get() = VersionRequirement(key = "androidxLifecycleRuntimeKtx", version = getVersionForKey("androidxLifecycleRuntimeKtx", "2.7.0"))
    internal val KOTLINX_COROUTINES_VERSION: VersionRequirement
        get() = VersionRequirement(key = "kotlinxCoroutines", version = getVersionForKey("kotlinxCoroutines", "1.7.3"))
    internal val MATERIAL_VERSION: VersionRequirement
        get() = VersionRequirement(key = "material", version = getVersionForKey("material", "1.11.0"))
    internal val OKHTTP3_VERSION: VersionRequirement
        get() = VersionRequirement(key = "okhttp3", version = getVersionForKey("okhttp3", "4.12.0"))
    internal val ANDROIDX_APPCOMPAT_VERSION: VersionRequirement
        get() = VersionRequirement(key = "androidxAppcompat", version = getVersionForKey("androidxAppcompat", "1.6.1"))
    internal val HILT_VERSION: VersionRequirement
        get() = VersionRequirement(key = "hilt", version = getVersionForKey("hilt", "2.57.2"))
    internal val ANDROIDX_RECYCLER_VIEW_VERSION: VersionRequirement
        get() = VersionRequirement(key = "androidxRecyclerView", version = getVersionForKey("androidxRecyclerView", "1.3.2"))
    internal val ANDROIDX_FRAGMENT_KTX_VERSION: VersionRequirement
        get() = VersionRequirement(key = "androidxFragmentKtx", version = getVersionForKey("androidxFragmentKtx", "1.6.2"))
    internal val ANDROIDX_NAVIGATION_FRAGMENT_KTX_VERSION: VersionRequirement
        get() =
            VersionRequirement(
                key = "androidxNavigationFragmentKtx",
                version = getVersionForKey("androidxNavigationFragmentKtx", "2.7.6")
            )
    internal val ANDROIDX_CONSTRAINT_LAYOUT_VERSION: VersionRequirement
        get() = VersionRequirement(key = "androidxConstraintLayout", version = getVersionForKey("androidxConstraintLayout", "2.1.4"))

    internal val ANDROIDX_ACTIVITY_COMPOSE_VERSION: VersionRequirement
        get() = VersionRequirement(key = "androidxActivityCompose", version = getVersionForKey("androidxActivityCompose", "1.8.2"))

    internal val KTOR_VERSION: VersionRequirement
        get() = VersionRequirement(key = "ktor", version = getVersionForKey("ktor", "3.0.3"))
    internal val RETROFIT_VERSION: VersionRequirement
        get() = VersionRequirement(key = "retrofit", version = getVersionForKey("retrofit", "2.11.0"))

    internal val TEST_MOCKITO_CORE_VERSION: VersionRequirement
        get() = VersionRequirement(key = "mockitoCore", version = getVersionForKey("mockitoCore", "5.20.0"))
    internal val TEST_MOCKITO_KOTLIN_VERSION: VersionRequirement
        get() = VersionRequirement(key = "mockitoKotlin", version = getVersionForKey("mockitoKotlin", "6.0.0"))
    internal val TEST_MOCKITO_ANDROID_VERSION: VersionRequirement
        get() = VersionRequirement(key = "mockitoAndroid", version = getVersionForKey("mockitoAndroid", "2.28.6"))

    internal val TEST_ANDROIDX_JUNIT_VERSION: VersionRequirement
        get() = VersionRequirement(key = "androidxJunit", version = getVersionForKey("androidxJunit", "1.1.5"))
    internal val TEST_ANDROIDX_ESPRESSO_CORE_VERSION: VersionRequirement
        get() = VersionRequirement(key = "androidxEspressoCore", version = getVersionForKey("androidxEspressoCore", "3.5.1"))
    internal val TEST_ANDROID_HILT_VERSION: VersionRequirement
        get() = VersionRequirement(key = "androidHilt", version = getVersionForKey("androidHilt", "2.48"))
    internal val TEST_ANDROID_UI_AUTOMATOR_VERSION: VersionRequirement
        get() = VersionRequirement(key = "androidxUiautomator", version = getVersionForKey("androidxUiautomator", "2.2.0"))
    internal val TEST_ANDROIDX_RULES_VERSION: VersionRequirement
        get() = VersionRequirement(key = "androidxTestRules", version = getVersionForKey("androidxTestRules", "1.5.0"))

    val ANDROID_VERSIONS =
        listOf(
            COMPILE_SDK_VERSION,
            MIN_SDK_VERSION,
            TARGET_SDK_VERSION,
            ANDROID_GRADLE_PLUGIN_VERSION
        )
}

object LibraryConstants {
    val ANDROIDX_CORE_KTX: LibraryRequirement
        get() =
            LibraryRequirement(
                key = "androidx-core-ktx",
                module = "androidx.core:core-ktx",
                version = ANDROIDX_CORE_KTX_VERSION
            )

    val ANDROIDX_LIFECYCLE_RUNTIME_KTX: LibraryRequirement
        get() =
            LibraryRequirement(
                key = "androidx-lifecycle-runtime-ktx",
                module = "androidx.lifecycle:lifecycle-runtime-ktx",
                version = ANDROIDX_LIFECYCLE_RUNTIME_KTX_VERSION
            )

    val KOTLINX_COROUTINES_CORE: LibraryRequirement
        get() =
            LibraryRequirement(
                key = "kotlinx-coroutines-core",
                module = "org.jetbrains.kotlinx:kotlinx-coroutines-core",
                version = KOTLINX_COROUTINES_VERSION
            )

    val TEST_KOTLINX_COROUTINES: LibraryRequirement
        get() =
            LibraryRequirement(
                key = "test-kotlinx-coroutines",
                module = "org.jetbrains.kotlinx:kotlinx-coroutines-test",
                version = KOTLINX_COROUTINES_VERSION
            )

    val COMPOSE_BOM: LibraryRequirement
        get() =
            LibraryRequirement(
                key = "compose-bom",
                module = "androidx.compose:compose-bom",
                version = COMPOSE_BOM_VERSION
            )

    val COMPOSE_UI: LibraryRequirement
        get() =
            LibraryRequirement(
                key = "compose-ui",
                module = "androidx.compose.ui:ui"
            )

    val COMPOSE_UI_GRAPHICS: LibraryRequirement
        get() =
            LibraryRequirement(
                key = "compose-ui-graphics",
                module = "androidx.compose.ui:ui-graphics"
            )

    val COMPOSE_UI_TOOLING_PREVIEW: LibraryRequirement
        get() =
            LibraryRequirement(
                key = "compose-ui-tooling-preview",
                module = "androidx.compose.ui:ui-tooling-preview"
            )

    val COMPOSE_MATERIAL3: LibraryRequirement
        get() =
            LibraryRequirement(
                key = "compose-material3",
                module = "androidx.compose.material3:material3"
            )

    val COMPOSE_NAVIGATION: LibraryRequirement
        get() =
            LibraryRequirement(
                key = "compose-navigation",
                module = "androidx.navigation:navigation-compose",
                version = COMPOSE_NAVIGATION_VERSION
            )

    val TEST_JUNIT: LibraryRequirement
        get() =
            LibraryRequirement(
                key = "test-junit",
                module = "junit:junit",
                version = JUNIT4_VERSION
            )

    val TEST_ANDROIDX_JUNIT: LibraryRequirement
        get() =
            LibraryRequirement(
                key = "test-androidx-junit",
                module = "androidx.test.ext:junit",
                version = TEST_ANDROIDX_JUNIT_VERSION
            )

    val TEST_ANDROIDX_ESPRESSO_CORE: LibraryRequirement
        get() =
            LibraryRequirement(
                key = "test-androidx-espresso-core",
                module = "androidx.test.espresso:espresso-core",
                version = TEST_ANDROIDX_ESPRESSO_CORE_VERSION
            )

    val TEST_ANDROID_HILT: LibraryRequirement
        get() =
            LibraryRequirement(
                key = "test-android-hilt",
                module = "com.google.dagger:hilt-android-testing",
                version = TEST_ANDROID_HILT_VERSION
            )

    val TEST_ANDROID_UI_AUTOMATOR: LibraryRequirement
        get() =
            LibraryRequirement(
                key = "test-android-uiautomator",
                module = "androidx.test.uiautomator:uiautomator",
                version = TEST_ANDROID_UI_AUTOMATOR_VERSION
            )

    val TEST_ANDROID_MOCKWEBSERVER: LibraryRequirement
        get() =
            LibraryRequirement(
                key = "test-android-mockwebserver",
                module = "com.squareup.okhttp3:mockwebserver",
                version = OKHTTP3_VERSION
            )

    val TEST_ANDROIDX_RULES: LibraryRequirement
        get() =
            LibraryRequirement(
                key = "test-androidx-rules",
                module = "androidx.test:rules",
                version = TEST_ANDROIDX_RULES_VERSION
            )

    val TEST_COMPOSE_UI_JUNIT4: LibraryRequirement
        get() =
            LibraryRequirement(
                key = "test-compose-ui-junit4",
                module = "androidx.compose.ui:ui-test-junit4"
            )

    val MATERIAL: LibraryRequirement
        get() =
            LibraryRequirement(
                key = "material",
                module = "com.google.android.material:material",
                version = MATERIAL_VERSION
            )

    val OKHTTP3: LibraryRequirement
        get() =
            LibraryRequirement(
                key = "okhttp3",
                module = "com.squareup.okhttp3:okhttp",
                version = OKHTTP3_VERSION
            )

    val HILT_ANDROID: LibraryRequirement
        get() =
            LibraryRequirement(
                key = "hilt-android",
                module = "com.google.dagger:hilt-android",
                version = VersionCatalogConstants.HILT_VERSION
            )

    val HILT_ANDROID_COMPILER: LibraryRequirement
        get() =
            LibraryRequirement(
                key = "hilt-android-compiler",
                module = "com.google.dagger:hilt-android-compiler",
                version = VersionCatalogConstants.HILT_VERSION
            )

    val ANDROIDX_APPCOMPAT: LibraryRequirement
        get() =
            LibraryRequirement(
                key = "androidx-appcompat",
                module = "androidx.appcompat:appcompat",
                version = ANDROIDX_APPCOMPAT_VERSION
            )

    val ANDROIDX_RECYCLERVIEW: LibraryRequirement
        get() =
            LibraryRequirement(
                key = "androidx-recyclerview",
                module = "androidx.recyclerview:recyclerview",
                version = ANDROIDX_RECYCLER_VIEW_VERSION
            )

    val ANDROIDX_FRAGMENT_KTX: LibraryRequirement
        get() =
            LibraryRequirement(
                key = "androidx-fragment-ktx",
                module = "androidx.fragment:fragment-ktx",
                version = ANDROIDX_FRAGMENT_KTX_VERSION
            )

    val ANDROIDX_NAVIGATION_FRAGMENT_KTX: LibraryRequirement
        get() =
            LibraryRequirement(
                key = "androidx-navigation-fragment-ktx",
                module = "androidx.navigation:navigation-fragment-ktx",
                version = ANDROIDX_NAVIGATION_FRAGMENT_KTX_VERSION
            )

    val ANDROIDX_UI_TOOLING: LibraryRequirement
        get() =
            LibraryRequirement(
                key = "compose-ui-tooling",
                module = "androidx.compose.ui:ui-tooling"
            )

    val ANDROIDX_UI_TEST_MANIFEST: LibraryRequirement
        get() =
            LibraryRequirement(
                key = "compose-ui-test-manifest",
                module = "androidx.compose.ui:ui-test-manifest"
            )

    val ANDROIDX_ACTIVITY_COMPOSE: LibraryRequirement
        get() =
            LibraryRequirement(
                key = "androidx-activity-compose",
                module = "androidx.activity:activity-compose",
                version = ANDROIDX_ACTIVITY_COMPOSE_VERSION
            )

    val ANDROIDX_CONSTRAINTLAYOUT: LibraryRequirement
        get() =
            LibraryRequirement(
                key = "androidx-constraintlayout",
                module = "androidx.constraintlayout:constraintlayout",
                version = ANDROIDX_CONSTRAINT_LAYOUT_VERSION
            )

    val KTOR_CLIENT_CORE: LibraryRequirement
        get() =
            LibraryRequirement(
                key = "ktor-client-core",
                module = "io.ktor:ktor-client-core",
                version = KTOR_VERSION
            )

    val KTOR_CLIENT_OKHTTP: LibraryRequirement
        get() =
            LibraryRequirement(
                key = "ktor-client-okhttp",
                module = "io.ktor:ktor-client-okhttp",
                version = KTOR_VERSION
            )

    val RETROFIT: LibraryRequirement
        get() =
            LibraryRequirement(
                key = "retrofit",
                module = "com.squareup.retrofit2:retrofit",
                version = RETROFIT_VERSION
            )

    val OKHTTP3_LOGGING_INTERCEPTOR: LibraryRequirement
        get() =
            LibraryRequirement(
                key = "okhttp3-logging-interceptor",
                module = "com.squareup.okhttp3:logging-interceptor",
                version = OKHTTP3_VERSION
            )

    val TEST_MOCKITO_CORE: LibraryRequirement
        get() =
            LibraryRequirement(
                key = "test-mockito-core",
                module = "org.mockito:mockito-core",
                version = TEST_MOCKITO_CORE_VERSION
            )

    val TEST_MOCKITO_KOTLIN: LibraryRequirement
        get() =
            LibraryRequirement(
                key = "test-mockito-kotlin",
                module = "org.mockito.kotlin:mockito-kotlin",
                version = TEST_MOCKITO_KOTLIN_VERSION
            )

    val TEST_MOCKITO_ANDROID: LibraryRequirement
        get() =
            LibraryRequirement(
                key = "test-mockito-android",
                module = "com.linkedin.dexmaker:dexmaker-mockito-inline",
                version = TEST_MOCKITO_ANDROID_VERSION
            )

    val CORE_ANDROID_LIBRARIES: List<LibraryRequirement>
        get() =
            listOf(
                ANDROIDX_CORE_KTX,
                ANDROIDX_LIFECYCLE_RUNTIME_KTX,
                ANDROIDX_APPCOMPAT,
                KOTLINX_COROUTINES_CORE,
                MATERIAL,
                OKHTTP3,
                HILT_ANDROID,
                HILT_ANDROID_COMPILER
            )

    val VIEW_LIBRARIES: List<LibraryRequirement>
        get() =
            listOf(
                ANDROIDX_RECYCLERVIEW,
                ANDROIDX_FRAGMENT_KTX,
                ANDROIDX_NAVIGATION_FRAGMENT_KTX,
                ANDROIDX_CONSTRAINTLAYOUT
            )

    val COMPOSE_LIBRARIES: List<LibraryRequirement>
        get() =
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

    val TESTING_LIBRARIES: List<LibraryRequirement>
        get() =
            listOf(
                TEST_JUNIT,
                TEST_ANDROIDX_JUNIT,
                TEST_ANDROIDX_ESPRESSO_CORE,
                TEST_ANDROID_HILT,
                TEST_ANDROID_UI_AUTOMATOR,
                TEST_ANDROID_MOCKWEBSERVER,
                TEST_ANDROIDX_RULES
            )

    val COMPOSE_TESTING_LIBRARIES: List<LibraryRequirement>
        get() =
            listOf(
                TEST_COMPOSE_UI_JUNIT4
            )

    val NETWORK_LIBRARIES: List<LibraryRequirement>
        get() =
            listOf(
                KTOR_CLIENT_CORE,
                KTOR_CLIENT_OKHTTP,
                RETROFIT,
                OKHTTP3_LOGGING_INTERCEPTOR
            )

    val TEST_MOCKITO_LIBRARIES: List<LibraryRequirement>
        get() =
            listOf(
                TEST_MOCKITO_CORE,
                TEST_MOCKITO_KOTLIN,
                TEST_MOCKITO_ANDROID
            )

    val ALL_LIBRARIES =
        CORE_ANDROID_LIBRARIES + VIEW_LIBRARIES + COMPOSE_LIBRARIES + TESTING_LIBRARIES + COMPOSE_TESTING_LIBRARIES +
            NETWORK_LIBRARIES + TEST_MOCKITO_LIBRARIES + TEST_KOTLINX_COROUTINES
}

object PluginConstants {
    val KOTLIN_JVM: PluginRequirement
        get() =
            PluginRequirement(
                key = "kotlin-jvm",
                id = "org.jetbrains.kotlin.jvm",
                version = KOTLIN_VERSION
            )

    val KOTLIN_ANDROID: PluginRequirement
        get() =
            PluginRequirement(
                key = "kotlin-android",
                id = "org.jetbrains.kotlin.android",
                version = KOTLIN_VERSION
            )

    val KSP: PluginRequirement
        get() =
            PluginRequirement(
                key = "ksp",
                id = "com.google.devtools.ksp",
                version = KSP_VERSION
            )

    val HILT_ANDROID: PluginRequirement
        get() =
            PluginRequirement(
                key = "hilt",
                id = "com.google.dagger.hilt.android",
                version = VersionCatalogConstants.HILT_VERSION
            )

    val COMPOSE_COMPILER: PluginRequirement
        get() =
            PluginRequirement(
                key = "compose-compiler",
                id = "org.jetbrains.kotlin.plugin.compose",
                version = KOTLIN_VERSION
            )

    val ANDROID_APPLICATION: PluginRequirement
        get() =
            PluginRequirement(
                key = "android-application",
                id = "com.android.application",
                version = ANDROID_GRADLE_PLUGIN_VERSION
            )

    val ANDROID_LIBRARY: PluginRequirement
        get() =
            PluginRequirement(
                key = "android-library",
                id = "com.android.library",
                version = ANDROID_GRADLE_PLUGIN_VERSION
            )

    val KTLINT: PluginRequirement
        get() =
            PluginRequirement(
                key = "ktlint",
                id = "org.jlleitschuh.gradle.ktlint",
                version = KTLINT_VERSION
            )

    val DETEKT: PluginRequirement
        get() =
            PluginRequirement(
                key = "detekt",
                id = "io.gitlab.arturbosch.detekt",
                version = DETEKT_VERSION
            )

    val KOTLIN_PLUGINS: List<PluginRequirement>
        get() =
            listOf(
                KOTLIN_JVM,
                KOTLIN_ANDROID,
                KSP
            )

    val ANDROID_PLUGINS: List<PluginRequirement>
        get() =
            listOf(
                ANDROID_APPLICATION,
                ANDROID_LIBRARY
            )

    val ALL_PLUGINS: List<PluginRequirement>
        get() =
            KOTLIN_PLUGINS + ANDROID_PLUGINS +
                listOf(
                    COMPOSE_COMPILER,
                    KTLINT,
                    DETEKT,
                    HILT_ANDROID
                )
}
