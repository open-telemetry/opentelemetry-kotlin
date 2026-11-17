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
    override fun createLongGauge(
        name: String,
        unit: String?,
        description: String?,
    ): LongGauge {
        val noopMeter = obtainNoopMeterIfInvalid(name) ?: return LongGaugeImpl(
            name,
            unit,
            description,
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
            unit,
            description,
        )
        return noopMeter.createDoubleGauge(name, unit, description)
    }

    override fun createDoubleUpDownCounter(
        name: String,
        unit: String?,
        description: String?,
    ): DoubleUpDownCounter {
        val noopMeter = obtainNoopMeterIfInvalid(name) ?: return DoubleUpDownCounterImpl(
            name,
            unit,
            description,
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
            unit,
            description,
        )
        return noopMeter.createLongUpDownCounter(name, unit, description)
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

    override fun createObservableDoubleGauge(
        name: String,
        unit: String?,
        description: String?,
        vararg callbacks: (ObservableDoubleMeasurement) -> Unit,
    ): ObservableDoubleGauge {
        val noopMeter = obtainNoopMeterIfInvalid(name) ?: getNoopMeter()
        return noopMeter.createObservableDoubleGauge(name, unit, description, *callbacks)
    }

    override fun batchCallback(
        measurement: ObservableMeasurement,
        vararg additionalMeasurements: ObservableMeasurement,
        callback: () -> Unit,
    ): AutoCloseable {
        val noopMeter = getNoopMeter()
        return noopMeter.batchCallback(measurement, *additionalMeasurements, callback = callback)
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

    private companion object {
        /**
         * The maximum length instrument names must support, per
         * https://opentelemetry.io/docs/specs/otel/metrics/api/#instrument-name-syntax.
         */
        const val MAX_INSTRUMENT_NAME_CHARS = 255

        val INSTRUMENT_NAME = Regex("^[A-Za-z][A-Za-z0-9_./-]{0,254}$")
    }
}
