package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.context.CurrentContextStorage
import io.opentelemetry.kotlin.context.createSingleSlotCurrentContextStorage

internal class ContextConfigImpl : ContextConfigDsl {
    override var storage: CurrentContextStorage? = null

    /**
     * True if a storage was configured.
     */
    internal val explicitlyConfigured: Boolean
        get() = storage != null

    internal fun generateStorage(): CurrentContextStorage =
        storage ?: createSingleSlotCurrentContextStorage()
}
