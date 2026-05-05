package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.context.ImplicitContextStorage
import io.opentelemetry.kotlin.context.ImplicitContextStorageMode

/**
 * Defines configuration for Context.
 */
@ExperimentalApi
@ConfigDsl
public interface ContextConfigDsl {

    /**
     * Selects among the built-in [ImplicitContextStorage] implementations. Ignored when a custom
     * implementation is supplied via [storage].
     */
    public var storageMode: ImplicitContextStorageMode

    /**
     * Plugs in a custom [ImplicitContextStorage] implementation. When set, this takes precedence
     * over [storageMode]. May only be called once.
     */
    public fun storage(action: () -> ImplicitContextStorage)
}
