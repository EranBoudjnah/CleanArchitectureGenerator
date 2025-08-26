import org.jetbrains.intellij.platform.gradle.tasks.RunIdeTask

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.intellij.platform")
    id("org.jlleitschuh.gradle.ktlint")
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
        androidStudio("2025.1.4.2")
        testFramework(org.jetbrains.intellij.platform.gradle.TestFrameworkType.Platform)
    }

    testImplementation("io.mockk:mockk:1.13.11")
    testImplementation("junit:junit:4.13.2")
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

    named<RunIdeTask>("runIde").configure {
        systemProperty("idea.kotlin.plugin.use.k2", "false")

        doFirst {
            logger.lifecycle("runIde: K2 disabled")
        }
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
