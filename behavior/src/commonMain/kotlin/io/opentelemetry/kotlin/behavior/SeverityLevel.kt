package io.opentelemetry.kotlin.behavior

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
