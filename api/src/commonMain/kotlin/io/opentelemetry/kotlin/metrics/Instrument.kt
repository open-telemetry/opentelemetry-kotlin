package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.ThreadSafe

/**
 * Records measurements which are aggregated to metrics.
 *
 * Instruments are obtained from a [Meter] and are identified by their [name], kind, [unit],
 * and [description].
 *
 * https://opentelemetry.io/docs/specs/otel/metrics/api/#instrument
 */
@ExperimentalApi
@ThreadSafe
public interface Instrument {

    /**
     * The name of this instrument, as supplied when it was created.
     */
    public val name: String

    /**
     * The unit of measure for values recorded by this instrument, or null if none was supplied.
     */
    public val unit: String?

    /**
     * A description of this instrument, or null if none was supplied.
     */
    public val description: String?
}
