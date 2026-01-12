package io.opentelemetry.kotlin.benchmark.fixtures.tracing

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaAttributeKey
import io.opentelemetry.kotlin.aliases.OtelJavaAttributes
import io.opentelemetry.kotlin.aliases.OtelJavaOpenTelemetry
import io.opentelemetry.kotlin.benchmark.fixtures.BenchmarkFixture

@OptIn(ExperimentalApi::class)
class OtelJavaAddSpanLinkFixture(
    otel: OtelJavaOpenTelemetry
) : BenchmarkFixture {

    private val tracer = otel.tracerProvider.get("test")
    private val span = tracer.spanBuilder("new_span").startSpan()
    private val other = tracer.spanBuilder("other").startSpan()

    override fun execute() {
        val attrs = OtelJavaAttributes.of(OtelJavaAttributeKey.stringKey("key"), "value")
        span.addLink(other.spanContext, attrs)
    }
}
