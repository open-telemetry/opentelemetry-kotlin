package io.opentelemetry.kotlin

import io.opentelemetry.kotlin.aliases.OtelJavaContext
import io.opentelemetry.kotlin.aliases.OtelJavaOpenTelemetry
import io.opentelemetry.kotlin.aliases.OtelJavaOpenTelemetrySdk
import io.opentelemetry.kotlin.aliases.OtelJavaSdkTracerProvider
import io.opentelemetry.kotlin.behavior.OpenTelemetryBehavior
import io.opentelemetry.kotlin.config.DeclarativeBehaviorReader
import io.opentelemetry.kotlin.context.CurrentContextStorage
import io.opentelemetry.kotlin.context.OtelJavaCurrentContextStorage
import io.opentelemetry.kotlin.context.TempContextStandInApi
import io.opentelemetry.kotlin.context.createContextKey
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
import io.opentelemetry.kotlin.tracing.model.OtelJavaSpanAdapter
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotSame
import kotlin.test.assertSame
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class, TempContextStandInApi::class)
internal class CompatProcessWideStateTest {

    private val sdks = SdkTracker()
    private var preinstalled: CurrentContextStorage? = null

    @AfterTest
    fun tearDown() {
        sdks.shutdownAll()
        preinstalled?.let(::internalUninstallContextStorage)
    }

    @Test
    fun `root-derived context attached from kotlin parents a java span`() {
        val otel = createCompatOpenTelemetry().also(sdks::track)
        val parent = otel.tracerProvider.getTracer("kotlin").startSpan("parent")
        val scope = internalRootContext().storeSpan(parent).attach()
        val child = try {
            otel.toOtelJavaApi().tracerProvider.get("java").spanBuilder("child").startSpan()
        } finally {
            scope.detach()
            parent.end()
        }
        assertEquals(parent.spanContext.traceId, child.spanContext.traceId)
        assertEquals(parent.spanContext.spanId, (child as OtelJavaSpanAdapter).span.parent.spanId)
        assertSame(OtelJavaContext.root(), OtelJavaContext.current())
    }

    @Test
    fun `createCompatOpenTelemetry claims and releases process-wide state`() = runTest {
        assertClaimsAndReleases(createCompatOpenTelemetry().also(sdks::track))
    }

    @Test
    fun `toOtelKotlinApi claims and releases process-wide state`() = runTest {
        assertClaimsAndReleases(OtelJavaOpenTelemetry.noop().toOtelKotlinApi().also(sdks::track))
    }

    @Test
    fun `toOtelKotlinApi shares the java current context`() {
        val javaSdk = OtelJavaOpenTelemetrySdk.builder()
            .setTracerProvider(OtelJavaSdkTracerProvider.builder().build())
            .build()
        javaSdk.toOtelKotlinApi().also(sdks::track)
        val key = createContextKey<String>("key")
        val ctx = internalRootContext().set(key, "value")
        val scope = ctx.attach()
        try {
            assertSame(ctx, internalCurrentContext())
            assertNotSame(OtelJavaContext.root(), OtelJavaContext.current())
        } finally {
            scope.detach()
        }
        assertSame(OtelJavaContext.root(), OtelJavaContext.current())
    }

    @Test
    fun `reportError reaches the compat handler until shutdown`() = runTest {
        val errorHandler = FakeSdkErrorHandler()
        val otel = createCompatOpenTelemetry { errorHandler(errorHandler) }.also(sdks::track)
        val error = SdkError.ApiMisuse("api", "message", SdkErrorSeverity.WARNING)

        reportError(error)
        assertSame(error, errorHandler.errors.last())

        otel.shutdown()
        val count = errorHandler.errors.size
        reportError(error)
        assertEquals(count, errorHandler.errors.size)
        setProcessSdkErrorHandler(null)
    }

    @Test
    fun `repeated shutdown does not release state claimed by a later SDK`() = runTest {
        val first = createCompatOpenTelemetry().also(sdks::track)
        first.shutdown()
        val errorHandler = FakeSdkErrorHandler()
        createCompatOpenTelemetry { errorHandler(errorHandler) }.also(sdks::track)

        first.shutdown()

        assertFalse(internalInstallContextStorage(OtelJavaCurrentContextStorage()))
        val error = SdkError.ApiMisuse("api", "message", SdkErrorSeverity.WARNING)
        reportError(error)
        assertSame(error, errorHandler.errors.last())
    }

    @Test
    fun `taken slot warns and uses the installed storage`() {
        val storage = OtelJavaCurrentContextStorage().also { preinstalled = it }
        assertTrue(internalInstallContextStorage(storage))

        val errorHandler = FakeSdkErrorHandler()
        createCompatOpenTelemetry { errorHandler(errorHandler) }.also(sdks::track)
        val error = errorHandler.apiMisuses.single()
        assertEquals("createCompatOpenTelemetry", error.api)
        assertEquals(SdkErrorSeverity.WARNING, error.severity)
    }

    @Test
    fun `disabled compat SDK claims nothing`() {
        val otel = createCompatOpenTelemetry {
            configFile(DeclarativeBehaviorReader { OpenTelemetryBehavior(disabled = true) }, "otel.yaml")
        }
        assertSame(NoopOpenTelemetrySdk, otel)
        assertSlotAndSinkFree()
    }

    private suspend fun assertClaimsAndReleases(otel: OpenTelemetrySdk) {
        val other = createCompatOpenTelemetry().also(sdks::track)
        other.shutdown()
        assertFalse(internalInstallContextStorage(OtelJavaCurrentContextStorage()))
        assertFalse(setProcessSdkErrorHandler(FakeSdkErrorHandler()))

        otel.shutdown()
        assertSlotAndSinkFree()
    }

    private fun assertSlotAndSinkFree() {
        val storage = OtelJavaCurrentContextStorage()
        assertTrue(internalInstallContextStorage(storage))
        assertTrue(internalUninstallContextStorage(storage))
        assertTrue(setProcessSdkErrorHandler(FakeSdkErrorHandler()))
        setProcessSdkErrorHandler(null)
    }
}
