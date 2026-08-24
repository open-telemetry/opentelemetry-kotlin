package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.error.SdkError
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.error.SdkErrorSeverity
import io.opentelemetry.kotlin.error.reportError

/**
 * The maximum length instrument names must support, per
 * https://opentelemetry.io/docs/specs/otel/metrics/api/#instrument-name-syntax.
 */
internal const val MAX_INSTRUMENT_NAME_CHARS = 255

private val INSTRUMENT_NAME = Regex("^[A-Za-z][A-Za-z0-9_./-]{0,254}$")

/**
 * Checks [name] against the
 * [instrument name syntax](https://opentelemetry.io/docs/specs/otel/metrics/api/#instrument-name-syntax):
 * a non-empty ASCII string starting with a letter, followed by up to
 * [MAX_INSTRUMENT_NAME_CHARS] - 1 alphanumeric characters, `_`, `.`, `-`, or `/`.
 *
 * Invalid names are reported as [SdkError.ApiMisuse] so callers can fall back to a noop
 * instrument rather than destabilizing the host or risking a name collision from rewriting.
 */
internal fun SdkErrorHandler.isValidInstrumentName(name: String): Boolean {
    if (INSTRUMENT_NAME.matches(name)) {
        return true
    }
    reportError(
        SdkError.ApiMisuse(
            api = "Instrument.name",
            message = "Instrument name \"$name\" is invalid; returning a noop instrument. " +
                "Names must be 1-$MAX_INSTRUMENT_NAME_CHARS ASCII characters, start with a letter, " +
                "and contain only alphanumeric characters, '_', '.', '-', and '/'.",
            severity = SdkErrorSeverity.WARNING,
        )
    )
    return false
}
