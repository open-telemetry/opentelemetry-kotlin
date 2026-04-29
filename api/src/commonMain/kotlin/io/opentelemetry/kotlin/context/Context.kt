package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.ThreadSafe
import io.opentelemetry.kotlin.baggage.Baggage

/**
 * Immutable propagation mechanism that carries values across concerns.
 *
 * https://opentelemetry.io/docs/specs/otel/context/
 */
@ExperimentalApi
@ThreadSafe
public interface Context {

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

    /**
     * Returns a new [Context] derived from this one with [baggage] stored under a pre-defined key.
     *
     * https://opentelemetry.io/docs/specs/otel/baggage/api/#context-interaction
     */
    @ThreadSafe
    public fun storeBaggage(baggage: Baggage): Context

    /**
     * Returns the [Baggage] stored in this [Context], or an empty [Baggage] if none is present.
     *
     * https://opentelemetry.io/docs/specs/otel/baggage/api/#context-interaction
     */
    @ThreadSafe
    public fun extractBaggage(): Baggage

    /**
     * Returns a new [Context] derived from this one with all baggage entries removed.
     *
     * https://opentelemetry.io/docs/specs/otel/baggage/api/#context-interaction
     */
    @ThreadSafe
    public fun clearBaggage(): Context
}
