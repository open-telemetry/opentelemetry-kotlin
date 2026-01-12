package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * Defines a 'scope' of execution in which an implicit Context is set. A scope
 * must be closed when an operation has finished, as this allows the previous context to be
 * restored.
 */
@ExperimentalApi
public interface Scope {

    /**
     * Detaches the scope, which resets the implicit context to the value that was present
     * at the time this scope was first attached.
     *
     * https://opentelemetry.io/docs/specs/otel/context/#detach-context
     */
    public fun detach()
}
