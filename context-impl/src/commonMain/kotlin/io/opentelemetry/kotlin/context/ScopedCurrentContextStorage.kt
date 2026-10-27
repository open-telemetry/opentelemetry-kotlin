package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * An [CurrentContextStorage] that manages its own scopes, such as one that delegates to another
 * library's context storage. [Context.attach] calls [attach] instead of
 * [CurrentContextStorage.setCurrent], and detaching the returned scope restores the
 * previous context.
 */
@ExperimentalApi
public interface ScopedCurrentContextStorage : CurrentContextStorage {

    /**
     * Makes [context] the current context. Detaching the returned [Scope] restores
     * whatever context was current beforehand.
     */
    public fun attach(context: Context): Scope
}
