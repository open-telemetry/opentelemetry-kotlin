package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import io.opentelemetry.kotlin.error.setProcessSdkErrorHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * Runs [asCoroutineContextElement] against the process-wide storage wrapper that reports misuse.
 * kotlinx.coroutines re-attaches a context that is already current on undispatched `withContext`
 * calls, which is allowed and must not be reported.
 */
@OptIn(ExperimentalApi::class)
internal class CoroutineMisuseReportingTest {

    private val storage = createThreadLocalCurrentContextStorage()
    private val errorHandler = FakeSdkErrorHandler()

    @BeforeTest
    fun setUp() {
        assertTrue(installContextStorage(storage))
        assertTrue(setProcessSdkErrorHandler(errorHandler))
    }

    @AfterTest
    fun tearDown() {
        setProcessSdkErrorHandler(null)
        uninstallContextStorage(storage)
    }

    @Test
    fun testUndispatchedWithContextReportsNothing() = runBlocking {
        val ctx = rootContext().set(createContextKey("key"), "value")
        withContext(ctx.asCoroutineContextElement()) {
            withContext(NonCancellable) {
                assertSame(ctx, currentContext())
            }
            withContext(CoroutineName("name")) {
                assertSame(ctx, currentContext())
            }
            assertSame(ctx, currentContext())
        }
        assertSame(rootContext(), currentContext())
        assertTrue(errorHandler.errors.isEmpty(), "unexpected reports: ${errorHandler.errors}")
    }
}
