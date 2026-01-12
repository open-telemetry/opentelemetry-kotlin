package io.opentelemetry.kotlin.benchmark.fixtures.logging

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.OpenTelemetry
import io.opentelemetry.kotlin.benchmark.fixtures.BenchmarkFixture

@OptIn(ExperimentalApi::class)
class LoggerCreationFixture(
    private val otel: OpenTelemetry
) : BenchmarkFixture {

    override fun execute() {
        otel.loggerProvider.getLogger(
            "test",
            "0.1.0",
            "https://example.com/schema"
        ) {
            setStringAttribute("key", "value")
        }
    }
}
