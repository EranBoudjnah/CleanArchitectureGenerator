package com.mitteloupe.cag.core.generation.architecture

import com.mitteloupe.cag.core.generation.generateFileIfMissing
import com.mitteloupe.cag.core.generation.optimizeImports
import com.mitteloupe.cag.core.kotlinpackage.buildPackageDirectory
import java.io.File

class DomainModuleCreator internal constructor() {
    fun generateDomainContent(
        architectureRoot: File,
        moduleNamespace: String,
        architecturePackageNameSegments: List<String>
    ) {
        val codeRoot = File(architectureRoot, "src/main/java")
        val packageDirectory = buildPackageDirectory(codeRoot, architecturePackageNameSegments)

        generateUseCase(packageDirectory, moduleNamespace)
        generateBackgroundExecutingUseCase(packageDirectory, moduleNamespace)
        generateContinuousExecutingUseCase(packageDirectory, moduleNamespace)
        generateDomainException(packageDirectory, moduleNamespace)
        generateUnknownDomainException(packageDirectory, moduleNamespace)
        generateUseCaseExecutor(packageDirectory, moduleNamespace)
        generateUseCaseExecutorProvider(packageDirectory, moduleNamespace)
    }

    private fun generateUseCase(
        packageDirectory: File,
        moduleNamespace: String
    ) {
        generateFileIfMissing(
            packageDirectory = packageDirectory,
            relativePath = "usecase/UseCase.kt",
            content =
                """
                package $moduleNamespace.domain.usecase

                interface UseCase<REQUEST, RESULT> {
                    fun execute(input: REQUEST, onResult: (RESULT) -> Unit)
                }

                """.trimIndent(),
            errorMessage = "use case"
        )
    }

    private fun generateBackgroundExecutingUseCase(
        packageDirectory: File,
        moduleNamespace: String
    ) {
        val imports =
            """
import ${moduleNamespace.substringBeforeLast('.')}.coroutine.CoroutineContextProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
""".optimizeImports()

        val content =
            """package $moduleNamespace.domain.usecase

$imports
abstract class BackgroundExecutingUseCase<REQUEST, RESULT>(
    private val coroutineContextProvider: CoroutineContextProvider,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) : UseCase<REQUEST, RESULT> {
    final override fun execute(input: REQUEST, onResult: (RESULT) -> Unit) {
        coroutineScope.launch {
            val result = withContext(coroutineContextProvider.io) {
                executeInBackground(input)
            }
            onResult(result)
        }
    }

    abstract fun executeInBackground(request: REQUEST): RESULT
}
"""

        generateFileIfMissing(
            packageDirectory = packageDirectory,
            relativePath = "usecase/BackgroundExecutingUseCase.kt",
            content = content,
            errorMessage = "background executing use case"
        )
    }

    private fun generateContinuousExecutingUseCase(
        packageDirectory: File,
        moduleNamespace: String
    ) {
        val imports =
            """
import ${moduleNamespace.substringBeforeLast('.')}.coroutine.CoroutineContextProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
""".optimizeImports()

        val content =
            """package $moduleNamespace.domain.usecase

$imports
abstract class ContinuousExecutingUseCase<REQUEST, RESULT>(
    private val coroutineContextProvider: CoroutineContextProvider,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) : UseCase<REQUEST, RESULT> {
    final override fun execute(input: REQUEST, onResult: (RESULT) -> Unit) {
        try {
            coroutineScope.launch {
                withContext(coroutineContextProvider.io) {
                    executeInBackground(input).collect { result ->
                        withContext(coroutineContextProvider.main) {
                            onResult(result)
                        }
                    }
                }
            }
        } catch (_: CancellationException) {
        }
    }

    abstract fun executeInBackground(request: REQUEST): Flow<RESULT>
}
"""

        generateFileIfMissing(
            packageDirectory = packageDirectory,
            relativePath = "usecase/ContinuousExecutingUseCase.kt",
            content = content,
            errorMessage = "continuous executing use case"
        )
    }

    private fun generateUseCaseExecutor(
        packageDirectory: File,
        moduleNamespace: String
    ) {
        val content =
            """package $moduleNamespace.domain

${
                """
import $moduleNamespace.domain.exception.DomainException
import $moduleNamespace.domain.exception.UnknownDomainException
import $moduleNamespace.domain.usecase.UseCase
""".optimizeImports()
            }
class UseCaseExecutor {
    fun <OUTPUT> execute(
        useCase: UseCase<Unit, OUTPUT>,
        onResult: (OUTPUT) -> Unit = {},
        onException: (DomainException) -> Unit = {}
    ) = execute(useCase, Unit, onResult, onException)

    fun <INPUT, OUTPUT> execute(
        useCase: UseCase<INPUT, OUTPUT>,
        value: INPUT,
        onResult: (OUTPUT) -> Unit = {},
        onException: (DomainException) -> Unit = {}
    ) {
        try {
            useCase.execute(value, onResult)
        } catch (@Suppress("TooGenericExceptionCaught") throwable: Throwable) {
            val domainException =
                ((throwable as? DomainException) ?: UnknownDomainException(throwable))
            onException(domainException)
        }
    }
}
"""

        generateFileIfMissing(
            packageDirectory = packageDirectory,
            relativePath = "UseCaseExecutor.kt",
            content = content,
            errorMessage = "use case executor"
        )
    }

    private fun generateUseCaseExecutorProvider(
        packageDirectory: File,
        moduleNamespace: String
    ) {
        val content =
            """package $moduleNamespace.domain

import kotlinx.coroutines.CoroutineScope

typealias UseCaseExecutorProvider =
    @JvmSuppressWildcards (coroutineScope: CoroutineScope) -> UseCaseExecutor
"""

        generateFileIfMissing(
            packageDirectory = packageDirectory,
            relativePath = "UseCaseExecutorProvider.kt",
            content = content,
            errorMessage = "use case executor provider"
        )
    }

    private fun generateDomainException(
        packageDirectory: File,
        moduleNamespace: String
    ) {
        generateFileIfMissing(
            packageDirectory = packageDirectory,
            relativePath = "exception/DomainException.kt",
            content =
                """
                package $moduleNamespace.domain.exception

                abstract class DomainException(cause: Throwable? = null) : Exception(cause)

                """.trimIndent(),
            errorMessage = "domain exception"
        )
    }

    private fun generateUnknownDomainException(
        packageDirectory: File,
        moduleNamespace: String
    ) {
        generateFileIfMissing(
            packageDirectory = packageDirectory,
            relativePath = "exception/UnknownDomainException.kt",
            content =
                """
                package $moduleNamespace.domain.exception
                
                class UnknownDomainException(cause: Throwable? = null) : DomainException(cause)
                
                """.trimIndent(),
            errorMessage = "unknown domain exception"
        )
    }
}
