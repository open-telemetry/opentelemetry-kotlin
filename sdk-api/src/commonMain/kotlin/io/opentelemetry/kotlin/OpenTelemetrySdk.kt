package io.opentelemetry.kotlin

import io.opentelemetry.kotlin.export.TelemetryCloseable

/**
 * The main entry point for the OpenTelemetry API.
 *
 * This contains interfaces in the SDK package and is not intended for use by instrumentation
 * authors: https://opentelemetry.io/docs/specs/otel/overview/#sdk
 *
 * This interface extends [TelemetryCloseable] which allows the owner of the SDK instance
 * to flush and shutdown the SDK.
 */
@ExperimentalApi
public interface OpenTelemetrySdk : OpenTelemetry, TelemetryCloseable {

    /**
     * The [Clock] that will be used for obtaining timestamps by this instance.
     */
    public val clock: Clock
}
