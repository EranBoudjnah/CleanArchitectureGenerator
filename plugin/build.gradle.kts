import org.jetbrains.intellij.platform.gradle.tasks.RunIdeTask
import java.util.Properties

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
        val localIdePath =
            project.readLocalProperty("intellij.localPath")
                ?: error(
                    "❌ Please define 'intellij.localPath' in local.properties " +
                        "(e.g., /Users/USERNAME/Applications/Android Studio.app/Contents)"
                )
        local(localIdePath)
        testFramework(org.jetbrains.intellij.platform.gradle.TestFrameworkType.Platform)
    }
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

private fun Project.readLocalProperty(propertyName: String): String? {
    val localPropertiesFile = File(rootProject.rootDir, "local.properties")
    if (!localPropertiesFile.exists()) return null
    val properties = Properties()
    localPropertiesFile.inputStream().use { properties.load(it) }
    return properties.getProperty(propertyName)
}
