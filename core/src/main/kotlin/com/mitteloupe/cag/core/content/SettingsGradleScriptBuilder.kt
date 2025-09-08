package com.mitteloupe.cag.core.content

fun buildSettingsGradleScript(
    projectName: String,
    featureNames: List<String>
): String {
    val featuresBlock =
        featureNames.joinToString(
            separator = "\n        "
        ) { featureName ->
            """
        setOf("ui", "presentation", "domain", "data").forEach { layer ->
           include("features:${featureName.lowercase()}:${'$'}layer")
        }
        """
        }
    return """
        enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
        
        pluginManagement {
            repositories {
                google()
                mavenCentral()
                gradlePluginPortal()
            }
        }
        dependencyResolutionManagement {
            repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
            repositories {
                google()
                mavenCentral()
                gradlePluginPortal()
            }
        }

        rootProject.name = "$projectName"
        include(":app")
        include(":coroutine")

        setOf(
            "ui",
            "instrumentation-test", 
            "presentation",
            "presentation-test",
            "domain"
        ).forEach { module ->
            include(":architecture:${'$'}module")
        }
        $featuresBlock
        setOf(
            "source",
            "implementation"
        ).forEach { module ->
            include(":datasource:${'$'}module")
        }
        """.trimIndent()
}
