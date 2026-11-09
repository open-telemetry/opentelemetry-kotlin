package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.factory.ContextFactory

/**
 * Retrieves the root [Context] from this [ContextFactory].
 *
 * Transitional: call sites will migrate to an api-level `rootContext()` function.
 */
public fun ContextFactory.rootContext(): Context = root()

/**
 * Retrieves the current [Context] from this [ContextFactory], or [rootContext] if none is set.
 *
 * Transitional: call sites will migrate to an api-level `currentContext()` function.
 */
public fun ContextFactory.currentContext(): Context = current()
