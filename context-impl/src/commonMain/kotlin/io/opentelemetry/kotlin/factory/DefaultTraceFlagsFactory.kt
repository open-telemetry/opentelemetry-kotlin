package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.tracing.TraceFlags

@OptIn(ExperimentalApi::class)
private val impl = TraceFlagsFactoryImpl()

/**
 * The default implementation of [TraceFlagsFactory].
 */
@OptIn(ExperimentalApi::class)
public object DefaultTraceFlagsFactory : TraceFlagsFactory by impl {

    /**
     * Returns the shared [TraceFlags] instance for the given flags.
     */
    public fun create(isSampled: Boolean, isRandom: Boolean): TraceFlags = impl.create(isSampled, isRandom)
}
