package io.opentelemetry.kotlin.logging

/**
 * Represents all possible severities of a log message.
 *
 * https://opentelemetry.io/docs/specs/otel/logs/data-model/#field-severitynumber
 */
public enum class SeverityNumber(public val value: Int) {
    UNKNOWN(0),
    TRACE(1),
    TRACE2(2),
    TRACE3(3),
    TRACE4(4),
    DEBUG(5),
    DEBUG2(6),
    DEBUG3(7),
    DEBUG4(8),
    INFO(9),
    INFO2(10),
    INFO3(11),
    INFO4(12),
    WARN(13),
    WARN2(14),
    WARN3(15),
    WARN4(16),
    ERROR(17),
    ERROR2(18),
    ERROR3(19),
    ERROR4(20),
    FATAL(21),
    FATAL2(22),
    FATAL3(23),
    FATAL4(24);

    public companion object {

        /**
         * Returns the [SeverityNumber] with the given numeric [value], or [UNKNOWN] if [value] is outside
         * the 0-24 range defined by the OpenTelemetry log data model.
         */
        public fun fromValue(value: Int): SeverityNumber = entries.getOrNull(value) ?: UNKNOWN
    }
}
