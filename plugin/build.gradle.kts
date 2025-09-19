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
            ide(IntelliJPlatformType.AndroidStudio, libs.versions.androidStudio.get())
        }
    }
}

dependencies {
    implementation(project(":core"))

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
            """
            Initial version.
            """.trimIndent()
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
