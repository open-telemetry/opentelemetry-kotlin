package io.opentelemetry.kotlin

/**
 * Constructs an [io.opentelemetry.kotlin.OpenTelemetry] instance that uses the opentelemetry-kotlin implementation.
 */
@ExperimentalApi
public fun createAppleOpenTelemetry(): OpenTelemetry = createOpenTelemetry()
