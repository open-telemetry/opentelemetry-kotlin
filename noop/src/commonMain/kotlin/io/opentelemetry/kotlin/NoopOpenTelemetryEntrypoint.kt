package io.opentelemetry.kotlin

/**
 * Returns a no-op instance of [OpenTelemetry] instance.
 */
@ExperimentalApi
@Deprecated("Use NoopOpenTelemetry instead.", ReplaceWith("NoopOpenTelemetry"))
public fun createNoopOpenTelemetry(): OpenTelemetry = NoopOpenTelemetryImpl

/**
 * A no-op instance of [OpenTelemetry]. This should be used if you want [OpenTelemetry] to perform
 * no action. This can be particularly beneficial if you are a library author and you wish to
 * allow library consumers to opt-in to telemetry, as you can specify a noop instance by default.
 *
 * For example, a library consumer could pass their own instance of [OpenTelemetry] when
 * initializing the library. If they are not interested in telemetry, the implementation defaults
 * to a noop:
 *
 * fun initializeLibrary(otel: OpenTelemetry = NoopOpenTelemetry)
 */
@ExperimentalApi
public val NoopOpenTelemetry: OpenTelemetry = NoopOpenTelemetryImpl
