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
     * @param name
     * * Must not be empty
     * * Maximum length of 255 characters
     * * Must start with an alphabetic character
     * * Consists of alphabets, numbers, and the following: '_', '.', '-', '/'
     */
    public fun createIntegerCounter(name: String, description: String? = null, unit: String? = null): IntegerCounter

    /**
     * @param name
     * * Must not be empty
     * * Maximum length of 255 characters
     * * Must start with an alphabetic character
     * * Consists of alphabets, numbers, and the following: '_', '.', '-', '/'
     */
    public fun createLongCounter(name: String, description: String? = null, unit: String? = null): LongCounter

    /**
     * @param name
     * * Must not be empty
     * * Maximum length of 255 characters
     * * Must start with an alphabetic character
     * * Consists of alphabets, numbers, and the following: '_', '.', '-', '/'
     */
    public fun createFloatCounter(name: String, description: String? = null, unit: String? = null): FloatCounter

    /**
     * @param name
     * * Must not be empty
     * * Maximum length of 255 characters
     * * Must start with an alphabetic character
     * * Consists of alphabets, numbers, and the following: '_', '.', '-', '/'
     */
    public fun createDoubleCounter(name: String, description: String? = null, unit: String? = null): DoubleCounter
}
