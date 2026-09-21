package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.InstrumentationScopeInfo
import io.opentelemetry.kotlin.NoopOpenTelemetry
import io.opentelemetry.kotlin.error.SdkError
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.error.SdkErrorSeverity
import io.opentelemetry.kotlin.error.reportError
import io.opentelemetry.kotlin.resource.Resource

internal class MeterImpl(
    val instrumentationScopeInfo: InstrumentationScopeInfo,
    val resource: Resource,
    private val sdkErrorHandler: SdkErrorHandler,
) : Meter {
    override fun createDoubleUpDownCounter(
        name: String,
        unit: String?,
        description: String?,
    ): DoubleUpDownCounter {
        val noopMeter = obtainNoopMeterIfInvalid(name) ?: return DoubleUpDownCounterImpl(
            name,
            sanitizeInstrumentUnit(unit),
            sanitizeInstrumentDescription(description),
        )
        return noopMeter.createDoubleUpDownCounter(name, unit, description)
    }

    override fun createLongUpDownCounter(
        name: String,
        unit: String?,
        description: String?,
    ): LongUpDownCounter {
        val noopMeter = obtainNoopMeterIfInvalid(name) ?: return LongUpDownCounterImpl(
            name,
            sanitizeInstrumentUnit(unit),
            sanitizeInstrumentDescription(description),
        )
        return noopMeter.createLongUpDownCounter(name, unit, description)
    }

    /**
     * Returns a noop meter if [name] is not a valid
     * [instrument name](https://opentelemetry.io/docs/specs/otel/metrics/api/#instrument-name-syntax),
     * so callers fall back to a noop instrument rather than destabilizing the host or risking a
     * name collision from rewriting. Returns `null` when [name] is valid.
     */
    private fun obtainNoopMeterIfInvalid(name: String): Meter? = if (isValidInstrumentName(name)) {
        null
    } else {
        NoopOpenTelemetry.meterProvider.getMeter("")
    }

    /**
     * Checks [name] against the
     * [instrument name syntax](https://opentelemetry.io/docs/specs/otel/metrics/api/#instrument-name-syntax):
     * a non-empty ASCII string starting with a letter, followed by up to
     * [MAX_INSTRUMENT_NAME_CHARS] - 1 alphanumeric characters, `_`, `.`, `-`, or `/`.
     */
    private fun isValidInstrumentName(name: String): Boolean {
        if (INSTRUMENT_NAME.matches(name)) {
            return true
        }
        sdkErrorHandler.reportError(
            SdkError.ApiMisuse(
                api = "Instrument.name",
                message = "Instrument name is invalid; returning a noop instrument. " +
                    "Names must be 1-$MAX_INSTRUMENT_NAME_CHARS ASCII characters, start with a letter, " +
                    "and contain only alphanumeric characters, '_', '.', '-', and '/'.",
                severity = SdkErrorSeverity.WARNING,
            )
        )
        return false
    }

    /**
     * Sanitizes [unit] to satisfy the
     * [instrument unit](https://opentelemetry.io/docs/specs/otel/metrics/api/#instrument-unit)
     * requirements: a case-sensitive ASCII string of at most [MAX_INSTRUMENT_UNIT_CHARS]
     * characters.
     *
     * Invalid units are reported as [SdkError.ApiMisuse] and dropped to `null` rather than
     * destabilizing the host or being rewritten to a misleading value. `null` stays `null`.
     */
    private fun sanitizeInstrumentUnit(unit: String?): String? {
        if (unit == null) {
            return null
        }
        if (unit.length <= MAX_INSTRUMENT_UNIT_CHARS && unit.all { it.code in 0..127 }) {
            return unit
        }
        sdkErrorHandler.reportError(
            SdkError.ApiMisuse(
                api = "Instrument.unit",
                message = "Instrument unit is invalid (must be ASCII, at most " +
                    "$MAX_INSTRUMENT_UNIT_CHARS characters); it was dropped.",
                severity = SdkErrorSeverity.WARNING,
            )
        )
        return null
    }

    /**
     * Sanitizes [description] to satisfy the
     * [instrument description](https://opentelemetry.io/docs/specs/otel/metrics/api/#instrument-description)
     * requirements: an opaque string that MUST support at least
     * [MAX_INSTRUMENT_DESCRIPTION_CHARS] Unicode code points. Descriptions exceeding that
     * length are truncated and reported as [SdkError.ApiMisuse] so the host is never
     * destabilized by invalid input.
     *
     * `null` and strings within the limit are returned unchanged.
     */
    private fun sanitizeInstrumentDescription(description: String?): String? {
        if (description == null) {
            return null
        }
        val codePointCount = description.codePointCount()
        if (codePointCount <= MAX_INSTRUMENT_DESCRIPTION_CHARS) {
            return description
        }
        sdkErrorHandler.reportError(
            SdkError.ApiMisuse(
                api = "Instrument.description",
                message = "Instrument description has $codePointCount code points, exceeding the " +
                    "$MAX_INSTRUMENT_DESCRIPTION_CHARS code point limit; it was truncated.",
                severity = SdkErrorSeverity.WARNING,
            )
        )
        return description.takeCodePoints(MAX_INSTRUMENT_DESCRIPTION_CHARS)
    }

    private companion object {
        /**
         * The maximum length instrument names must support, per
         * https://opentelemetry.io/docs/specs/otel/metrics/api/#instrument-name-syntax.
         */
        const val MAX_INSTRUMENT_NAME_CHARS = 255

        val INSTRUMENT_NAME = Regex("^[A-Za-z][A-Za-z0-9_./-]{0,254}$")

        /**
         * The maximum length instrument units must support, per
         * https://opentelemetry.io/docs/specs/otel/metrics/api/#instrument-unit.
         */
        const val MAX_INSTRUMENT_UNIT_CHARS = 63

        /**
         * The minimum number of Unicode code points instrument descriptions must support, per
         * https://opentelemetry.io/docs/specs/otel/metrics/api/#instrument-description.
         */
        const val MAX_INSTRUMENT_DESCRIPTION_CHARS = 1023
    }
}

/**
 * Counts Unicode code points in this string, treating each surrogate pair as a single code
 * point. `String.length` is not used directly because on all Kotlin targets it counts UTF-16
 * code units, which over-counts characters outside the Basic Multilingual Plane.
 */
private fun String.codePointCount(): Int {
    var count = 0
    var index = 0
    while (index < length) {
        index += if (isHighSurrogateAt(index)) {
            2
        } else {
            1
        }
        count++
    }
    return count
}

/**
 * Returns the prefix of this string containing at most [maxCodePoints] Unicode code points,
 * never splitting a surrogate pair.
 */
private fun String.takeCodePoints(maxCodePoints: Int): String {
    var count = 0
    var index = 0
    while (index < length && count < maxCodePoints) {
        index += if (isHighSurrogateAt(index)) {
            2
        } else {
            1
        }
        count++
    }
    return substring(0, index)
}

private fun String.isHighSurrogateAt(index: Int): Boolean {
    val char = this[index]
    return char.isHighSurrogate() && index + 1 < length && this[index + 1].isLowSurrogate()
}
