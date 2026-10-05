package io.opentelemetry.kotlin

import io.opentelemetry.kotlin.aliases.OtelJavaContextPropagators
import io.opentelemetry.kotlin.factory.CompatContextFactory
import io.opentelemetry.kotlin.logging.FakeLoggerProvider
import io.opentelemetry.kotlin.logging.OtelJavaLoggerProviderAdapter
import io.opentelemetry.kotlin.metrics.FakeMeterProvider
import io.opentelemetry.kotlin.metrics.OtelJavaMeterProviderAdapter
import io.opentelemetry.kotlin.tracing.FakeTracerProvider
import io.opentelemetry.kotlin.tracing.OtelJavaTracerProviderAdapter
import org.junit.Test
import kotlin.test.assertSame

internal class OtelJavaOpenTelemetrySdkTest {

    @Test
    fun `test entrypoint`() {
        val tracerProvider = OtelJavaTracerProviderAdapter(FakeTracerProvider(), CompatContextFactory())
        val loggerProvider = OtelJavaLoggerProviderAdapter(FakeLoggerProvider())
        val meterProvider = OtelJavaMeterProviderAdapter(FakeMeterProvider())

        val otel = OtelJavaOpenTelemetrySdk(tracerProvider, loggerProvider, meterProvider)
        assertSame(tracerProvider, otel.tracerProvider)
        assertSame(loggerProvider, otel.logsBridge)
        assertSame(meterProvider, otel.meterProvider)
        assertSame(otel.propagators, OtelJavaContextPropagators.noop())
    }
}
