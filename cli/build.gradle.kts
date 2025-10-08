plugins {
    alias(libs.plugins.kotlin.jvm)
    id("application")
    alias(libs.plugins.shadow)
    alias(libs.plugins.ktlint)
}

group = "com.mitteloupe.cag"
version = "0.3.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":core"))
    implementation(project(":git"))
    implementation(kotlin("stdlib"))
    testImplementation(libs.mockk)
    testImplementation(libs.junit4)
}

var terminalCommand = "cag"

application {
    applicationName = terminalCommand
    mainClass.set("com.mitteloupe.cag.cli.MainKt")
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
    }
}

tasks {
    jar {
        manifest {
            attributes(
                "Implementation-Title" to project.name,
                "Implementation-Version" to project.version
            )
        }
        archiveBaseName.set(terminalCommand)
    }

    shadowJar {
        archiveBaseName.set(terminalCommand)
        archiveClassifier.set("all")
        mergeServiceFiles()
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

apply(from = "man.gradle.kts")
