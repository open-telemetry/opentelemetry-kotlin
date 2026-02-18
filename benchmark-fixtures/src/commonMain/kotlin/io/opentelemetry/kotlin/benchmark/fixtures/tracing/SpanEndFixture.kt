package io.opentelemetry.kotlin.benchmark.fixtures.tracing

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.OpenTelemetry
import io.opentelemetry.kotlin.benchmark.fixtures.BenchmarkFixture

@OptIn(ExperimentalApi::class)
class SpanEndFixture(
    otel: OpenTelemetry
) : BenchmarkFixture {

    private val tracer = otel.tracerProvider.getTracer("test")
    private val span = tracer.startSpan("new_span")

    override fun execute() {
        span.end()
    }
}
