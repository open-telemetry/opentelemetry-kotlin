package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.tracing.model.TraceFlags

/**
 * A factory for creating [TraceFlags] instances.
 */
@ExperimentalApi
public interface TraceFlagsFactory {

    /**
     * Retrieves the default TraceFlags implementation.
     */
    public val default: TraceFlags

    /**
     * Creates TraceFlags with the specified sampling and randomization behavior.
     */
    public fun create(sampled: Boolean, random: Boolean): TraceFlags

    /**
     * Creates TraceFlags from a hex string representation.
     * Returns the default TraceFlags implementation if the input is invalid.
     */
    public fun fromHex(hex: String): TraceFlags
}
