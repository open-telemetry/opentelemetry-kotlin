package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.ExperimentalApi

@OptIn(ExperimentalApi::class)
internal class TraceFlagsImpl(
    override val isSampled: Boolean,
    override val isRandom: Boolean
) : TraceFlags {

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other !is TraceFlagsImpl) {
            return false
        }
        return isSampled == other.isSampled && isRandom == other.isRandom
    }

    override fun hashCode(): Int = 31 * isSampled.hashCode() + isRandom.hashCode()

    override fun toString(): String = "TraceFlagsImpl(isSampled=$isSampled, isRandom=$isRandom)"
}
