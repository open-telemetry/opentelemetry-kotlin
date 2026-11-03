package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.context.CurrentContextStorage
import io.opentelemetry.kotlin.context.CurrentContextStorageMode

/**
 * Defines configuration for Context. This is ignored in compat mode.
 */
@ExperimentalApi
@ConfigDsl
public interface ContextConfigDsl {

    /**
     * Selects among the built-in [CurrentContextStorage] implementations. Ignored when a custom
     * implementation is supplied via [storage].
     */
    public var storageMode: CurrentContextStorageMode?

    /**
     * Plugs in a custom [CurrentContextStorage] implementation. When set, this takes precedence
     * over [storageMode]. May only be called once.
     *
     * The [action] receives a `rootSupplier` that provides the root [Context], allowing the
     * custom implementation to fall back to the root context when none is set.
     */
    public fun storage(action: (rootSupplier: () -> Context) -> CurrentContextStorage)
}
