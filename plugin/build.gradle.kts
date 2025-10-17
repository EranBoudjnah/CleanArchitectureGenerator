import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("java")
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.intellij.platform)
    alias(libs.plugins.ktlint)
}

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

intellijPlatform {
    pluginVerification {
        ides {
            create(IntelliJPlatformType.AndroidStudio, "2024.3.1.13")
            create(IntelliJPlatformType.AndroidStudio, "2025.1.2.11")
            create(IntelliJPlatformType.AndroidStudio, "2025.1.3.7")
            create(IntelliJPlatformType.AndroidStudio, libs.versions.androidStudio.get())
        }
    }
}

dependencies {
    implementation(project(":core"))
    implementation(project(":git"))
    implementation(project(":lint"))

    intellijPlatform {
        androidStudio(libs.versions.androidStudio.get())
        bundledPlugin("org.jetbrains.android")
        testFramework(org.jetbrains.intellij.platform.gradle.TestFrameworkType.Platform)
    }

    testImplementation(libs.mockk)
    testImplementation(libs.junit4)
    testImplementation(libs.hamcrest)
}

intellijPlatform {
    pluginConfiguration {
        changeNotes =
            """## [0.4.0]

### Added

- Added dependency injection (DI) support across CLI, plugin, and templates, including options for Hilt and Koin (#39, #40, #41, #42, #43, #44, #45, #49, #50)
- Added hot-reload functionality to new project template (#48)
- Added plugin icon to README.md (#46, #47)

### Changed

- Polished and updated generated MainActivity and Application files (#34)
- Consolidated and improved help contents (#43)
- Broke down code generator for better maintainability (#36)
- Tightened Hilt integration and decoupled it from core libraries (#35, #37)
- Updated all dependencies and reformatted codebase (#33)
- Tidied and updated version catalog in generated projects (#32)

### Fixed

- Limited visibility of DI-related code (#49)
"""
    }
}

tasks {
    signPlugin {
        certificateChain = System.getenv("CAG_CERTIFICATE_CHAIN")
        privateKey = System.getenv("CAG_PRIVATE_KEY")
        password = System.getenv("CAG_PRIVATE_KEY_PASSWORD")
    }

    publishPlugin {
        token = System.getenv("CAG_PUBLISH_TOKEN")
    }

    withType<JavaCompile> {
        sourceCompatibility = JvmTarget.JVM_21.target
        targetCompatibility = JvmTarget.JVM_21.target
    }

    buildSearchableOptions.configure {
        enabled = false
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

ktlint {
    android.set(false)
    ignoreFailures.set(false)
    filter {
        exclude("**/build/**")
        include("**/*.kt")
        include("**/*.kts")
    }
}
