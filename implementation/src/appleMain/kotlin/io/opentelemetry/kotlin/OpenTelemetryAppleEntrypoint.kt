package io.opentelemetry.kotlin

/**
 * Constructs an [OpenTelemetrySdk] instance that uses the opentelemetry-kotlin implementation.
 */
@ExperimentalApi
public fun createAppleOpenTelemetry(): OpenTelemetrySdk = createOpenTelemetry()
