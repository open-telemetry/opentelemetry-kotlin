package io.opentelemetry.kotlin.error

/**
 * Handles errors and misuse of the SDK.
 */
public interface SdkErrorHandler {

    /**
     * Called when the API was misused (e.g. passing an empty string to something that requires non-empty)
     */
    public fun onApiMisuse(api: String, details: String, severity: SdkErrorSeverity)

    /**
     * Called when user-supplied code throws
     */
    public fun onUserCodeError(exc: Throwable, details: String, severity: SdkErrorSeverity)

    /**
     * Called when SDK code throws
     */
    public fun onSdkCodeError(exc: Throwable, details: String, severity: SdkErrorSeverity)
}
