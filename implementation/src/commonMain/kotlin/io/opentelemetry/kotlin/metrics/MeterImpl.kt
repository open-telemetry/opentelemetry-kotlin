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
            description,
        )
        return noopMeter.createDoubleUpDownCounter(name, unit, description)
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
    }
}
