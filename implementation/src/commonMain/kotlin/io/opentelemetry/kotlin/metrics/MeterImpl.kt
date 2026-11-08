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
            InstrumentType.DOUBLE_UP_DOWN_COUNTER,
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
            InstrumentType.LONG_UP_DOWN_COUNTER,
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
        type: InstrumentType,
        create: () -> T,
        createNoop: () -> T,
    ): T {
        val requestedDescriptor = InstrumentDescriptor(
            name = name,
            unit = unit,
            description = description,
            kind = InstrumentKind.HISTOGRAM, // Placeholder, only valueType matters for registry
            valueType = type.toInstrumentValueType(),
        )
        val key = name.lowercase()

        // First check read-only
        val (existingEntry, conflictInfo) = lock.read {
            val entry = instrumentRegistry[key]
            if (entry != null) {
                val conflict = checkForConflict(requestedDescriptor, entry.descriptor, entry.instrumentName)
                Pair(entry, conflict)
            } else {
                Pair(null, null)
            }
        }

        // Report any conflict found during read lock (outside the lock)
        conflictInfo?.let { reportConflict(it) }

        if (existingEntry != null) {
            // If valueTypes don't match, return a noop instrument
            if (existingEntry.descriptor.valueType != requestedDescriptor.valueType) {
                return createNoop()
            }
            // Return existing instrument even if unit/description differ - per spec
            @Suppress("UNCHECKED_CAST")
            return existingEntry.instrument as T
        }

        // Double-check and insert write-only
        val (registeredEntry, writeConflictInfo) = lock.write {
            val doubleCheck = instrumentRegistry[key]
            if (doubleCheck != null) {
                val conflict = checkForConflict(requestedDescriptor, doubleCheck.descriptor, doubleCheck.instrumentName)
                Pair(doubleCheck, conflict)
            } else {
                val instrument = create()
                val entry = InstrumentEntry(
                    instrument = instrument,
                    descriptor = requestedDescriptor,
                    instrumentName = name,
                )
                instrumentRegistry[key] = entry
                Pair(entry, null)
            }
        }

        // Report any conflict found during write lock (outside the lock)
        writeConflictInfo?.let { reportConflict(it) }

        // If valueTypes don't match after write lock race, return a noop instrument
        if (registeredEntry.descriptor.valueType != requestedDescriptor.valueType) {
            return createNoop()
        }

        @Suppress("UNCHECKED_CAST")
        return registeredEntry.instrument as T
    }

    private fun checkForConflict(
        requested: InstrumentDescriptor,
        existing: InstrumentDescriptor,
        existingName: String,
    ): ConflictInfo? {
        // Check for valueType mismatch (high severity)
        if (requested.valueType != existing.valueType) {
            return ConflictInfo(
                type = ConflictType.TYPE_MISMATCH,
                requestedName = requested.name,
                existingName = existingName,
                requestedValueType = requested.valueType,
                existingValueType = existing.valueType,
            )
        }

        // Check for case-only name difference (medium severity)
        if (requested.name != existingName && requested.name.equals(existingName, ignoreCase = true)) {
            return ConflictInfo(
                type = ConflictType.CASE_ONLY_DIFFERENCE,
                requestedName = requested.name,
                existingName = existingName,
                requestedValueType = requested.valueType,
                existingValueType = existing.valueType,
            )
        }

        // Check for descriptor mismatch (unit/description difference) - warning only
        if (requested != existing) {
            return ConflictInfo(
                type = ConflictType.DESCRIPTOR_MISMATCH,
                requestedName = requested.name,
                existingName = existingName,
                requestedValueType = requested.valueType,
                existingValueType = existing.valueType,
            )
        }

        return null
    }

    private fun reportConflict(conflict: ConflictInfo) {
        when (conflict.type) {
            ConflictType.TYPE_MISMATCH -> {
                sdkErrorHandler.reportError(
                    SdkError.ApiMisuse(
                        api = "Meter",
                        message = "Instrument '${conflict.requestedName}' is already registered as " +
                            "${conflict.existingValueType}, " +
                            "cannot re-register as ${conflict.requestedValueType}",
                        severity = SdkErrorSeverity.ERROR,
                    ),
                )
            }
            ConflictType.CASE_ONLY_DIFFERENCE -> {
                sdkErrorHandler.reportError(
                    SdkError.ApiMisuse(
                        api = "Meter",
                        message = "Instrument name '${conflict.requestedName}' differs only in case " +
                            "from existing '${conflict.existingName}'. Using first-seen name.",
                        severity = SdkErrorSeverity.ERROR,
                    ),
                )
            }
            ConflictType.DESCRIPTOR_MISMATCH -> {
                sdkErrorHandler.reportError(
                    SdkError.ApiMisuse(
                        api = "Meter",
                        message = "Instrument '${conflict.requestedName}' is already registered with " +
                            "different unit or description. Using first-seen descriptor.",
                        severity = SdkErrorSeverity.WARNING,
                    ),
                )
            }
        }
    }

    private enum class ConflictType {
        TYPE_MISMATCH,
        CASE_ONLY_DIFFERENCE,
        DESCRIPTOR_MISMATCH;
    }

    private data class ConflictInfo(
        val type: ConflictType,
        val requestedName: String,
        val existingName: String,
        val requestedValueType: InstrumentValueType,
        val existingValueType: InstrumentValueType,
    )

    private enum class InstrumentType {
        DOUBLE_UP_DOWN_COUNTER,
        LONG_UP_DOWN_COUNTER;

        fun toInstrumentValueType(): InstrumentValueType = when (this) {
            DOUBLE_UP_DOWN_COUNTER -> InstrumentValueType.DOUBLE
            LONG_UP_DOWN_COUNTER -> InstrumentValueType.LONG
        }
    }

    private data class InstrumentEntry(
        val instrument: Any,
        val descriptor: InstrumentDescriptor,
        val instrumentName: String,
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
