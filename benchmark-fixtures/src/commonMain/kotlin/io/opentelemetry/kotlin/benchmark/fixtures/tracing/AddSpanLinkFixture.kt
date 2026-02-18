package io.opentelemetry.kotlin.benchmark.fixtures.tracing

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.OpenTelemetry
import io.opentelemetry.kotlin.benchmark.fixtures.BenchmarkFixture
import io.opentelemetry.kotlin.tracing.addLink

@OptIn(ExperimentalApi::class)
class AddSpanLinkFixture(
    otel: OpenTelemetry
) : BenchmarkFixture {

    private val tracer = otel.tracerProvider.getTracer("test")
    private val other = tracer.startSpan("other")
    private val span = tracer.startSpan("new_span")

    override fun execute() {
        span.addLink(other) {
            setStringAttribute("key", "value")
        }
    }
}
