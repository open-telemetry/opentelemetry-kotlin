package io.opentelemetry.kotlin

/**
 * The main entry point for the OpenTelemetry API.
 *
 * This contains interfaces in the SDK package and is not intended for use by instrumentation
 * authors: https://opentelemetry.io/docs/specs/otel/overview/#sdk
 */
@ExperimentalApi
public interface OpenTelemetrySdk : OpenTelemetry {

    /**
     * The [Clock] that will be used for obtaining timestamps by this instance.
     */
    public val clock: Clock
}
