package io.opentelemetry.kotlin.benchmark.fixtures.logging

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaContext
import io.opentelemetry.kotlin.aliases.OtelJavaLogger
import io.opentelemetry.kotlin.aliases.OtelJavaOpenTelemetry
import io.opentelemetry.kotlin.aliases.OtelJavaSeverity
import io.opentelemetry.kotlin.benchmark.fixtures.BenchmarkFixture
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalApi::class)
class OtelJavaComplexLoggingFixture(
    otel: OtelJavaOpenTelemetry
) : BenchmarkFixture {

    private val logger: OtelJavaLogger = otel.logsBridge.get("logger")

    override fun execute() {
        logger.logRecordBuilder()
            .setBody("Hello world!")
            .setTimestamp(500, TimeUnit.NANOSECONDS)
            .setObservedTimestamp(1000, TimeUnit.NANOSECONDS)
            .setContext(OtelJavaContext.root())
            .setSeverity(OtelJavaSeverity.DEBUG3)
            .setSeverityText("debug3")
            .setAttribute("key", "value")
            .emit()
    }
}
