package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.tracing.model.TraceFlags

@OptIn(ExperimentalApi::class)
internal class TraceFlagsImpl(
    override val isSampled: Boolean,
    override val isRandom: Boolean
) : TraceFlags {

    override val hex: String by lazy {
        when {
            isRandom && isSampled -> "03"
            isRandom && !isSampled -> "02"
            !isRandom && isSampled -> "01"
            else -> "00"
        }
    }
}
