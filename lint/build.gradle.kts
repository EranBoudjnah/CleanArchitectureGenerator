import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("java-library")
    alias(libs.plugins.kotlin.jvm)
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

repositories {
    google()
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))

    compileOnly(libs.lint.api)
    compileOnly(libs.lint.checks)

    testImplementation(libs.junit4)
}

tasks.register<Jar>("lintJar") {
    dependsOn(tasks.named("compileKotlin"))
    from(sourceSets.main.get().output)
    archiveBaseName.set("lint")
}
