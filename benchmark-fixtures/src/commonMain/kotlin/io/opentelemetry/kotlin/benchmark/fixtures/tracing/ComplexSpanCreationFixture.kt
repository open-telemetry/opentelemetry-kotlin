package io.opentelemetry.kotlin.benchmark.fixtures.tracing

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.OpenTelemetry
import io.opentelemetry.kotlin.benchmark.fixtures.BenchmarkFixture
import io.opentelemetry.kotlin.tracing.model.SpanKind

@OptIn(ExperimentalApi::class)
class ComplexSpanCreationFixture(
    private val otel: OpenTelemetry
) : BenchmarkFixture {

    private val tracer = otel.tracerProvider.getTracer("test")
    private val other = tracer.startSpan("other")

    override fun execute() {
        tracer.startSpan(
            "new_span",
            otel.contextFactory.root(),
            SpanKind.CLIENT,
            null
        ) {
            repeat(100) { k ->
                setStringAttribute("key_$k", "value")
                addEvent("my_event_$k") {
                    setBooleanAttribute("event", true)
                }
                addLink(other.spanContext) {
                    setStringAttribute("link_$k", "value")
                }
            }
        }
    }
}
