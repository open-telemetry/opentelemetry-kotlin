package io.opentelemetry.kotlin.benchmark.fixtures.tracing

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaAttributeKey
import io.opentelemetry.kotlin.aliases.OtelJavaAttributes
import io.opentelemetry.kotlin.aliases.OtelJavaContext
import io.opentelemetry.kotlin.aliases.OtelJavaOpenTelemetry
import io.opentelemetry.kotlin.aliases.OtelJavaSpanKind
import io.opentelemetry.kotlin.benchmark.fixtures.BenchmarkFixture

@OptIn(ExperimentalApi::class)
class OtelJavaComplexSpanCreationFixture(
    otel: OtelJavaOpenTelemetry
) : BenchmarkFixture {

    private val tracer = otel.tracerProvider.get("test")
    private val other = tracer.spanBuilder("other").startSpan()

    override fun execute() {
        val builder = tracer.spanBuilder("new_span")
            .setParent(OtelJavaContext.root())
            .setSpanKind(OtelJavaSpanKind.CLIENT)

        repeat(100) { k ->
            builder.setAttribute("key_$k", "value")
            val attrs = OtelJavaAttributes.of(OtelJavaAttributeKey.stringKey("link_$k"), "value")
            builder.addLink(other.spanContext, attrs)
        }

        val span = builder.startSpan()

        repeat(100) { k ->
            val attrs = OtelJavaAttributes.of(OtelJavaAttributeKey.stringKey("key"), "value")
            span.addEvent("my_event_$k", attrs)
        }
    }
}
