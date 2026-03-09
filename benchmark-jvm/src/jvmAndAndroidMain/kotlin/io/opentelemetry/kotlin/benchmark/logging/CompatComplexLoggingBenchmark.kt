package io.kotlin.opentelemetry.benchmark.logging

import io.opentelemetry.kotlin.OpenTelemetry
import io.opentelemetry.kotlin.createCompatOpenTelemetry
import io.opentelemetry.kotlin.createOpenTelemetry
import io.opentelemetry.kotlin.logging.Logger
import io.opentelemetry.kotlin.logging.model.SeverityNumber
import kotlinx.benchmark.Benchmark
import kotlinx.benchmark.Scope
import kotlinx.benchmark.Setup
import kotlinx.benchmark.State

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
        logger.emit(
            body = "Hello world!",
            timestamp = 500,
            observedTimestamp = 1000,
            context = otel.contextFactory.root(),
            severityNumber = SeverityNumber.DEBUG3,
            severityText = "debug3"
        ) {
            setStringAttribute("key", "value")
        }
    }
}
