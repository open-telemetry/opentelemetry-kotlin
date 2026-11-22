package io.opentelemetry.kotlin

import io.opentelemetry.kotlin.context.CurrentContextStorage
import io.opentelemetry.kotlin.context.createSingleSlotCurrentContextStorage
import io.opentelemetry.kotlin.context.currentContext
import io.opentelemetry.kotlin.context.installContextStorage
import io.opentelemetry.kotlin.context.rootContext
import io.opentelemetry.kotlin.context.uninstallContextStorage
import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import io.opentelemetry.kotlin.error.SdkError
import io.opentelemetry.kotlin.error.SdkErrorSeverity
import io.opentelemetry.kotlin.error.reportError
import io.opentelemetry.kotlin.error.setProcessSdkErrorHandler
import io.opentelemetry.kotlin.framework.SdkTracker
import io.opentelemetry.kotlin.tracing.FakeSpan
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
internal class ProcessWideContextInstallationTest {

    private val sdks = SdkTracker()
    private var preinstalled: CurrentContextStorage? = null

    @AfterTest
    fun tearDown() {
        sdks.shutdownAll()
        preinstalled?.let(::uninstallContextStorage)
    }

    @Test
    fun testCurrentContextReflectsAttachAfterInit() {
        createOpenTelemetry().also(sdks::track)
        val ctx = rootContext().storeSpan(FakeSpan())
        val scope = ctx.attach()
        try {
            assertSame(ctx, currentContext())
        } finally {
            scope.detach()
        }
        assertSame(rootContext(), currentContext())
    }

    @Test
    fun testSpanWithoutParentUsesAttachedContext() {
        val otel = createOpenTelemetry().also(sdks::track)
        val tracer = otel.tracerProvider.getTracer("test")
        val parent = tracer.startSpan("parent")
        val scope = rootContext().storeSpan(parent).attach()
        try {
            val child = tracer.startSpan("child")
            assertEquals(parent.spanContext.traceId, child.spanContext.traceId)
            assertSame(parent, currentContext().extractSpan())
        } finally {
            scope.detach()
        }
    }

    @Test
    fun testSecondConfiguredSdkWarnsAndSharesCurrentContext() {
        val first = createOpenTelemetry().also(sdks::track)
        val errorHandler = FakeSdkErrorHandler()
        val second = createOpenTelemetry {
            errorHandler(errorHandler)
            context { storage { createSingleSlotCurrentContextStorage() } }
        }.also(sdks::track)

        val error = errorHandler.apiMisuses.single()
        assertEquals("context", error.api)
        assertEquals(SdkErrorSeverity.WARNING, error.severity)

        val parent = second.tracerProvider.getTracer("test").startSpan("parent")
        val scope = rootContext().storeSpan(parent).attach()
        try {
            val child = first.tracerProvider.getTracer("test").startSpan("child")
            assertEquals(parent.spanContext.traceId, child.spanContext.traceId)
        } finally {
            scope.detach()
        }
    }

    @Test
    fun testSecondDefaultSdkDoesNotWarn() {
        createOpenTelemetry().also(sdks::track)
        val errorHandler = FakeSdkErrorHandler()
        createOpenTelemetry { errorHandler(errorHandler) }.also(sdks::track)
        assertFalse(errorHandler.hasErrors())
    }

    @Test
    fun testOwnerShutdownReleasesSlotAndErrorSink() = runTest {
        val owner = createOpenTelemetry().also(sdks::track)
        val other = createOpenTelemetry().also(sdks::track)

        other.shutdown()
        assertSlotAndSinkTaken()

        owner.shutdown()
        assertSlotAndSinkFree()
    }

    @Test
    fun testPreinstalledStorageWinsSilently() = runTest {
        val storage = createSingleSlotCurrentContextStorage().also { preinstalled = it }
        assertTrue(installContextStorage(storage))

        val errorHandler = FakeSdkErrorHandler()
        val otel = createOpenTelemetry { errorHandler(errorHandler) }.also(sdks::track)
        val ctx = rootContext().storeSpan(FakeSpan())
        val scope = ctx.attach()
        try {
            assertSame(ctx, storage.current())
        } finally {
            scope.detach()
        }
        assertFalse(errorHandler.hasErrors())

        otel.shutdown()
        assertFalse(installContextStorage(createSingleSlotCurrentContextStorage()))
    }

    @Test
    fun testOutOfOrderDetachReachesOwningSdkHandler() {
        val errorHandler = FakeSdkErrorHandler()
        val otel = createOpenTelemetry { errorHandler(errorHandler) }.also(sdks::track)
        val outer = rootContext().storeSpan(FakeSpan()).attach()
        val inner = currentContext().storeSpan(FakeSpan()).attach()

        outer.detach()
        inner.detach()

        val error = errorHandler.apiMisuses.first()
        assertEquals("Scope.detach", error.api)
    }

    @Test
    fun testReportErrorReachesSdkHandler() {
        val errorHandler = FakeSdkErrorHandler()
        createOpenTelemetry { errorHandler(errorHandler) }.also(sdks::track)
        val error = SdkError.ApiMisuse("api", "message", SdkErrorSeverity.WARNING)

        reportError(error)

        assertSame(error, errorHandler.errors.last())
    }

    private fun assertSlotAndSinkTaken() {
        assertFalse(installContextStorage(createSingleSlotCurrentContextStorage()))
        assertFalse(setProcessSdkErrorHandler(FakeSdkErrorHandler()))
    }

    private fun assertSlotAndSinkFree() {
        val storage = createSingleSlotCurrentContextStorage()
        assertTrue(installContextStorage(storage))
        assertTrue(uninstallContextStorage(storage))
        assertTrue(setProcessSdkErrorHandler(FakeSdkErrorHandler()))
        setProcessSdkErrorHandler(null)
    }
}
