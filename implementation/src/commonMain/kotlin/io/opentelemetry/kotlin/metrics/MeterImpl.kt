package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.InstrumentationScopeInfo
import io.opentelemetry.kotlin.NoopOpenTelemetry
import io.opentelemetry.kotlin.ReentrantReadWriteLock
import io.opentelemetry.kotlin.error.SdkError
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.error.SdkErrorSeverity
import io.opentelemetry.kotlin.error.reportError
import io.opentelemetry.kotlin.internal.codePointCount
import io.opentelemetry.kotlin.internal.takeCodePoints
import io.opentelemetry.kotlin.resource.Resource

internal class MeterImpl(
    val instrumentationScopeInfo: InstrumentationScopeInfo,
    val resource: Resource,
    private val sdkErrorHandler: SdkErrorHandler,
) : Meter {
    private val lock = ReentrantReadWriteLock()
    private val instrumentRegistry = mutableMapOf<String, InstrumentEntry>()

    override fun createLongGauge(
        name: String,
        unit: String?,
        description: String?,
    ): LongGauge {
        val noopMeter = obtainNoopMeterIfInvalid(name) ?: return LongGaugeImpl(
            name,
            sanitizeInstrumentUnit(unit),
            sanitizeInstrumentDescription(description),
        )
        return noopMeter.createLongGauge(name, unit, description)
    }

    override fun createDoubleGauge(
        name: String,
        unit: String?,
        description: String?,
    ): DoubleGauge {
        val noopMeter = obtainNoopMeterIfInvalid(name) ?: return DoubleGaugeImpl(
            name,
            sanitizeInstrumentUnit(unit),
            sanitizeInstrumentDescription(description),
        )
        return noopMeter.createDoubleGauge(name, unit, description)
    }
    override fun createDoubleUpDownCounter(
        name: String,
        unit: String?,
        description: String?,
    ): DoubleUpDownCounter {
        val noopMeter = obtainNoopMeterIfInvalid(name)
        if (noopMeter != null) {
            return noopMeter.createDoubleUpDownCounter(name, unit, description)
        }
        val sanitizedUnit = sanitizeInstrumentUnit(unit)
        val sanitizedDescription = sanitizeInstrumentDescription(description)
        return getOrRegisterInstrument(
            name,
            sanitizedUnit,
            sanitizedDescription,
            InstrumentType.DOUBLE_UP_DOWN_COUNTER,
            create = {
                DoubleUpDownCounterImpl(name, sanitizedUnit, sanitizedDescription)
            },
            createNoop = {
                NoopOpenTelemetry.meterProvider.getMeter("")
                    .createDoubleUpDownCounter(name, sanitizedUnit, sanitizedDescription)
            },
        )
    }

    override fun createLongUpDownCounter(
        name: String,
        unit: String?,
        description: String?,
    ): LongUpDownCounter {
        val noopMeter = obtainNoopMeterIfInvalid(name)
        if (noopMeter != null) {
            return noopMeter.createLongUpDownCounter(name, unit, description)
        }
        val sanitizedUnit = sanitizeInstrumentUnit(unit)
        val sanitizedDescription = sanitizeInstrumentDescription(description)
        return getOrRegisterInstrument(
            name,
            sanitizedUnit,
            sanitizedDescription,
            InstrumentType.LONG_UP_DOWN_COUNTER,
            create = {
                LongUpDownCounterImpl(name, sanitizedUnit, sanitizedDescription)
            },
            createNoop = {
                NoopOpenTelemetry.meterProvider.getMeter("")
                    .createLongUpDownCounter(name, sanitizedUnit, sanitizedDescription)
            },
        )
    }

    override fun createObservableDoubleCounter(
        name: String,
        unit: String?,
        description: String?,
        vararg callbacks: (ObservableDoubleMeasurement) -> Unit,
    ): ObservableDoubleCounter {
        val noopMeter = obtainNoopMeterIfInvalid(name) ?: getNoopMeter()

        return noopMeter.createObservableDoubleCounter(name, unit, description, *callbacks)
    }

    override fun createObservableLongCounter(
        name: String,
        unit: String?,
        description: String?,
        vararg callbacks: (ObservableLongMeasurement) -> Unit,
    ): ObservableLongCounter {
        val noopMeter = obtainNoopMeterIfInvalid(name) ?: getNoopMeter()

        return noopMeter.createObservableLongCounter(name, unit, description, *callbacks)
    }

    override fun createObservableLongUpDownCounter(
        name: String,
        unit: String?,
        description: String?,
        vararg callbacks: (ObservableLongMeasurement) -> Unit,
    ): ObservableLongUpDownCounter {
        val noopMeter = obtainNoopMeterIfInvalid(name) ?: getNoopMeter()
        return noopMeter.createObservableLongUpDownCounter(name, unit, description, *callbacks)
    }

    override fun createObservableDoubleUpDownCounter(
        name: String,
        unit: String?,
        description: String?,
        vararg callbacks: (ObservableDoubleMeasurement) -> Unit,
    ): ObservableDoubleUpDownCounter {
        val noopMeter = obtainNoopMeterIfInvalid(name) ?: getNoopMeter()
        return noopMeter.createObservableDoubleUpDownCounter(name, unit, description, *callbacks)
    }

    override fun createObservableLongGauge(
        name: String,
        unit: String?,
        description: String?,
        vararg callbacks: (ObservableLongMeasurement) -> Unit,
    ): ObservableLongGauge {
        val noopMeter = obtainNoopMeterIfInvalid(name) ?: getNoopMeter()
        return noopMeter.createObservableLongGauge(name, unit, description, *callbacks)
    }

    private fun <T : Any> getOrRegisterInstrument(
        name: String,
        unit: String?,
        description: String?,
        type: InstrumentType,
        create: () -> T,
        createNoop: () -> T,
    ): T {
        val key = name.lowercase()

        // First check read-only
        val existing = lock.read {
            instrumentRegistry[key]
        }

        if (existing != null) {
            if (existing.type != type) {
                sdkErrorHandler.reportError(
                    SdkError.ApiMisuse(
                        api = "Meter",
                        message = "Instrument '$name' is already registered as ${existing.type}, " +
                            "cannot re-register as $type",
                        severity = SdkErrorSeverity.ERROR,
                    ),
                )
                // Return a noop instrument when types don't match
                return createNoop()
            }
            // Return existing instrument even if unit/description differ - per spec
            @Suppress("UNCHECKED_CAST")
            return existing.instrument as T
        }

        // Double-check and insert write-only
        return lock.write {
            val doubleCheck = instrumentRegistry[key]
            if (doubleCheck != null) {
                if (doubleCheck.type != type) {
                    return@write createNoop()
                }
                @Suppress("UNCHECKED_CAST")
                return@write doubleCheck.instrument as T
            }
            val instrument = create()
            instrumentRegistry[key] = InstrumentEntry(instrument, type, unit, description)
            instrument
        }
    }

    private enum class InstrumentType {
        DOUBLE_UP_DOWN_COUNTER,
        LONG_UP_DOWN_COUNTER,
    }

    private data class InstrumentEntry(
        val instrument: Any,
        val type: InstrumentType,
        val unit: String?,
        val description: String?,
    )

    /**
     * Returns a noop meter if [name] is not a valid
     * [instrument name](https://opentelemetry.io/docs/specs/otel/metrics/api/#instrument-name-syntax),
     * so callers fall back to a noop instrument rather than destabilizing the host or risking a
     * name collision from rewriting. Returns `null` when [name] is valid.
     */
    private fun obtainNoopMeterIfInvalid(name: String): Meter? = if (isValidInstrumentName(name)) {
        null
    } else {
        getNoopMeter()
    }

    private fun getNoopMeter(): Meter = NoopOpenTelemetry.meterProvider.getMeter("")

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
