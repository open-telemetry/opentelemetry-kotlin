package io.opentelemetry.kotlin

import io.opentelemetry.kotlin.context.CurrentContextStorage
import io.opentelemetry.kotlin.context.TempContextStandInApi
import io.opentelemetry.kotlin.context.createSingleSlotCurrentContextStorage
import io.opentelemetry.kotlin.context.internalCurrentContext
import io.opentelemetry.kotlin.context.internalInstallContextStorage
import io.opentelemetry.kotlin.context.internalRootContext
import io.opentelemetry.kotlin.context.internalUninstallContextStorage
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

@OptIn(ExperimentalApi::class, TempContextStandInApi::class)
internal class ProcessWideContextInstallationTest {

    private val sdks = SdkTracker()
    private var preinstalled: CurrentContextStorage? = null

    @AfterTest
    fun tearDown() {
        sdks.shutdownAll()
        preinstalled?.let(::internalUninstallContextStorage)
    }

    @Test
    fun testCurrentContextReflectsAttachAfterInit() {
        createOpenTelemetry().also(sdks::track)
        val ctx = internalRootContext().storeSpan(FakeSpan())
        val scope = ctx.attach()
        try {
            assertSame(ctx, internalCurrentContext())
        } finally {
            scope.detach()
        }
        assertSame(internalRootContext(), internalCurrentContext())
    }

    @Test
    fun testSpanWithoutParentUsesAttachedContext() {
        val otel = createOpenTelemetry().also(sdks::track)
        val tracer = otel.tracerProvider.getTracer("test")
        val parent = tracer.startSpan("parent")
        val scope = internalRootContext().storeSpan(parent).attach()
        try {
            val child = tracer.startSpan("child")
            assertEquals(parent.spanContext.traceId, child.spanContext.traceId)
            assertSame(parent, otel.context.current().extractSpan())
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
            context { storage = createSingleSlotCurrentContextStorage() }
        }.also(sdks::track)

        val error = errorHandler.apiMisuses.single()
        assertEquals("context", error.api)
        assertEquals(SdkErrorSeverity.WARNING, error.severity)

        val ctx = second.context.root().storeSpan(FakeSpan())
        val scope = ctx.attach()
        try {
            assertSame(ctx, first.context.current())
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
        assertTrue(internalInstallContextStorage(storage))

        val errorHandler = FakeSdkErrorHandler()
        val otel = createOpenTelemetry { errorHandler(errorHandler) }.also(sdks::track)
        val ctx = otel.context.root().storeSpan(FakeSpan())
        val scope = ctx.attach()
        try {
            assertSame(ctx, storage.current())
        } finally {
            scope.detach()
        }
        assertFalse(errorHandler.hasErrors())

        otel.shutdown()
        assertFalse(internalInstallContextStorage(createSingleSlotCurrentContextStorage()))
    }

    @Test
    fun testOutOfOrderDetachReachesOwningSdkHandler() {
        val errorHandler = FakeSdkErrorHandler()
        val otel = createOpenTelemetry { errorHandler(errorHandler) }.also(sdks::track)
        val outer = otel.context.root().storeSpan(FakeSpan()).attach()
        val inner = otel.context.current().storeSpan(FakeSpan()).attach()

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
        assertFalse(internalInstallContextStorage(createSingleSlotCurrentContextStorage()))
        assertFalse(setProcessSdkErrorHandler(FakeSdkErrorHandler()))
    }

    private fun assertSlotAndSinkFree() {
        val storage = createSingleSlotCurrentContextStorage()
        assertTrue(internalInstallContextStorage(storage))
        assertTrue(internalUninstallContextStorage(storage))
        assertTrue(setProcessSdkErrorHandler(FakeSdkErrorHandler()))
        setProcessSdkErrorHandler(null)
    }
}
