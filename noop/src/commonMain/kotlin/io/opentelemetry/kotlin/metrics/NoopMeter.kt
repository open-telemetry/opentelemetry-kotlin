package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * No-op implementation of [Meter].
 *
 * This implementation should induce as close to zero overhead as possible.
 */
@ExperimentalApi
internal object NoopMeter : Meter {
    override fun createIntegerCounter(
        name: String,
        description: String?,
        unit: String?
    ): IntegerCounter {
        return NoopIntegerCounter
    }

    override fun createFloatCounter(
        name: String,
        description: String?,
        unit: String?
    ): FloatCounter {
        return NoopFloatCounter
    }

    override fun createLongCounter(
        name: String,
        description: String?,
        unit: String?
    ): LongCounter {
        return NoopLongCounter
    }

    override fun createDoubleCounter(
        name: String,
        description: String?,
        unit: String?
    ): DoubleCounter {
        return NoopDoubleCounter
    }
}
