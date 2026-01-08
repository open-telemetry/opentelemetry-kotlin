package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.tracing.model.TraceState

/**
 * A factory for creating [TraceState] instances.
 */
@ExperimentalApi
public interface TraceStateFactory {

    /**
     * Retrieves the default TraceState implementation.
     */
    public val default: TraceState
}
