package io.opentelemetry.kotlin

/**
 * Returns a no-op instance of [OpenTelemetry] instance.
 */
@OptIn(ExperimentalApi::class)
public fun createNoopOpenTelemetry(): OpenTelemetry = NoopOpenTelemetry
