package io.opentelemetry.kotlin

import io.opentelemetry.kotlin.aliases.OtelJavaOpenTelemetry
import io.opentelemetry.kotlin.logging.OtelJavaLoggerProviderAdapter
import io.opentelemetry.kotlin.metrics.OtelJavaMeterProviderAdapter
import io.opentelemetry.kotlin.tracing.OtelJavaTracerProviderAdapter

/**
 * Constructs an [OtelJavaOpenTelemetry] instance that makes the Kotlin implementation conform
 * to the opentelemetry-java API.
 *
 * End-users should generally not use this function and should call [createCompatOpenTelemetry]
 * or [toOtelKotlinApi] instead.
 *
 * Avoid reading or setting the opentelemetry-java implicit context directly through
 * `Context.current()`, `Context.makeCurrent()`, `Span.current()`, `Baggage.current()`, etc. These
 * APIs use separate stores that is not possible to wrap, and therefore trace context will not work
 * correctly. See the compat README for details.
 */
@ExperimentalApi
public fun OpenTelemetry.toOtelJavaApi(): OtelJavaOpenTelemetry {
    if (this == NoopOpenTelemetry) {
        return OtelJavaOpenTelemetry.noop()
    }
    return OtelJavaOpenTelemetrySdk(
        OtelJavaTracerProviderAdapter(tracerProvider, context),
        OtelJavaLoggerProviderAdapter(loggerProvider),
        OtelJavaMeterProviderAdapter(meterProvider),
    )
}
