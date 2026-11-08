package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.InstrumentationScopeInfo
import io.opentelemetry.kotlin.NoopOpenTelemetry
import io.opentelemetry.kotlin.ReentrantReadWriteLock
import io.opentelemetry.kotlin.error.SdkError
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.error.SdkErrorSeverity
import io.opentelemetry.kotlin.error.reportError
import io.opentelemetry.kotlin.metrics.instrument.InstrumentDescriptor
import io.opentelemetry.kotlin.metrics.instrument.InstrumentKind
import io.opentelemetry.kotlin.metrics.instrument.InstrumentValueType
import io.opentelemetry.kotlin.resource.Resource

internal class MeterImpl(
    val instrumentationScopeInfo: InstrumentationScopeInfo,
    val resource: Resource,
    private val sdkErrorHandler: SdkErrorHandler,
) : Meter {
    private val lock = ReentrantReadWriteLock()
    private val instrumentRegistry = mutableMapOf<String, InstrumentEntry>()

    /**
     * Test-only hook invoked after the read-lock lookup misses, immediately before the
     * write lock is acquired. Allows tests to deterministically coordinate a write-lock
     * race. Not part of the public API.
     */
    internal var beforeWriteLock: ((instrumentName: String) -> Unit)? = null

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
        val noopMeter = obtainNoopMeterIfInvalid(name)
        if (noopMeter != null) {
            return noopMeter.createDoubleUpDownCounter(name, unit, description)
        }
        return getOrRegisterInstrument(
            name,
            unit,
            description,
            InstrumentValueType.DOUBLE,
            create = {
                DoubleUpDownCounterImpl(name, unit, description)
            },
            createNoop = {
                NoopOpenTelemetry.meterProvider.getMeter("")
                    .createDoubleUpDownCounter(name, unit, description)
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
        return getOrRegisterInstrument(
            name,
            unit,
            description,
            InstrumentValueType.LONG,
            create = {
                LongUpDownCounterImpl(name, unit, description)
            },
            createNoop = {
                NoopOpenTelemetry.meterProvider.getMeter("")
                    .createLongUpDownCounter(name, unit, description)
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
        valueType: InstrumentValueType,
        create: () -> T,
        createNoop: () -> T,
    ): T {
        val requested = InstrumentDescriptor(
            name = name,
            unit = unit,
            description = description,
            kind = InstrumentKind.HISTOGRAM, // Placeholder, only valueType matters for registry
            valueType = valueType,
        )
        val key = name.lowercase()
        var conflict: SdkError? = null
        val entry = lock.read {
            instrumentRegistry[key]?.also { conflict = checkForConflict(requested, it.descriptor) }
        } ?: run {
            beforeWriteLock?.invoke(name)
            lock.write {
                val existing = instrumentRegistry[key]
                if (existing == null) {
                    InstrumentEntry(create(), requested).also { instrumentRegistry[key] = it }
                } else {
                    conflict = checkForConflict(requested, existing.descriptor)
                    existing
                }
            }
        }
        // Conflicts are reported only after the registry lock has been released so the
        // user-provided error handler never executes under the lock.
        conflict?.let(sdkErrorHandler::reportError)
        // A type mismatch cannot reuse the registered instrument; return a noop instead.
        if (entry.descriptor.valueType != valueType) {
            return createNoop()
        }
        @Suppress("UNCHECKED_CAST")
        return entry.instrument as T
    }

    private fun checkForConflict(
        requested: InstrumentDescriptor,
        existing: InstrumentDescriptor,
    ): SdkError? = when {
        requested.valueType != existing.valueType -> SdkError.ApiMisuse(
            api = "Meter",
            message = "Instrument '${requested.name}' is already registered as " +
                "${existing.valueType}, cannot re-register as ${requested.valueType}",
            severity = SdkErrorSeverity.ERROR,
        )
        requested.name != existing.name &&
            requested.name.equals(existing.name, ignoreCase = true) -> SdkError.ApiMisuse(
            api = "Meter",
            message = "Instrument name '${requested.name}' differs only in case " +
                "from existing '${existing.name}'. Using first-seen name.",
            severity = SdkErrorSeverity.ERROR,
        )
        requested != existing -> SdkError.ApiMisuse(
            api = "Meter",
            message = "Instrument '${requested.name}' is already registered with " +
                "different unit or description. Using first-seen descriptor.",
            severity = SdkErrorSeverity.WARNING,
        )
        else -> null
    }

    private data class InstrumentEntry(
        val instrument: Any,
        val descriptor: InstrumentDescriptor,
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

    private companion object {
        /**
         * The maximum length instrument names must support, per
         * https://opentelemetry.io/docs/specs/otel/metrics/api/#instrument-name-syntax.
         */
        const val MAX_INSTRUMENT_NAME_CHARS = 255

        val INSTRUMENT_NAME = Regex("^[A-Za-z][A-Za-z0-9_./-]{0,254}$")
    }
}
