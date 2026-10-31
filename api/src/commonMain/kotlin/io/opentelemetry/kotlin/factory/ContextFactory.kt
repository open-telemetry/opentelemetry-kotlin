package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.context.Context

/**
 * A factory for retrieving [Context] instances.
 */
@ExperimentalApi
public interface ContextFactory {

    /**
     * Retrieves the root Context.
     */
    public fun root(): Context

    /**
     * Retrieves the implicit Context, or [root] if none is currently set.
     */
    public fun current(): Context
}
