package io.opentelemetry.kotlin.behavior

import io.opentelemetry.kotlin.error.SdkErrorSeverity
import io.opentelemetry.kotlin.logging.SeverityNumber

enum class SeverityLevel {
    TRACE,
    TRACE2,
    TRACE3,
    TRACE4,
    DEBUG,
    DEBUG2,
    DEBUG3,
    DEBUG4,
    INFO,
    INFO2,
    INFO3,
    INFO4,
    WARN,
    WARN2,
    WARN3,
    WARN4,
    ERROR,
    ERROR2,
    ERROR3,
    ERROR4,
    FATAL,
    FATAL2,
    FATAL3,
    FATAL4,
}

fun String.toSeverityLevel(): SeverityLevel? {
    return try {
        // Should be interpreted in a case-insensitive manner
        // See the spec: https://opentelemetry.io/docs/specs/otel/configuration/sdk-environment-variables/#enum
        SeverityLevel.valueOf(this.uppercase())
    } catch (_: IllegalArgumentException) {
        null
    }
}

/**
 * Maps this log level onto the lowest [SdkErrorSeverity] the SDK error handler should receive.
 */
fun SeverityLevel.toMinimumSdkErrorSeverity(): SdkErrorSeverity = when (this) {
    SeverityLevel.TRACE, SeverityLevel.TRACE2, SeverityLevel.TRACE3, SeverityLevel.TRACE4,
    SeverityLevel.DEBUG, SeverityLevel.DEBUG2, SeverityLevel.DEBUG3, SeverityLevel.DEBUG4,
    SeverityLevel.INFO, SeverityLevel.INFO2, SeverityLevel.INFO3, SeverityLevel.INFO4 -> SdkErrorSeverity.INFO

    SeverityLevel.WARN, SeverityLevel.WARN2, SeverityLevel.WARN3, SeverityLevel.WARN4 -> SdkErrorSeverity.WARNING

    SeverityLevel.ERROR, SeverityLevel.ERROR2, SeverityLevel.ERROR3, SeverityLevel.ERROR4,
    SeverityLevel.FATAL, SeverityLevel.FATAL2, SeverityLevel.FATAL3, SeverityLevel.FATAL4 -> SdkErrorSeverity.ERROR
}

/**
 * Maps a log [SeverityNumber] onto the equivalent [SeverityLevel], or `null` for
 * [SeverityNumber.UNKNOWN], which has no level.
 */
fun SeverityNumber.toSeverityLevel(): SeverityLevel? = when (this) {
    SeverityNumber.UNKNOWN -> null
    else -> SeverityLevel.valueOf(name)
}
