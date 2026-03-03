package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.tracing.model.TraceFlags

internal class TraceFlagsImpl(
    override val isSampled: Boolean,
    override val isRandom: Boolean
) : TraceFlags
