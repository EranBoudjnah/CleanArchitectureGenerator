package com.mitteloupe.cag.core.generation.architecture

import com.mitteloupe.cag.core.generation.generateFileIfMissing
import com.mitteloupe.cag.core.generation.optimizeImports
import com.mitteloupe.cag.core.kotlinpackage.buildPackageDirectory
import java.io.File

class InstrumentationTestModuleCreator internal constructor() {
    fun generateInstrumentationTestContent(
        architectureRoot: File,
        architecturePackageName: String,
        architecturePackageNameSegments: List<String>
    ) {
        val codeRoot = File(architectureRoot, "src/main/java")
        val packageDirectory = buildPackageDirectory(codeRoot, architecturePackageNameSegments)

        generateBaseTest(packageDirectory, architecturePackageName)
        generateClickChildView(packageDirectory, architecturePackageName)
        generateServerRequestResponseAnnotation(packageDirectory, architecturePackageName)
        generateLocalStoreAnnotation(packageDirectory, architecturePackageName)
        generateItemAtPositionMatcher(packageDirectory, architecturePackageName)
        generateAssetReader(packageDirectory, architecturePackageName)
        generateAppNotRespondingHandler(packageDirectory, architecturePackageName)
        generateDoesNot(packageDirectory, architecturePackageName)
        generateRetry(packageDirectory, architecturePackageName)
    }

    private fun generateBaseTest(
        packageDirectory: File,
        architecturePackageName: String
    ) {
        val imports =
            """
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.annotation.SuppressLint
import android.content.SharedPreferences
import androidx.annotation.CallSuper
import androidx.compose.ui.test.IdlingResource as ComposeIdlingResource
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.IdlingResource as EspressoIdlingResource
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.uiautomator.UiDevice
import $architecturePackageName.test.idlingresource.findAndCloseAppNotRespondingDialog
import $architecturePackageName.test.idlingresource.registerAppNotRespondingWatcher
import $architecturePackageName.test.launcher.AppLauncher
import $architecturePackageName.test.localstore.KeyValueStore
import $architecturePackageName.test.rule.HiltInjectorRule
import $architecturePackageName.test.rule.LocalStoreRule
import $architecturePackageName.test.rule.ScreenshotFailureRule
import $architecturePackageName.test.rule.SdkAwareGrantPermissionRule
import $architecturePackageName.test.rule.WebServerRule
import $architecturePackageName.test.server.MockDispatcher
import $architecturePackageName.test.server.MockWebServerProvider
import $architecturePackageName.test.server.ResponseStore
import dagger.hilt.android.testing.HiltAndroidRule
import javax.inject.Inject
import org.junit.After
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.rules.RuleChain
""".optimizeImports()

        val content =
            """package $architecturePackageName.test.test

$imports
typealias TypedAndroidComposeTestRule<ACTIVITY> =
    AndroidComposeTestRule<ActivityScenarioRule<ACTIVITY>, ACTIVITY>

abstract class BaseTest {
    private val hiltAndroidRule by lazy { HiltAndroidRule(this) }

    @Inject
    lateinit var mockDispatcher: MockDispatcher

    @Inject
    lateinit var responseStore: ResponseStore

    @Inject
    lateinit var mockWebServerProvider: MockWebServerProvider

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    @Inject
    lateinit var keyValueStore: KeyValueStore

    @Inject
    lateinit var espressoIdlingResources: @JvmSuppressWildcards Collection<EspressoIdlingResource>

    @Inject
    lateinit var composeIdlingResources: @JvmSuppressWildcards Collection<ComposeIdlingResource>

    private val webServerRule = WebServerRule(
        lazy { mockDispatcher },
        lazy { responseStore }
    )

    private val localStoreRule = LocalStoreRule(
        lazy { sharedPreferences },
        lazy { keyValueStore }
    )

    private val idlingRegistry by lazy { IdlingRegistry.getInstance() }

    protected abstract val composeTestRule: ComposeContentTestRule

    @SuppressLint("UnsafeOptInUsageError")
    private val grantPermissionRule = SdkAwareGrantPermissionRule.grant(WRITE_EXTERNAL_STORAGE)

    @get:Rule
    val testRules: RuleChain by lazy {
        RuleChain
            .outerRule(hiltAndroidRule)
            .around(HiltInjectorRule(hiltAndroidRule))
            .around(ScreenshotFailureRule())
            .around(webServerRule)
            .around(localStoreRule)
            .around(composeTestRule)
            .around(grantPermissionRule)
    }

    protected abstract val startActivityLauncher: AppLauncher

    @Before
    fun setUp() {
        val deviceUi = UiDevice.getInstance(getInstrumentation())
        deviceUi.findAndCloseAppNotRespondingDialog()
        registerIdlingResources()
        startActivityLauncher.launch()
    }

    @After
    fun cleanUp() {
        unregisterIdlingResources()
    }

    private fun registerIdlingResources() {
        idlingRegistry.register(*(espressoIdlingResources).toTypedArray())
        composeIdlingResources.forEach(composeTestRule::registerIdlingResource)
    }

    private fun unregisterIdlingResources() {
        idlingRegistry.unregister(*(espressoIdlingResources).toTypedArray())
        composeIdlingResources.forEach(composeTestRule::unregisterIdlingResource)
    }

    companion object {
        @BeforeClass
        @CallSuper
        @JvmStatic
        fun setUpGlobally() {
            val deviceUi = UiDevice.getInstance(getInstrumentation())
            deviceUi.registerAppNotRespondingWatcher()
        }
    }
}
"""

        generateFileIfMissing(
            packageDirectory = packageDirectory,
            relativePath = "test/BaseTest.kt",
            content = content,
            errorMessage = "base test"
        )
    }

    private fun generateItemAtPositionMatcher(
        packageDirectory: File,
        architecturePackageName: String
    ) {
        val imports =
            """
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import junit.framework.AssertionFailedError
import org.hamcrest.Matcher
""".optimizeImports()

        val content =
            """package $architecturePackageName.test.assertion

$imports
fun matchesItemAtPosition(matcher: Matcher<View?>?, position: Int) =
    ViewAssertion { view, noViewFoundException ->
        if (noViewFoundException != null) {
            throw noViewFoundException
        }
        val recyclerView = view as RecyclerView
        val viewHolder = recyclerView.findViewHolderForAdapterPosition(position)
            ?: throw AssertionFailedError("No view holder at position: ${'$'}position")
        assertThat(viewHolder.itemView, matcher)
    }
"""

        generateFileIfMissing(
            packageDirectory = packageDirectory,
            relativePath = "assertion/ItemAtPositionMatcher.kt",
            content = content,
            errorMessage = "item at position matcher"
        )
    }

    private fun generateAssetReader(
        packageDirectory: File,
        architecturePackageName: String
    ) {
        val imports =
            """
import androidx.test.platform.app.InstrumentationRegistry
import java.io.InputStream
""".optimizeImports()

        val content =
            """package $architecturePackageName.test.asset

$imports
fun getAssetAsString(name: String): String =
    processAssetStream(name) { stream -> stream.bufferedReader().readText() }

fun <OUTPUT> processAssetStream(
    filename: String,
    performOnStream: (inputStream: InputStream) -> OUTPUT
): OUTPUT = InstrumentationRegistry.getInstrumentation().context.assets.open(filename)
    .use { stream -> performOnStream(stream) }
"""

        generateFileIfMissing(
            packageDirectory = packageDirectory,
            relativePath = "asset/AssetReader.kt",
            content = content,
            errorMessage = "asset reader"
        )
    }

    private fun generateAppNotRespondingHandler(
        packageDirectory: File,
        architecturePackageName: String
    ) {
        val imports =
            """
import android.util.Log
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject
import androidx.test.uiautomator.UiObjectNotFoundException
import androidx.test.uiautomator.UiSelector
""".optimizeImports()

        val content =
            """package $architecturePackageName.test.idlingresource

$imports
private const val APP_NOT_RESPONDING_TEXT = " isn't responding"
private const val APP_NOT_RESPONDING_TAG = "AppNotResponding"
fun UiDevice.registerAppNotRespondingWatcher() {
    registerWatcher("AppNotResponding") {
        findAndCloseAppNotRespondingDialog()
    }
    findAndCloseAppNotRespondingDialog()
}

private fun UiDevice.appNotRespondingDialog() = findObject(
    UiSelector()
        .packageName("android")
        .textContains(APP_NOT_RESPONDING_TEXT)
)

fun UiDevice.findAndCloseAppNotRespondingDialog() =
    appNotRespondingDialog().let { appNotRespondingDialog ->
        appNotRespondingDialog.exists()
            .also { dialogExists ->
                if (dialogExists) {
                    closeAnrWithWait(appNotRespondingDialog)
                }
            }
    }

private fun UiDevice.closeAnrWithWait(appNotRespondingDialog: UiObject) {
    Log.i(APP_NOT_RESPONDING_TAG, "App not responding (ANR) dialog detected.")
    try {
        findObject(
            UiSelector()
                .text("Wait")
                .className("android.widget.Button")
                .packageName("android")
        ).click()
        val dialogText = appNotRespondingDialog.text
        val appName = dialogText.take(dialogText.length - APP_NOT_RESPONDING_TEXT.length)
        Log.i(APP_NOT_RESPONDING_TAG, "App \"${'$'}appName\" is not responding. Pressed on wait.")
    } catch (uiObjectNotFoundException: UiObjectNotFoundException) {
        Log.i(APP_NOT_RESPONDING_TAG, "Detected app not responding dialog, but window disappeared.")
    }
}
"""

        generateFileIfMissing(
            packageDirectory = packageDirectory,
            relativePath = "idlingresource/AppNotRespondingHandler.kt",
            content = content,
            errorMessage = "app not responding handler"
        )
    }

    fun generateClickChildView(
        packageDirectory: File,
        architecturePackageName: String
    ) {
        val content =
            """package $architecturePackageName.test.action

${
                """
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.Matcher
""".optimizeImports() }
fun clickChildView(matcher: Matcher<View>) = ClickChildView(matcher)

class ClickChildView(private val matcher: Matcher<View>) : ViewAction {
    override fun getConstraints(): Matcher<View> = allOf(isDisplayed(), matcher)

    override fun getDescription() = "Click on a matching view"

    override fun perform(uiController: UiController, view: View) {
        clickOnMatchingView(view)
    }

    private fun clickOnMatchingView(view: View): Boolean {
        if (matcher.matches(view)) {
            view.performClick()
            return true
        }

        return if (view is ViewGroup) {
            view.children.iterator().asSequence().firstOrNull { childView ->
                clickOnMatchingView(childView)
            } != null
        } else {
            false
        }
    }
}
"""

        generateFileIfMissing(
            packageDirectory = packageDirectory,
            relativePath = "action/ClickChildView.kt",
            content = content,
            errorMessage = "click child view"
        )
    }

    fun generateServerRequestResponseAnnotation(
        packageDirectory: File,
        architecturePackageName: String
    ) {
        generateFileIfMissing(
            packageDirectory = packageDirectory,
            relativePath = "annotation/ServerRequestResponse.kt",
            content =
                """package $architecturePackageName.test.annotation

@Target(AnnotationTarget.FUNCTION)
annotation class ServerRequestResponse(val requestResponseIds: Array<String>)
""",
            errorMessage = "server request response annotation"
        )
    }

    fun generateLocalStoreAnnotation(
        packageDirectory: File,
        architecturePackageName: String
    ) {
        generateFileIfMissing(
            packageDirectory = packageDirectory,
            relativePath = "annotation/LocalStore.kt",
            content =
                """package $architecturePackageName.test.annotation

@Target(AnnotationTarget.FUNCTION)
annotation class LocalStore(val localStoreDataIds: Array<String>)
""",
            errorMessage = "local store annotation"
        )
    }

    fun generateDoesNot(
        packageDirectory: File,
        architecturePackageName: String
    ) {
        generateFileIfMissing(
            packageDirectory = packageDirectory,
            relativePath = "test/DoesNot.kt",
            content =
                """package $architecturePackageName.test.test

import junit.framework.AssertionFailedError

fun doesNot(description: String, block: () -> Unit) {
    try {
        block()
        error("Unexpected: ${'$'}description")
    } catch (_: AssertionFailedError) {
    }
}
""",
            errorMessage = "does not"
        )
    }

    fun generateRetry(
        packageDirectory: File,
        architecturePackageName: String
    ) {
        val imports =
            """
import junit.framework.AssertionFailedError
""".optimizeImports()

        val content =
            """package $architecturePackageName.test.test

$imports
fun retry(waitMilliseconds: Long = 200L, repeat: Int = 5, block: () -> Unit) {
    var lastExceptionMessage = ""
    repeat(repeat) {
        try {
            block()
            return
        } catch (exception: AssertionFailedError) {
            lastExceptionMessage = exception.message.orEmpty()
            Thread.sleep(waitMilliseconds)
        }
    }
    throw AssertionFailedError(lastExceptionMessage)
}
"""

        generateFileIfMissing(
            packageDirectory = packageDirectory,
            relativePath = "test/Retry.kt",
            content = content,
            errorMessage = "retry"
        )
    }
}
