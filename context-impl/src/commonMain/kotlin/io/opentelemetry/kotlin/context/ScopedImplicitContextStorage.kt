package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * An [ImplicitContextStorage] that manages its own scopes, such as one that delegates to another
 * library's context storage. [Context.attach] calls [attach] instead of
 * [ImplicitContextStorage.setImplicitContext], and detaching the returned scope restores the
 * previous context.
 */
@ExperimentalApi
public interface ScopedImplicitContextStorage : ImplicitContextStorage {

    /**
     * Makes [context] the current implicit context. Detaching the returned [Scope] restores
     * whatever context was current beforehand.
     */
    public fun attach(context: Context): Scope
}
