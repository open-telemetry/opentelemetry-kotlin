package io.opentelemetry.kotlin

import io.opentelemetry.kotlin.aliases.OtelJavaOpenTelemetry
import io.opentelemetry.kotlin.aliases.OtelJavaOpenTelemetrySdk
import io.opentelemetry.kotlin.aliases.OtelJavaSdkLoggerProvider
import io.opentelemetry.kotlin.aliases.OtelJavaSdkTracerProvider
import io.opentelemetry.kotlin.export.OperationResultCode.Success
import io.opentelemetry.kotlin.fakes.otel.java.FakeOtelJavaLogRecordExporter
import io.opentelemetry.kotlin.fakes.otel.java.FakeOtelJavaSpanExporter
import io.opentelemetry.sdk.logs.export.BatchLogRecordProcessor
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor
import kotlinx.coroutines.runBlocking
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Verifies that flush/shutdown on an [OpenTelemetrySdk] created by the compat module is delegated
 * to the opentelemetry-java providers it wraps.
 */
@OptIn(ExperimentalApi::class)
internal class CompatOpenTelemetrySdkCloseableTest {

    private val spanExporter = FakeOtelJavaSpanExporter()
    private val logExporter = FakeOtelJavaLogRecordExporter()

    private fun createSdk(): OpenTelemetrySdk {
        val otelJavaSdk = OtelJavaOpenTelemetrySdk.builder()
            .setTracerProvider(
                OtelJavaSdkTracerProvider.builder()
                    .addSpanProcessor(BatchSpanProcessor.builder(spanExporter).build())
                    .build()
            )
            .setLoggerProvider(
                OtelJavaSdkLoggerProvider.builder()
                    .addLogRecordProcessor(BatchLogRecordProcessor.builder(logExporter).build())
                    .build()
            )
            .build()
        return otelJavaSdk.toOtelKotlinApi() as OpenTelemetrySdk
    }

    @Test
    fun `force flush exports buffered telemetry`() = runBlocking {
        val sdk = createSdk()
        sdk.tracerProvider.getTracer("test").startSpan("span").end()
        sdk.loggerProvider.getLogger("test").emit(body = "log")

        assertEquals(Success, sdk.forceFlush())
        assertEquals(1, spanExporter.exports.size)
        assertEquals(1, logExporter.exports.size)
    }

    @Test
    fun `shutdown shuts down the wrapped providers`() = runBlocking {
        val sdk = createSdk()
        sdk.tracerProvider.getTracer("test").startSpan("span").end()
        sdk.loggerProvider.getLogger("test").emit(body = "log")

        assertEquals(Success, sdk.shutdown())
        assertEquals(1, spanExporter.shutdownCount)
        assertEquals(1, logExporter.shutdownCount)
        assertEquals(1, spanExporter.exports.size)
        assertEquals(1, logExporter.exports.size)

        // nothing is exported after shutdown
        sdk.tracerProvider.getTracer("test").startSpan("after").end()
        sdk.loggerProvider.getLogger("test").emit(body = "after")
        assertEquals(Success, sdk.forceFlush())
        assertEquals(1, spanExporter.exports.size)
        assertEquals(1, logExporter.exports.size)
    }

    @Test
    fun `shutdown is idempotent`() = runBlocking {
        val sdk = createSdk()
        assertEquals(Success, sdk.shutdown())
        assertEquals(Success, sdk.shutdown())
        assertEquals(1, spanExporter.shutdownCount)
        assertEquals(1, logExporter.shutdownCount)
    }

    @Test
    fun `flush and shutdown succeed when the wrapped providers are not sdk instances`() = runBlocking {
        val sdk = OtelJavaOpenTelemetry.noop().toOtelKotlinApi() as OpenTelemetrySdk
        assertEquals(Success, sdk.forceFlush())
        assertEquals(Success, sdk.shutdown())
    }

    @Test
    fun `shutdown of a dsl configured sdk stops spans being recorded`() = runBlocking {
        val sdk = createCompatOpenTelemetry() as OpenTelemetrySdk
        val tracer = sdk.tracerProvider.getTracer("test")
        assertTrue(tracer.startSpan("before").isRecording())
        assertEquals(Success, sdk.forceFlush())
        assertEquals(Success, sdk.shutdown())
        assertEquals(Success, sdk.shutdown())
        assertFalse(tracer.startSpan("after").isRecording())
    }
}
