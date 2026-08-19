package io.opentelemetry.kotlin.error

/**
 * Handles errors and misuse of the SDK.
 *
 * Telemetry must never destabilize the host application, so implementations should not throw.
 * https://opentelemetry.io/docs/specs/otel/error-handling/
 */
public fun interface SdkErrorHandler {

    /**
     * Called when the SDK detects an error or misuse.
     */
    public fun onError(error: SdkError)
}
