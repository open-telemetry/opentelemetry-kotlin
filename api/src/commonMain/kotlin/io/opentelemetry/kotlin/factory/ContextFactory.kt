package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.tracing.model.Span

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
    public fun implicitContext(): Context

    /**
     * Stores a span and returns a new [Context], using a pre-defined key.
     */
    public fun storeSpan(context: Context, span: Span): Context
}
