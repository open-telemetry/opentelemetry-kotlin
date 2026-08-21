package io.opentelemetry.kotlin.logging

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * Immutable model of how a [Logger] should behave.
 *
 * https://opentelemetry.io/docs/specs/otel/logs/sdk/#loggerconfig
 */
@ExperimentalApi
public interface LoggerConfig {

    /**
     * Whether the logger is enabled. A disabled logger behaves the same as a no-op logger.
     */
    public val enabled: Boolean
        get() = true

    /**
     * The minimum severity that a log record must have to be processed.
     *
     * A log record whose severity is specified (i.e. not [SeverityNumber.UNKNOWN]) and is less
     * than this value is dropped. Log records without a specified severity are unaffected.
     */
    public val minimumSeverity: SeverityNumber
        get() = SeverityNumber.UNKNOWN

    /**
     * Whether log records associated with an unsampled trace should be dropped.
     *
     * Log records that are not associated with a trace are unaffected.
     */
    public val traceBased: Boolean
        get() = false
}
