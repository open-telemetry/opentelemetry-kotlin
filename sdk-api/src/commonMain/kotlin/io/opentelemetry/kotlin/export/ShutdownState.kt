package io.opentelemetry.kotlin.export

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * Read-only view of whether a component is shutdown or not.
 */
@ExperimentalApi
public abstract class ShutdownState {

    /**
     * Whether this is shutdown or not
     */
    public abstract val isShutdown: Boolean
}
