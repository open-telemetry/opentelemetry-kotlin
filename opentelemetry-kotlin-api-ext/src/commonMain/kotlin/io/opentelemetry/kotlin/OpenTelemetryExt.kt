package io.opentelemetry.kotlin

import io.opentelemetry.kotlin.logging.Logger
import io.opentelemetry.kotlin.tracing.Tracer

/**
 * Returns a [Tracer] for the given [instrumentationScopeName]. This is equivalent to calling
 * [io.opentelemetry.kotlin.tracing.TracerProvider.getTracer].
 */
@ExperimentalApi
@ThreadSafe
public fun OpenTelemetry.getTracer(instrumentationScopeName: String): Tracer {
    return tracerProvider.getTracer(name = instrumentationScopeName)
}

/**
 * Returns a [Logger] for the given [instrumentationScopeName]. This is equivalent to calling
 * [io.opentelemetry.kotlin.logging.LoggerProvider.getLogger].
 */
@ExperimentalApi
@ThreadSafe
public fun OpenTelemetry.getLogger(instrumentationScopeName: String): Logger {
    return loggerProvider.getLogger(name = instrumentationScopeName)
}
