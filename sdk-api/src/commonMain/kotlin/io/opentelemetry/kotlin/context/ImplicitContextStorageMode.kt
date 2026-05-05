package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * Defines the storage mechanism used for the implicit context.
 */
@ExperimentalApi
public enum class ImplicitContextStorageMode {

    /**
     * Implicit context is stored via an in-memory property. Any thread/coroutine can
     * set the context for any others. This is the default storage mechanism.
     */
    GLOBAL,

    /**
     * Implicit context is stored per-thread i.e. each thread observes its own current context
     * independently of others. This is useful if you primarily use a thread-based execution model
     * rather than using coroutines.
     */
    THREAD_LOCAL
}
