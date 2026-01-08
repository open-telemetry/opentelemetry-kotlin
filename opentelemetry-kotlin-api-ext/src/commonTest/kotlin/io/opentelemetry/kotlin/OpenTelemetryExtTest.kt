package io.opentelemetry.kotlin

import kotlin.test.Test
import kotlin.test.assertSame

@OptIn(ExperimentalApi::class)
internal class OpenTelemetryExtTest {

    @Test
    fun testGetTracer() {
        val otel = FakeOpenTelemetry()
        val name = "my_tracer"
        val expected = otel.tracerProvider.getTracer(name)
        val observed = otel.getTracer(name)
        assertSame(expected, observed)
    }

    @Test
    fun testGetLogger() {
        val otel = FakeOpenTelemetry()
        val name = "my_logger"
        val expected = otel.loggerProvider.getLogger(name)
        val observed = otel.getLogger(name)
        assertSame(expected, observed)
    }
}
