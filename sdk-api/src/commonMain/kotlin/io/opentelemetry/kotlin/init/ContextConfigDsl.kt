package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.context.CurrentContextStorage

/**
 * Defines configuration for Context. This is ignored in compat mode.
 */
@ExperimentalApi
@ConfigDsl
public interface ContextConfigDsl {

    /**
     * The [CurrentContextStorage] that holds the current context. When `null` (the default), the
     * SDK uses single-slot storage, shared by every thread.
     *
     * Use a factory such as `createThreadLocalCurrentContextStorage()`, or supply your own
     * implementation. A custom implementation's [CurrentContextStorage.current] must return the
     * root context when nothing is attached.
     */
    public var storage: CurrentContextStorage?
}
