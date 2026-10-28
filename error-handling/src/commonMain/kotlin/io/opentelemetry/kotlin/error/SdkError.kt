package io.opentelemetry.kotlin.error

/**
 * An error or misuse detected by the SDK, as reported to an [SdkErrorHandler].
 */
public sealed class SdkError(

    /**
     * Human-readable description of what went wrong.
     */
    public val message: String,

    /**
     * How serious the problem is.
     */
    public val severity: SdkErrorSeverity,
) {

    /**
     * The API was misused (e.g. passing an empty string to something that requires non-empty).
     */
    public class ApiMisuse(
        public val api: String,
        message: String,
        severity: SdkErrorSeverity,
    ) : SdkError(message, severity) {
        override fun toString(): String = "[$severity] $api misused: $message"
    }

    /**
     * User-supplied code, such as an exporter or processor, threw.
     */
    public class UserCodeError(
        public val cause: Throwable,
        message: String,
        severity: SdkErrorSeverity,
    ) : SdkError(message, severity) {
        override fun toString(): String = "[$severity] user code failed: $message ($cause)"
    }

    /**
     * SDK code threw.
     */
    public class SdkCodeError(
        public val cause: Throwable,
        message: String,
        severity: SdkErrorSeverity,
    ) : SdkError(message, severity) {
        override fun toString(): String = "[$severity] SDK code failed: $message ($cause)"
    }
}
