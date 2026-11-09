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
    private val instrumentRegistry = mutableMapOf<String, MutableList<InstrumentEntry>>()

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
                DoubleUpDownCounterImpl(it, unit, description)
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
                LongUpDownCounterImpl(it, unit, description)
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
        create: (resolvedName: String) -> T,
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
        // The read lookup reuses an existing registration when possible; anything else
        // (new name or conflicting descriptor) takes the write lock to register.
        val found = lock.read { instrumentRegistry[key]?.let { lookupEntry(it, requested) } }
        val entry = if (found != null) {
            conflict = found.second
            found.first
        } else {
            beforeWriteLock?.invoke(name)
            lock.write {
                val entries = instrumentRegistry.getOrPut(key) { mutableListOf() }
                val resolved = lookupEntry(entries, requested)
                if (resolved != null) {
                    conflict = resolved.second
                    resolved.first
                } else {
                    val baseline = entries.firstOrNull()
                    conflict = baseline?.let { conflictFor(entries, requested) }
                    // Conflicting registrations keep the bucket's first-seen name casing
                    // while retaining their own unit, description, and value type.
                    val resolvedName = baseline?.descriptor?.name ?: requested.name
                    val resolvedDescriptor = InstrumentDescriptor(
                        name = resolvedName,
                        unit = unit,
                        description = description,
                        kind = InstrumentKind.HISTOGRAM,
                        valueType = valueType,
                    )
                    InstrumentEntry(create(resolvedName), resolvedDescriptor).also { entries += it }
                }
            }
        }
        // Conflicts are reported only after the registry lock has been released so the
        // user-provided error handler never executes under the lock.
        conflict?.let(sdkErrorHandler::reportError)
        @Suppress("UNCHECKED_CAST")
        return entry.instrument as T
    }

    /**
     * Finds an existing [entries] registration reusable for [requested]. Returns the entry
     * paired with the conflict to report, or `null` when a new instrument must be created.
     * An identical descriptor reuses silently; a name differing only in case reuses the
     * first-seen registration and reports the case difference.
     */
    private fun lookupEntry(
        entries: List<InstrumentEntry>,
        requested: InstrumentDescriptor,
    ): Pair<InstrumentEntry, SdkError?>? {
        // InstrumentDescriptor.equals is case-insensitive, so an exact-name match is
        // checked first; a case-only variant then reuses the first-seen registration.
        entries.firstOrNull { it.descriptor == requested && it.descriptor.name == requested.name }
            ?.let { return it to null }
        entries.firstOrNull { it.descriptor == requested }?.let { entry ->
            return entry to SdkError.ApiMisuse(
                api = "Meter",
                message = "Instrument name '${requested.name}' differs only in case " +
                    "from existing '${entry.descriptor.name}'. Using first-seen name.",
                severity = SdkErrorSeverity.ERROR,
            )
        }
        return null
    }

    /**
     * Builds the conflict diagnostic for a registration that cannot reuse any existing
     * entry. Prefers an entry with the same value type so unit/description mismatches are
     * reported as warnings; otherwise reports the numeric-type conflict as an error.
     */
    private fun conflictFor(
        entries: List<InstrumentEntry>,
        requested: InstrumentDescriptor,
    ): SdkError {
        val baseline = entries.firstOrNull { it.descriptor.valueType == requested.valueType }
            ?: entries.first()
        return checkForConflict(requested, baseline.descriptor)
            ?: error("conflictFor called for a descriptor with no conflict")
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
