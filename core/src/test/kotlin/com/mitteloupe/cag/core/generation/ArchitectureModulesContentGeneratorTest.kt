package com.mitteloupe.cag.core.generation

import com.mitteloupe.cag.core.GenerationException
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.File
import kotlin.io.path.createTempDirectory

class ArchitectureModulesContentGeneratorTest {
    private lateinit var classUnderTest: ArchitectureModulesContentGenerator
    private lateinit var tempDirectory: File

    @Before
    fun setUp() {
        classUnderTest = ArchitectureModulesContentGenerator()
        tempDirectory = createTempDirectory(prefix = "test").toFile()
    }

    @Test(expected = GenerationException::class)
    fun `Given empty architecture package name when generate then throws exception`() {
        // Given
        val architectureRoot = File(tempDirectory, "architecture").apply { mkdirs() }
        val architecturePackageName = ""
        val enableCompose = true

        // When
        classUnderTest.generate(architectureRoot, architecturePackageName, enableCompose)

        // Then throws GenerationException
    }

    @Test(expected = GenerationException::class)
    fun `Given invalid architecture package name when generate then throws exception`() {
        // Given
        val architectureRoot = File(tempDirectory, "architecture").apply { mkdirs() }
        val architecturePackageName = "..."
        val enableCompose = true

        // When
        classUnderTest.generate(architectureRoot, architecturePackageName, enableCompose)

        // Then throws GenerationException
    }

    @Test(expected = GenerationException::class)
    fun `Given architecture root that is a file when generate then throws exception`() {
        // Given
        val architectureRoot = File(tempDirectory, "architecture").apply { createNewFile() }
        val architecturePackageName = "com.example.architecture"
        val enableCompose = true

        // When
        classUnderTest.generate(architectureRoot, architecturePackageName, enableCompose)

        // Then throws GenerationException
    }

    @Test
    fun `Given valid architecture package when generate then creates UseCase interface with exact content`() {
        // Given
        val architectureRoot = File(tempDirectory, "architecture").apply { mkdirs() }
        val architecturePackageName = "com.example.architecture"
        val enableCompose = true

        // When
        classUnderTest.generate(architectureRoot, architecturePackageName, enableCompose)

        // Then
        val useCaseFile = File(architectureRoot, "domain/src/main/java/com/example/architecture/usecase/UseCase.kt")
        val expectedContent = """package com.example.architecture.domain.usecase

interface UseCase<REQUEST, RESULT> {
    fun execute(input: REQUEST, onResult: (RESULT) -> Unit)
}
"""
        assertEquals("UseCase.kt should have exact content", expectedContent, useCaseFile.readText())
    }

    @Test
    fun `Given valid architecture package when generate then creates Repository interface with exact content`() {
        // Given
        val architectureRoot = File(tempDirectory, "architecture").apply { mkdirs() }
        val architecturePackageName = "com.example.architecture"
        val enableCompose = true

        // When
        classUnderTest.generate(architectureRoot, architecturePackageName, enableCompose)

        // Then
        val repositoryFile = File(architectureRoot, "domain/src/main/java/com/example/architecture/repository/Repository.kt")
        val expectedContent = """package com.example.architecture.domain.repository

interface Repository"""
        assertEquals("Repository.kt should have exact content", expectedContent, repositoryFile.readText())
    }

    @Test
    fun `Given valid architecture package when generate then creates DomainException class with exact content`() {
        // Given
        val architectureRoot = File(tempDirectory, "architecture").apply { mkdirs() }
        val architecturePackageName = "com.example.architecture"
        val enableCompose = true

        // When
        classUnderTest.generate(architectureRoot, architecturePackageName, enableCompose)

        // Then
        val domainExceptionFile = File(architectureRoot, "domain/src/main/java/com/example/architecture/exception/DomainException.kt")
        val expectedContent = """package com.example.architecture.domain.exception

abstract class DomainException(cause: Throwable? = null) : Exception(cause)"""
        assertEquals("DomainException.kt should have exact content", expectedContent, domainExceptionFile.readText())
    }

    @Test
    fun `Given valid architecture package when generate then creates UnknownDomainException class with exact content`() {
        // Given
        val architectureRoot = File(tempDirectory, "architecture").apply { mkdirs() }
        val architecturePackageName = "com.example.architecture"
        val enableCompose = true

        // When
        classUnderTest.generate(architectureRoot, architecturePackageName, enableCompose)

        // Then
        val unknownDomainExceptionFile =
            File(architectureRoot, "domain/src/main/java/com/example/architecture/exception/UnknownDomainException.kt")
        val expectedContent = """package com.example.architecture.domain.exception

class UnknownDomainException(cause: Throwable? = null) : DomainException(cause)"""
        assertEquals("UnknownDomainException.kt should have exact content", expectedContent, unknownDomainExceptionFile.readText())
    }

    @Test
    fun `Given valid architecture package when generate then creates UseCaseExecutor class with exact content`() {
        // Given
        val architectureRoot = File(tempDirectory, "architecture").apply { mkdirs() }
        val architecturePackageName = "com.example.architecture"
        val enableCompose = true

        // When
        classUnderTest.generate(architectureRoot, architecturePackageName, enableCompose)

        // Then
        val useCaseExecutorFile = File(architectureRoot, "domain/src/main/java/com/example/architecture/UseCaseExecutor.kt")
        val expectedContent = """package com.example.architecture.domain

import com.example.architecture.domain.exception.DomainException
import com.example.architecture.domain.exception.UnknownDomainException
import com.example.architecture.domain.usecase.UseCase

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
        assertEquals("UseCaseExecutor.kt should have exact content", expectedContent, useCaseExecutorFile.readText())
    }

    @Test
    fun `Given valid architecture package when generate then creates UseCaseExecutorProvider typealias with exact content`() {
        // Given
        val architectureRoot = File(tempDirectory, "architecture").apply { mkdirs() }
        val architecturePackageName = "com.example.architecture"
        val enableCompose = true

        // When
        classUnderTest.generate(architectureRoot, architecturePackageName, enableCompose)

        // Then
        val useCaseExecutorProviderFile = File(architectureRoot, "domain/src/main/java/com/example/architecture/UseCaseExecutorProvider.kt")
        val expectedContent = """package com.example.architecture.domain

import kotlinx.coroutines.CoroutineScope

typealias UseCaseExecutorProvider =
    @JvmSuppressWildcards (coroutineScope: CoroutineScope) -> UseCaseExecutor
"""
        assertEquals("UseCaseExecutorProvider.kt should have exact content", expectedContent, useCaseExecutorProviderFile.readText())
    }

    @Test
    fun `Given valid architecture package when generate then creates BackgroundExecutingUseCase class with exact content`() {
        // Given
        val architectureRoot = File(tempDirectory, "architecture").apply { mkdirs() }
        val architecturePackageName = "com.example.architecture"
        val enableCompose = true

        // When
        classUnderTest.generate(architectureRoot, architecturePackageName, enableCompose)

        // Then
        val backgroundUseCaseFile =
            File(architectureRoot, "domain/src/main/java/com/example/architecture/usecase/BackgroundExecutingUseCase.kt")
        val expectedContent = """package com.example.architecture.domain.usecase

import com.example.architecture.coroutine.CoroutineContextProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
        assertEquals("BackgroundExecutingUseCase.kt should have exact content", expectedContent, backgroundUseCaseFile.readText())
    }

    @Test
    fun `Given valid architecture package when generate then creates ContinuousExecutingUseCase class with exact content`() {
        // Given
        val architectureRoot = File(tempDirectory, "architecture").apply { mkdirs() }
        val architecturePackageName = "com.example.architecture"
        val enableCompose = true

        // When
        classUnderTest.generate(architectureRoot, architecturePackageName, enableCompose)

        // Then
        val continuousUseCaseFile =
            File(architectureRoot, "domain/src/main/java/com/example/architecture/usecase/ContinuousExecutingUseCase.kt")
        val expectedContent = """package com.example.architecture.domain.usecase

import com.example.architecture.coroutine.CoroutineContextProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
        assertEquals("ContinuousExecutingUseCase.kt should have exact content", expectedContent, continuousUseCaseFile.readText())
    }

    @Test
    fun `Given valid architecture package when generate then creates BaseViewModel class with exact content`() {
        // Given
        val architectureRoot = File(tempDirectory, "architecture").apply { mkdirs() }
        val architecturePackageName = "com.example.architecture"
        val enableCompose = true

        // When
        classUnderTest.generate(architectureRoot, architecturePackageName, enableCompose)

        // Then
        val viewModelFile =
            File(architectureRoot, "presentation/src/main/java/com/example/architecture/presentation/viewmodel/BaseViewModel.kt")
        val expectedContent = """package com.example.architecture.presentation.viewmodel

import com.example.architecture.domain.UseCaseExecutor
import com.example.architecture.domain.exception.DomainException
import com.example.architecture.domain.usecase.UseCase
import com.example.architecture.presentation.navigation.PresentationNavigationEvent
import com.example.architecture.presentation.notification.PresentationNotification
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

abstract class BaseViewModel<VIEW_STATE : Any, NOTIFICATION : PresentationNotification>(
    private val useCaseExecutor: UseCaseExecutor
) {
    val viewState: Flow<VIEW_STATE>
        field = MutableSharedFlow()

    val notification: Flow<NOTIFICATION>
        field = MutableSharedFlow()

    val navigationEvent: Flow<PresentationNavigationEvent>
        field = MutableSharedFlow()

    protected fun updateViewState(newState: VIEW_STATE) {
        MainScope().launch {
            viewState.emit(newState)
        }
    }

    protected fun notify(notification: NOTIFICATION) {
        MainScope().launch {
            this@BaseViewModel.notification.emit(notification)
        }
    }

    protected fun emitNavigationEvent(navigationEvent: PresentationNavigationEvent) {
        MainScope().launch {
            this@BaseViewModel.navigationEvent.emit(navigationEvent)
        }
    }

    protected operator fun <OUTPUT> UseCase<Unit, OUTPUT>.invoke(
        onResult: (OUTPUT) -> Unit = {},
        onException: (DomainException) -> Unit = {}
    ) {
        useCaseExecutor.execute(this, onResult, onException)
    }

    protected operator fun <INPUT, OUTPUT> UseCase<INPUT, OUTPUT>.invoke(
        value: INPUT,
        onResult: (OUTPUT) -> Unit = {},
        onException: (DomainException) -> Unit = {}
    ) {
        useCaseExecutor.execute(this, value, onResult, onException)
    }
}
"""
        assertEquals("BaseViewModel.kt should have exact content", expectedContent, viewModelFile.readText())
    }

    @Test
    fun `Given valid architecture package when generate then creates PresentationNavigationEvent class with exact content`() {
        // Given
        val architectureRoot = File(tempDirectory, "architecture").apply { mkdirs() }
        val architecturePackageName = "com.example.architecture"
        val enableCompose = true

        // When
        classUnderTest.generate(architectureRoot, architecturePackageName, enableCompose)

        // Then
        val navigationEventFile =
            File(
                architectureRoot,
                "presentation/src/main/java/com/example/architecture/presentation/navigation/PresentationNavigationEvent.kt"
            )
        val expectedContent = """package com.example.architecture.presentation.navigation

sealed class PresentationNavigationEvent"""
        assertEquals("PresentationNavigationEvent.kt should have exact content", expectedContent, navigationEventFile.readText())
    }

    @Test
    fun `Given valid architecture package when generate then creates Screen interface with exact content`() {
        // Given
        val architectureRoot = File(tempDirectory, "architecture").apply { mkdirs() }
        val architecturePackageName = "com.example.architecture"
        val enableCompose = true

        // When
        classUnderTest.generate(architectureRoot, architecturePackageName, enableCompose)

        // Then
        val screenFile = File(architectureRoot, "ui/src/main/java/com/example/architecture/view/Screen.kt")
        val expectedContent = """package com.example.architecture.ui.view

import androidx.compose.runtime.Composable

interface Screen {
    @Composable
    fun Content()
}
"""
        assertEquals("Screen.kt should have exact content", expectedContent, screenFile.readText())
    }

    @Test
    fun `Given valid architecture package when generate then creates Mapper interface with exact content`() {
        // Given
        val architectureRoot = File(tempDirectory, "architecture").apply { mkdirs() }
        val architecturePackageName = "com.example.architecture"
        val enableCompose = true

        // When
        classUnderTest.generate(architectureRoot, architecturePackageName, enableCompose)

        // Then
        val mapperFile = File(architectureRoot, "ui/src/main/java/com/example/architecture/mapper/Mapper.kt")
        val expectedContent = """package com.example.architecture.ui.mapper

interface Mapper<Input, Output> {
    fun map(input: Input): Output
}
"""
        assertEquals("Mapper.kt should have exact content", expectedContent, mapperFile.readText())
    }
}
