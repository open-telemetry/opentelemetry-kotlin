package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * Defines a storage mechanism for the current Context.
 *
 * https://opentelemetry.io/docs/specs/otel/context/#optional-global-operations
 */
@ExperimentalApi
public interface CurrentContextStorage {

    /**
     * Returns the current context. When nothing is attached this returns the root context.
     */
    public fun current(): Context

    /**
     * Attaches [context] to this storage. Detaching the returned [Scope] restores the state this
     * storage had before the call.
     */
    public fun attach(context: Context): Scope
}
