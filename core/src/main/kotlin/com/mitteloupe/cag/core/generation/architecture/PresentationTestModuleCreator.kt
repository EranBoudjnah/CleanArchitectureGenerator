package com.mitteloupe.cag.core.generation.architecture

import com.mitteloupe.cag.core.generation.generateFileIfMissing
import com.mitteloupe.cag.core.generation.optimizeImports
import com.mitteloupe.cag.core.kotlinpackage.buildPackageDirectory
import java.io.File

class PresentationTestModuleCreator internal constructor() {
    fun generatePresentationTestContent(
        architectureRoot: File,
        architecturePackageName: String,
        architecturePackageNameSegments: List<String>
    ) {
        val codeRoot = File(architectureRoot, "src/main/java")
        val packageDirectory = buildPackageDirectory(codeRoot, architecturePackageNameSegments)

        generateBaseViewModelTest(packageDirectory, architecturePackageName)
    }

    private fun generateBaseViewModelTest(
        packageDirectory: File,
        architecturePackageName: String
    ) {
        val imports =
            """
import $architecturePackageName.domain.UseCaseExecutor
import $architecturePackageName.domain.exception.DomainException
import $architecturePackageName.domain.usecase.UseCase
import $architecturePackageName.presentation.notification.PresentationNotification
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.mockito.BDDMockito.willAnswer
import org.mockito.Mock
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.stubbing.Answer
""".optimizeImports()

        val content =
            """package $architecturePackageName.presentation.viewmodel

$imports
private const val NO_INPUT_ON_RESULT_ARGUMENT_INDEX = 1
private const val NO_INPUT_ON_EXCEPTION_ARGUMENT_INDEX = 2
private const val ON_RESULT_ARGUMENT_INDEX = 2
private const val ON_EXCEPTION_ARGUMENT_INDEX = 3

abstract class BaseViewModelTest<
    VIEW_STATE : Any,
    NOTIFICATION : PresentationNotification,
    VIEW_MODEL : BaseViewModel<VIEW_STATE, NOTIFICATION>
    > {
    private val testScheduler = TestCoroutineScheduler()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val testDispatcher = UnconfinedTestDispatcher(testScheduler)

    protected lateinit var classUnderTest: VIEW_MODEL

    @Mock
    protected lateinit var useCaseExecutor: UseCaseExecutor

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun coroutineSetUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @After
    fun coroutineTearDown() {
        Dispatchers.resetMain()
    }

    protected fun UseCase<Unit, *>.givenFailedExecution(domainException: DomainException) {
        givenExecutionWillAnswer { invocation ->
            val onException: (DomainException) -> Unit =
                invocation.getArgument(NO_INPUT_ON_EXCEPTION_ARGUMENT_INDEX)
            onException(domainException)
        }
    }

    protected fun <REQUEST> UseCase<REQUEST, *>.givenFailedExecution(
        input: REQUEST,
        domainException: DomainException
    ) {
        givenExecutionWillAnswer(input) { invocation ->
            val onException: (DomainException) -> Unit =
                invocation.getArgument(ON_EXCEPTION_ARGUMENT_INDEX)
            onException(domainException)
        }
    }

    protected fun <REQUEST, RESULT> UseCase<REQUEST, RESULT>.givenSuccessfulExecution(
        input: REQUEST,
        result: RESULT
    ) {
        givenExecutionWillAnswer(input) { invocation ->
            val onResult: (RESULT) -> Unit = invocation.getArgument(ON_RESULT_ARGUMENT_INDEX)
            onResult(result)
        }
    }

    protected fun <REQUEST> UseCase<REQUEST, Unit>.givenSuccessfulNoResultExecution(
        input: REQUEST
    ) {
        givenSuccessfulExecution(input, Unit)
    }

    protected fun <RESULT> UseCase<Unit, RESULT>.givenSuccessfulExecution(result: RESULT) {
        givenExecutionWillAnswer { invocationOnMock ->
            val onResult: (RESULT) -> Unit =
                invocationOnMock.getArgument(NO_INPUT_ON_RESULT_ARGUMENT_INDEX)
            onResult(result)
        }
    }

    protected fun UseCase<Unit, Unit>.givenSuccessfulNoArgumentNoResultExecution() {
        givenExecutionWillAnswer { invocationOnMock ->
            val onResult: (Unit) -> Unit = invocationOnMock.getArgument(ON_RESULT_ARGUMENT_INDEX)
            onResult(Unit)
        }
    }

    private fun <RESULT> UseCase<Unit, RESULT>.givenExecutionWillAnswer(answer: Answer<*>) {
        willAnswer(answer).given(useCaseExecutor).execute(
            useCase = eq(this@givenExecutionWillAnswer),
            onResult = any(),
            onException = any()
        )
    }

    private fun <REQUEST, RESULT> UseCase<REQUEST, RESULT>.givenExecutionWillAnswer(
        input: REQUEST,
        answer: Answer<*>
    ) {
        willAnswer(answer).given(useCaseExecutor).execute(
            useCase = eq(this@givenExecutionWillAnswer),
            value = eq(input),
            onResult = any(),
            onException = any()
        )
    }
}
"""

        generateFileIfMissing(
            packageDirectory = packageDirectory,
            relativePath = "viewmodel/BaseViewModelTest.kt",
            content = content,
            errorMessage = "base view model test"
        )
    }
}
