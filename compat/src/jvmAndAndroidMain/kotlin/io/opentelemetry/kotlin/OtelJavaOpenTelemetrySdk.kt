package io.opentelemetry.kotlin

import io.opentelemetry.kotlin.aliases.OtelJavaContextPropagators
import io.opentelemetry.kotlin.aliases.OtelJavaLoggerProvider
import io.opentelemetry.kotlin.aliases.OtelJavaOpenTelemetry
import io.opentelemetry.kotlin.aliases.OtelJavaTracerProvider

internal class OtelJavaOpenTelemetrySdk(
    private val tracerProvider: OtelJavaTracerProvider,
    private val loggerProvider: OtelJavaLoggerProvider,
) : OtelJavaOpenTelemetry {

    override fun getTracerProvider(): OtelJavaTracerProvider = tracerProvider
    override fun getLogsBridge(): OtelJavaLoggerProvider = loggerProvider
    override fun getPropagators(): OtelJavaContextPropagators = OtelJavaContextPropagators.noop()
}
