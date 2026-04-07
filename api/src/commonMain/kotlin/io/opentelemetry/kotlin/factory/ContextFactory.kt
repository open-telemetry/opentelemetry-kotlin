package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.context.ContextKey
import io.opentelemetry.kotlin.context.Scope
import io.opentelemetry.kotlin.tracing.Span

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
    public fun implicit(): Context

    /**
     * Stores a span and returns a new [Context], using a pre-defined key.
     */
    public fun storeSpan(context: Context, span: Span): Context

    /**
     * Creates a new [ContextKey] with the given name. The name is used for debugging and does NOT
     * uniquely identify values - use the [ContextKey] itself for that.
     *
     * [T] represents the type of the value that is stored in the context.
     */
    public fun <T> createKey(name: String): ContextKey<T>

    /**
     * Stores [span] in a new [Context] derived from [implicit] and sets
     * the new [Context] as the implicit context.
     *
     * Returns a [Scope]. Neglecting to detach the [Scope] is a programming error.
     *
     * https://opentelemetry.io/docs/specs/otel/trace/api/#context-interaction
     */
    public fun makeCurrent(span: Span): Scope
}
