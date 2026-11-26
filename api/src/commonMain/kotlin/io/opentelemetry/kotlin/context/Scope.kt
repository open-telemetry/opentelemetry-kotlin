package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * Defines a 'scope' of execution in which an current Context is set. A scope
 * must be closed when an operation has finished, as this allows the previous context to be
 * restored.
 */
@ExperimentalApi
public interface Scope {

    /**
     * Detaches the scope, which resets the current context to the value that was present
     * at the time this scope was first attached.
     *
     * A scope returned by [Context.attach] after the installed storage (see [installContextStorage])
     * attached the context returns false if the scope was already detached, if it was detached
     * out of order (the current context was no longer the one this scope attached), or if the
     * storage failed to detach it. What an out-of-order detach does to the current context depends
     * on the storage.
     *
     * Other scopes don't detect misuse. A scope returned by [CurrentContextStorage.attach] directly
     * returns whatever that storage returns, and the bundled storages always return true. When no
     * storage is installed, or the storage failed to attach, [Context.attach] changes nothing and
     * its scope always returns true.
     *
     * https://opentelemetry.io/docs/specs/otel/context/#detach-context
     */
    public fun detach(): Boolean
}
