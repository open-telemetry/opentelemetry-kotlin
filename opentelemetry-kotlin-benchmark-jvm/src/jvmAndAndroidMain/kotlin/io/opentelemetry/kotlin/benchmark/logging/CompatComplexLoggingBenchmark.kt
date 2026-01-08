package io.kotlin.opentelemetry.benchmark.logging

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.OpenTelemetry
import io.opentelemetry.kotlin.createCompatOpenTelemetry
import io.opentelemetry.kotlin.createOpenTelemetry
import io.opentelemetry.kotlin.logging.Logger
import io.opentelemetry.kotlin.logging.model.SeverityNumber
import kotlinx.benchmark.Benchmark
import kotlinx.benchmark.Scope
import kotlinx.benchmark.Setup
import kotlinx.benchmark.State

@OptIn(ExperimentalApi::class)
@State(Scope.Benchmark)
class CompatComplexLoggingBenchmark {

    private lateinit var otel: OpenTelemetry
    private lateinit var logger: Logger

    @Setup
    fun setup() {
        otel = createCompatOpenTelemetry()
        logger = otel.loggerProvider.getLogger("logger")
    }

    @Benchmark
    fun benchmarkComplexLogCompat() {
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
