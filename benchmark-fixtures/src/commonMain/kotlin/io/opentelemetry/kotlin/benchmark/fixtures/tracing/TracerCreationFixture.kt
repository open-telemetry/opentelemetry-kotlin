package io.opentelemetry.kotlin.benchmark.fixtures.tracing

import io.opentelemetry.kotlin.OpenTelemetry
import io.opentelemetry.kotlin.benchmark.fixtures.BenchmarkFixture

class TracerCreationFixture(
    private val otel: OpenTelemetry
) : BenchmarkFixture {

    override fun execute() {
        otel.tracerProvider.getTracer(
            "test",
            "0.1.0",
            "https://example.com/schema"
        ) {
            setStringAttribute("key", "value")
        }
    }
}
