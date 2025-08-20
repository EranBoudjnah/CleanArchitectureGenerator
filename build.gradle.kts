import java.util.Properties

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.1.0"
    id("org.jetbrains.intellij.platform") version "2.5.0"
    id("org.jlleitschuh.gradle.ktlint") version "12.1.1"
}

group = "com.mitteloupe.cag"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
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
        ideaVersion {
            sinceBuild = "241"
        }

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

    register<Task>("installGitHooks") {
        group = "automation"
        description = "Install pre-commit git hook from automation/ into the repository .git/hooks directory"
        doLast {
            project.installGitHookFromAutomation()
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

private fun Project.installGitHookFromAutomation() {
    val projectRootDirectory = rootProject.rootDir
    val gitDirectory = File(projectRootDirectory, ".git")
    val gitHooksDirectory = File(gitDirectory, "hooks")
    val preCommitScriptFile = File(projectRootDirectory, "automation/pre-commit")

    if (!gitDirectory.exists()) {
        logger.lifecycle("⚠\uFE0F Skipping git hook installation: .git directory not found at ${gitDirectory.absolutePath}")
        return
    }
    if (!preCommitScriptFile.exists()) {
        logger.lifecycle("⚠\uFE0F Skipping git hook installation: script not found at ${preCommitScriptFile.absolutePath}")
        return
    }

    if (!gitHooksDirectory.exists()) gitHooksDirectory.mkdirs()
    val scriptDestinationFile = File(gitHooksDirectory, "pre-commit")
    preCommitScriptFile.copyTo(scriptDestinationFile, overwrite = true)
    logger.lifecycle("✅ Installed git pre-commit hook -> ${scriptDestinationFile.absolutePath}")
    if (!scriptDestinationFile.setExecutable(true)) {
        logger.lifecycle("⚠\uFE0F Failed to make pre-commit git hook script executable")
    }
}

private fun Project.readLocalProperty(propertyName: String): String? {
    val localPropertiesFile = File(rootProject.rootDir, "local.properties")
    if (!localPropertiesFile.exists()) return null
    val properties = Properties()
    localPropertiesFile.inputStream().use { properties.load(it) }
    return properties.getProperty(propertyName)
}

gradle.projectsEvaluated {
    project.installGitHookFromAutomation()
}
