package io.opentelemetry.kotlin.smoketest

import io.opentelemetry.kotlin.OpenTelemetry
import io.opentelemetry.kotlin.createOpenTelemetry
import io.opentelemetry.kotlin.export.OperationResultCode.Success
import io.opentelemetry.kotlin.export.TelemetryCloseable
import io.opentelemetry.kotlin.logging.export.otlpHttpLogRecordExporter
import io.opentelemetry.kotlin.logging.export.simpleLogRecordProcessor
import io.opentelemetry.kotlin.tracing.export.otlpHttpSpanExporter
import io.opentelemetry.kotlin.tracing.export.simpleSpanProcessor
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

class ShutdownComplianceSmokeTest {

    private lateinit var server: FakeOtlpServer
    private lateinit var otel: OpenTelemetry

    @BeforeTest
    fun setUp() {
        server = FakeOtlpServer()
        otel = createOpenTelemetry {
            tracerProvider {
                export {
                    simpleSpanProcessor(
                        otlpHttpSpanExporter(server.baseUrl, server.mockEngine)
                    )
                }
            }
            loggerProvider {
                export {
                    simpleLogRecordProcessor(
                        otlpHttpLogRecordExporter(server.baseUrl, server.mockEngine)
                    )
                }
            }
        }
    }

    @AfterTest
    fun tearDown() {
        server.close()
    }

    @Test
    fun telemetryNotExportedAfterShutdown() = runTest {
        val tracer = otel.tracerProvider.getTracer("test")
        val logger = otel.loggerProvider.getLogger("test")

        // baseline: pipeline works
        tracer.startSpan("before").end()
        logger.emit(body = "before")
        server.awaitSpan { it.name == "before" }
        server.awaitLog { it.body == "before" }

        // shutdown
        val closeable = otel as TelemetryCloseable
        assertEquals(Success, closeable.shutdown())

        // emit after shutdown
        tracer.startSpan("after").end()
        logger.emit(body = "after")
        delay(1.seconds)

        // only pre-shutdown telemetry should be present
        assertEquals(1, server.getSpans().size)
        assertEquals(1, server.getLogs().size)
    }

    @Test
    fun tracerAndLoggerObtainedBeforeShutdownDoNothingAfterShutdown() = runTest {
        val tracer = otel.tracerProvider.getTracer("test")
        val logger = otel.loggerProvider.getLogger("test")

        // baseline: held references work
        tracer.startSpan("pre").end()
        logger.emit(body = "pre")
        server.awaitSpan { it.name == "pre" }
        server.awaitLog { it.body == "pre" }

        // shutdown
        val closeable = otel as TelemetryCloseable
        closeable.shutdown()

        // same references should be inert
        tracer.startSpan("post").end()
        logger.emit(body = "post")
        delay(1.seconds)

        assertEquals(1, server.getSpans().size)
        assertEquals(1, server.getLogs().size)
        assertFalse(logger.enabled())
    }

    @Test
    fun getTracerAndGetLoggerReturnNoopsAfterShutdown() = runTest {
        val closeable = otel as TelemetryCloseable
        closeable.shutdown()

        // obtain new tracer/logger after shutdown
        val tracer = otel.tracerProvider.getTracer("new")
        val logger = otel.loggerProvider.getLogger("new")

        tracer.startSpan("noop-span").end()
        logger.emit(body = "noop-log")
        delay(1.seconds)

        assertTrue(server.getSpans().isEmpty())
        assertTrue(server.getLogs().isEmpty())
        assertFalse(logger.enabled())
    }

    @Test
    fun shutdownCanBeCalledMultipleTimes() = runTest {
        val closeable = otel as TelemetryCloseable
        assertEquals(Success, closeable.shutdown())
        assertEquals(Success, closeable.shutdown())
    }

    @Test
    fun forceFlushIndependentOfShutdown() = runTest {
        val closeable = otel as TelemetryCloseable
        assertEquals(Success, closeable.shutdown())
        assertEquals(Success, closeable.forceFlush())
    }
}
