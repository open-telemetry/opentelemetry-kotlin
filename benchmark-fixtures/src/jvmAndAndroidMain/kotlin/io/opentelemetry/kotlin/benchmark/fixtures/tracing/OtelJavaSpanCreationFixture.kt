package io.opentelemetry.kotlin.benchmark.fixtures.tracing

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaOpenTelemetry
import io.opentelemetry.kotlin.benchmark.fixtures.BenchmarkFixture

@OptIn(ExperimentalApi::class)
class OtelJavaSpanCreationFixture(
    otel: OtelJavaOpenTelemetry
) : BenchmarkFixture {

    private val tracer = otel.tracerProvider.get("test")

    override fun execute() {
        tracer.spanBuilder("new_span").startSpan()
    }
}
