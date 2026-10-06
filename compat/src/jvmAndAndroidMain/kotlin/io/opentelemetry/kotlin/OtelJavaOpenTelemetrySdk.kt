package io.opentelemetry.kotlin

import io.opentelemetry.kotlin.aliases.OtelJavaContextPropagators
import io.opentelemetry.kotlin.aliases.OtelJavaLoggerProvider
import io.opentelemetry.kotlin.aliases.OtelJavaMeterProvider
import io.opentelemetry.kotlin.aliases.OtelJavaOpenTelemetry
import io.opentelemetry.kotlin.aliases.OtelJavaTracerProvider

internal class OtelJavaOpenTelemetrySdk(
    private val tracerProvider: OtelJavaTracerProvider,
    private val loggerProvider: OtelJavaLoggerProvider,
    private val meterProvider: OtelJavaMeterProvider,
    private val propagators: OtelJavaContextPropagators,
) : OtelJavaOpenTelemetry {

    override fun getTracerProvider(): OtelJavaTracerProvider = tracerProvider
    override fun getLogsBridge(): OtelJavaLoggerProvider = loggerProvider
    override fun getMeterProvider(): OtelJavaMeterProvider = meterProvider
    override fun getPropagators(): OtelJavaContextPropagators = propagators
}
