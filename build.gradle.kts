plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.intellij.platform) apply false
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.shadow) apply false
}

group = "com.mitteloupe.cag"
version = "1.0-SNAPSHOT"

subprojects {
    repositories {
        mavenCentral()
    }

    plugins.withId("org.jlleitschuh.gradle.ktlint") {
        extensions.configure(org.jlleitschuh.gradle.ktlint.KtlintExtension::class.java) {
            android.set(false)
            ignoreFailures.set(false)
            filter {
                exclude("**/build/**")
                include("**/*.kt")
                include("**/*.kts")
            }
        }
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

gradle.projectsEvaluated {
    project.installGitHookFromAutomation()
}
