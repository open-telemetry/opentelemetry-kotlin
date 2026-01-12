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
        logger.log(
            "Hello world!",
            500,
            1000,
            otel.contextFactory.root(),
            SeverityNumber.DEBUG3,
            "debug3"
        ) {
            setStringAttribute("key", "value")
        }
    }
}
