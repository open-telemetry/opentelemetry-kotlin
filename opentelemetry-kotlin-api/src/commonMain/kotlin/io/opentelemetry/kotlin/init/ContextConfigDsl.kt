package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.context.ImplicitContextStorageMode

/**
 * Defines configuration for Context.
 */
@ExperimentalApi
@ConfigDsl
public interface ContextConfigDsl {

    /**
     * Defines the storage mechanism used for the implicit context.
     */
    public var storageMode: ImplicitContextStorageMode
}
