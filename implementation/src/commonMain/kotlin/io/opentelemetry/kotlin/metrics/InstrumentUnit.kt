package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.error.SdkError
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.error.SdkErrorSeverity
import io.opentelemetry.kotlin.error.reportError

/**
 * The maximum length instrument units must support, per
 * https://opentelemetry.io/docs/specs/otel/metrics/api/#instrument-unit.
 */
internal const val MAX_INSTRUMENT_UNIT_CHARS = 63

/**
 * Sanitizes [unit] to satisfy the
 * [instrument unit](https://opentelemetry.io/docs/specs/otel/metrics/api/#instrument-unit)
 * requirements: a case-sensitive ASCII string of at most [MAX_INSTRUMENT_UNIT_CHARS] characters.
 *
 * Invalid units are reported as [SdkError.ApiMisuse] and dropped to `null` rather than
 * destabilizing the host or being rewritten to a misleading value. `null` stays `null`.
 */
internal fun SdkErrorHandler.sanitizeInstrumentUnit(unit: String?): String? {
    if (unit == null) {
        return null
    }
    if (unit.length <= MAX_INSTRUMENT_UNIT_CHARS && unit.all { it.code in 0..127 }) {
        return unit
    }
    reportError(
        SdkError.ApiMisuse(
            api = "Instrument.unit",
            message = "Instrument unit is invalid (must be ASCII, at most " +
                "$MAX_INSTRUMENT_UNIT_CHARS characters); it was dropped.",
            severity = SdkErrorSeverity.WARNING,
        )
    )
    return null
}
