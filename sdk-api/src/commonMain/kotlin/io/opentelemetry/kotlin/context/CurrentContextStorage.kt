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
     * Stores the supplied context as the current context.
     */
    public fun setCurrent(context: Context)

    /**
     * Returns the current context, or
     * [io.opentelemetry.kotlin.factory.ContextFactory.root] if none is set.
     */
    public fun current(): Context
}
