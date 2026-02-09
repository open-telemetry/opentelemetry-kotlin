package io.opentelemetry.kotlin.benchmark.fixtures.tracing

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaOpenTelemetry
import io.opentelemetry.kotlin.benchmark.fixtures.BenchmarkFixture

@OptIn(ExperimentalApi::class)
class OtelJavaTracerCreationFixture(
    private val otel: OtelJavaOpenTelemetry
) : BenchmarkFixture {

    override fun execute() {
        // note: not possible to pass version/schemaUrl/attributes in opentelemetry-java
        otel.tracerProvider.get("test")
    }
}
