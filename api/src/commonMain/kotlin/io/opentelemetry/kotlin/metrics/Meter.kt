package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.ThreadSafe

/**
 * Provides instruments used to record measurements which are aggregated to metrics.
 *
 * Instruments are obtained through methods provided by this interface.
 *
 * See the [instrument selection guidelines](https://opentelemetry.io/docs/specs/otel/metrics/supplementary-guidelines/#instrument-selection)
 * for help choosing the right instrument.
 *
 * https://opentelemetry.io/docs/specs/otel/metrics/api/#meter
 */
@ExperimentalApi
@ThreadSafe
public interface Meter {

    /**
     * Creates a [DoubleUpDownCounter] for recording signed floating-point increments and decrements.
     *
     * [name] is required and should conform to the
     * [instrument name syntax](https://opentelemetry.io/docs/specs/otel/metrics/api/#instrument-name-syntax).
     * [unit] and [description] are optional.
     *
     * https://opentelemetry.io/docs/specs/otel/metrics/api/#updowncounter-creation
     */
    @ThreadSafe
    public fun createDoubleUpDownCounter(
        name: String,
        unit: String? = null,
        description: String? = null,
    ): DoubleUpDownCounter

    /**
     * Creates a [LongUpDownCounter] for recording signed integer increments and decrements.
     *
     * [name] is required and should conform to the
     * [instrument name syntax](https://opentelemetry.io/docs/specs/otel/metrics/api/#instrument-name-syntax).
     * [unit] and [description] are optional.
     *
     * https://opentelemetry.io/docs/specs/otel/metrics/api/#updowncounter-creation
     */
    @ThreadSafe
    public fun createLongUpDownCounter(
        name: String,
        unit: String? = null,
        description: String? = null,
    ): LongUpDownCounter

    /**
     * Creates an [ObservableDoubleCounter] for reporting monotonically increasing values during
     * metric collection.
     *
     * [name] is required and should conform to the
     * [instrument name syntax](https://opentelemetry.io/docs/specs/otel/metrics/api/#instrument-name-syntax).
     * [unit] and [description] are optional. [callbacks] are registered with the instrument at
     * creation and remain registered until the instrument is closed.
     *
     * https://opentelemetry.io/docs/specs/otel/metrics/api/#asynchronous-counter-creation
     */
    @ThreadSafe
    public fun createObservableDoubleCounter(
        name: String,
        unit: String? = null,
        description: String? = null,
        vararg callbacks: (ObservableDoubleMeasurement) -> Unit,
    ): ObservableDoubleCounter
}
