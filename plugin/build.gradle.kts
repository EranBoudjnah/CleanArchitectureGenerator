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

dependencies {
    implementation(project(":core"))

    intellijPlatform {
        androidStudio(libs.versions.androidStudio.get())
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
    withType<JavaCompile> {
        sourceCompatibility = "21"
        targetCompatibility = "21"
    }

    named("buildSearchableOptions").configure {
        enabled = false
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
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
