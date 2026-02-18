package io.opentelemetry.kotlin.benchmark.fixtures.logging

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.OpenTelemetry
import io.opentelemetry.kotlin.benchmark.fixtures.BenchmarkFixture
import io.opentelemetry.kotlin.logging.Logger
import io.opentelemetry.kotlin.logging.model.SeverityNumber

@OptIn(ExperimentalApi::class)
class ComplexLoggingFixture(
    private val otel: OpenTelemetry
) : BenchmarkFixture {

    private val logger: Logger = otel.loggerProvider.getLogger("logger")

    override fun execute() {
        logger.emit(
            body = "Hello world!",
            timestamp = 500,
            observedTimestamp = 1000,
            context = otel.contextFactory.root(),
            severityNumber = SeverityNumber.DEBUG3,
            severityText = "debug3"
        ) { setStringAttribute("key", "value") }
    }
}
