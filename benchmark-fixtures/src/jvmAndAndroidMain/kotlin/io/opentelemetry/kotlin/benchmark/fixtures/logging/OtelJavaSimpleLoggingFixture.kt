package io.opentelemetry.kotlin.benchmark.fixtures.logging

import io.opentelemetry.kotlin.aliases.OtelJavaLogger
import io.opentelemetry.kotlin.aliases.OtelJavaOpenTelemetry
import io.opentelemetry.kotlin.benchmark.fixtures.BenchmarkFixture

class OtelJavaSimpleLoggingFixture(
    otel: OtelJavaOpenTelemetry
) : BenchmarkFixture {

    private val logger: OtelJavaLogger = otel.logsBridge.get("logger")

    override fun execute() {
        logger.logRecordBuilder()
            .setBody("Hello world!")
            .emit()
    }
}
