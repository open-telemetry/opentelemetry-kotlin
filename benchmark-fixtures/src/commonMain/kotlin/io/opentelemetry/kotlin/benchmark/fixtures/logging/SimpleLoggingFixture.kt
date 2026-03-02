package io.opentelemetry.kotlin.benchmark.fixtures.logging

import io.opentelemetry.kotlin.OpenTelemetry
import io.opentelemetry.kotlin.benchmark.fixtures.BenchmarkFixture
import io.opentelemetry.kotlin.logging.Logger

class SimpleLoggingFixture(
    otel: OpenTelemetry
) : BenchmarkFixture {

    private val logger: Logger = otel.loggerProvider.getLogger("logger")

    override fun execute() {
        logger.emit("Hello world!")
    }
}
