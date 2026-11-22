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
     * Supplies the [CurrentContextStorage] that holds the current context. If this is not called,
     * the SDK uses single-slot storage, shared by every thread.
     *
     * The supplier is invoked once when the SDK is created. If it throws, the error is reported
     * to the configured error handler and the single-slot default is used instead.
     *
     * Use a factory such as `createThreadLocalCurrentContextStorage()`, or supply your own
     * implementation. A custom implementation's [CurrentContextStorage.current] must return the
     * root context when nothing is attached.
     */
    public fun storage(action: () -> CurrentContextStorage)
}
