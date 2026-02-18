package io.opentelemetry.kotlin.smoketest

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.NoopOpenTelemetry
import io.opentelemetry.kotlin.OpenTelemetry
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalApi::class)
class OpenTelemetryNoopSmokeTest {

    private lateinit var server: FakeOtlpServer
    private lateinit var otel: OpenTelemetry

    @BeforeTest
    fun setUp() {
        server = FakeOtlpServer()
        otel = NoopOpenTelemetry
    }

    @AfterTest
    fun tearDown() {
        server.close()
    }

    @Test
    fun exportsSpansAndLogs() = runTest {
        val tracer = otel.tracerProvider.getTracer("test-tracer")
        val span = tracer.createSpan("test-span")
        span.end()

        val logger = otel.loggerProvider.getLogger("test-logger")
        logger.emit(body = "test-log")

        // assert nothing sent after 1s
        delay(1.seconds)
        assertTrue(server.getSpans().isEmpty())
        assertTrue(server.getLogs().isEmpty())
    }
}
