package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.propagation.utils.isValidHex
import io.opentelemetry.kotlin.tracing.TraceFlags
import io.opentelemetry.kotlin.tracing.TraceFlagsImpl

private const val SAMPLED_BIT = 0b00000001
private const val RANDOM_BIT = 0b00000010

@ExperimentalApi
public class TraceFlagsFactoryImpl : TraceFlagsFactory {

    // index by the sampled & random bits, as there can only ever be 4 permutations of this
    // immutable object
    private val flags: Array<TraceFlags> = Array(4) {
        TraceFlagsImpl(isSampled = (it and SAMPLED_BIT) != 0, isRandom = (it and RANDOM_BIT) != 0)
    }

    /**
     * No flags set, per https://www.w3.org/TR/trace-context/#trace-flags
     */
    override val default: TraceFlags = flags[0]

    /**
     * Returns the shared [TraceFlags] instance for the given flags.
     */
    public fun create(isSampled: Boolean, isRandom: Boolean): TraceFlags = when {
        isSampled && isRandom -> flags[SAMPLED_BIT or RANDOM_BIT]
        isSampled -> flags[SAMPLED_BIT]
        isRandom -> flags[RANDOM_BIT]
        else -> default
    }

    override fun fromHex(hex: String): TraceFlags {
        if (hex.length != 2 || !hex.isValidHex()) {
            return default
        }
        return flags[hex.toInt(16) and (SAMPLED_BIT or RANDOM_BIT)]
    }
}
