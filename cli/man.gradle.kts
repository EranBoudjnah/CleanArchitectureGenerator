import org.gradle.api.DefaultTask
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.bundling.Jar
import org.gradle.process.ExecOperations
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.file.Paths
import java.util.zip.GZIPOutputStream
import javax.inject.Inject

abstract class GenerateManPage
    @Inject
    constructor() : DefaultTask() {
        @get:Inject
        protected abstract val execOperations: ExecOperations

        @get:Inject
        protected abstract val layout: ProjectLayout

        @get:OutputFile
        abstract val outputFile: RegularFileProperty

        @get:InputFile
        abstract val shadowJarFile: RegularFileProperty

        init {
            description = "Generates a man page at build/man/cag.1"
            group = "documentation"
            outputFile.convention(layout.buildDirectory.file("man/cag.1"))
        }

        @TaskAction
        fun generate() {
            val manFile = outputFile.get().asFile
            val manDirectory = manFile.parentFile
            if (!manDirectory.exists()) {
                manDirectory.mkdirs()
            }
            manFile.writeText("")

            execOperations.exec {
                commandLine(
                    "java",
                    "-jar",
                    shadowJarFile.get().asFile.absolutePath,
                    "--help",
                    "--format=man"
                )
                standardOutput = manFile.outputStream()
            }
        }
    }

val shadowJar = tasks.named("shadowJar", Jar::class.java)
tasks.register("generateManPage", GenerateManPage::class.java) {
    notCompatibleWithConfigurationCache("Exec-based stream redirection and script-defined task class can hold non-serializable references.")
    dependsOn(shadowJar)
    shadowJarFile.set(shadowJar.flatMap { it.archiveFile })
}

abstract class InstallManPage
    @Inject
    constructor() :
    DefaultTask() {
        @get:Inject
        protected abstract val layout: ProjectLayout

        @get:Inject
        protected abstract val providers: ProviderFactory

        @get:InputFile
        abstract val sourceFileProperty: RegularFileProperty

        @get:Input
        abstract val manInstallDirectory: Property<String>

        @get:OutputFile
        abstract val outputGzipFile: RegularFileProperty

        init {
            description = "Installs the man page cag.1.gz to the man1 directory."
            group = "documentation"

            manInstallDirectory.convention(providers.gradleProperty("manInstallDir").orElse(defaultDirectory()))

            outputGzipFile.convention(
                manInstallDirectory.flatMap { dir ->
                    layout.file(providers.provider { File(dir).resolve("cag.1.gz") })
                }
            )
        }

        private fun defaultDirectory(): String {
            val osName = System.getProperty("os.name").lowercase()
            val homePath = System.getProperty("user.home")
            val defaultDirectory =
                if (osName.contains("mac") || osName.contains("darwin")) {
                    val brewPrefix = brewPrefix()
                    if (brewPrefix != null) {
                        Paths.get(brewPrefix, "share", "man", "man1").toString()
                    } else {
                        Paths.get(homePath, "Library", "Man", "man1").toString()
                    }
                } else {
                    Paths.get(homePath, ".local", "share", "man", "man1").toString()
                }
            return defaultDirectory
        }

        @TaskAction
        fun install() {
            val sourceFile = sourceFileProperty.get().asFile
            require(sourceFile.exists()) { "Man page not found at $sourceFile. Run :cli:generateManPage first." }

            val chosenDirectory = manInstallDirectory.get()
            val homePath = System.getProperty("user.home")
            val resolvedDirectory =
                if (chosenDirectory.startsWith("~/")) {
                    File(Paths.get(homePath, chosenDirectory.removePrefix("~/")).toString())
                } else {
                    File(chosenDirectory)
                }
            val outputFile = File(resolvedDirectory, "cag.1.gz")
            val outputDirectory = outputFile.parentFile

            val osName = System.getProperty("os.name").lowercase()
            if ((osName.contains("mac") || osName.contains("darwin")) && outputDirectory.absolutePath.startsWith("/usr/local/")) {
                logger.warn(
                    "Target is under /usr/local; you may need elevated permissions (sudo) or " +
                        "choose a user directory like ~/Library/Man/man1"
                )
            }

            if (!outputDirectory.exists()) {
                check(outputDirectory.mkdirs()) { "Failed to create man directory: $outputDirectory" }
            }

            FileInputStream(sourceFile).use { fileInputStream ->
                FileOutputStream(outputFile).use { fileOutputStream ->
                    GZIPOutputStream(fileOutputStream).use { gzipOutputStream ->
                        fileInputStream.copyTo(gzipOutputStream)
                    }
                }
            }

            println("Installed man page: $outputFile")
            if (osName.contains("linux")) {
                println("You may need to update the man database (e.g., 'sudo mandb') on some systems.")
            }

            val sectionPattern = Regex("man[1-9]")
            val manRootDirectory =
                if (sectionPattern.matches(outputDirectory.name)) {
                    outputDirectory.parentFile ?: outputDirectory
                } else {
                    outputDirectory
                }
            println("Preview with: man -M \"${manRootDirectory.absolutePath}\" cag")

            val isMac = osName.let { it.contains("mac") || it.contains("darwin") }
            if (isMac) {
                val suggestedRoot = Paths.get(homePath, "Library", "Man").toString()
                println("To make it permanent on zsh, add this line to ~/.zshrc then 'source ~/.zshrc':")
                println("  export MANPATH=\"$suggestedRoot:$(manpath 2>/dev/null)\"")
                val brewPrefix = brewPrefix()
                if (brewPrefix != null) {
                    val brewMan1Path = Paths.get(brewPrefix, "share", "man", "man1").toString()
                    println("Alternatively, install system-wide (may require sudo):")
                    println("  ./gradlew :cli:installManPage -PmanInstallDir=$brewMan1Path")
                }
            }
        }

        private fun brewPrefix(): String? {
            val brewEnv = System.getenv("HOMEBREW_PREFIX")
            val prefix =
                when {
                    !brewEnv.isNullOrBlank() -> brewEnv
                    File("/opt/homebrew").exists() -> "/opt/homebrew"
                    File("/usr/local").exists() -> "/usr/local"
                    else -> null
                }
            return prefix
        }
    }

tasks.register("installManPage", InstallManPage::class.java) {
    dependsOn("generateManPage")
    sourceFileProperty.set(layout.buildDirectory.file("man/cag.1"))
}
