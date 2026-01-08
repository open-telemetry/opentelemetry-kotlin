package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.ThreadSafe

/**
 * Immutable propagation mechanism that carries values across concerns.
 *
 * https://opentelemetry.io/docs/specs/otel/context/
 */
@ExperimentalApi
@ThreadSafe
public interface Context {

    /**
     * Creates a new [ContextKey] with the given name. The name is used for debugging and does NOT uniquely identify
     * values - use the [ContextKey] itself for that.
     *
     * [T] represents the type of the value that is stored in the context.
     */
    @ThreadSafe
    public fun <T> createKey(name: String): ContextKey<T>

    /**
     * Associates a value on the [Context] with the given [ContextKey].
     *
     * [T] represents the type of the value that is stored in the context.
     *
     * This function returns a new immutable [Context] that contains the key-value pair.
     */
    @ThreadSafe
    public fun <T> set(key: ContextKey<T>, value: T?): Context

    /**
     * Retrieves a value from the [Context] associated with the given [ContextKey].
     *
     * [T] represents the type of the value that is stored in the context.
     */
    @ThreadSafe
    public fun <T> get(key: ContextKey<T>): T?

    /**
     * Sets the supplied context as the current implicit context. A [Scope] object
     * is returned; when the [Scope] is closed the previous implicit context
     * will be restored.
     *
     * Neglecting to close the [Scope] once the 'execution unit' of the context
     * has finished is considered a programming error that will leak resources.
     *
     * https://opentelemetry.io/docs/specs/otel/context/#attach-context
     */
    @ThreadSafe
    public fun attach(): Scope
}
