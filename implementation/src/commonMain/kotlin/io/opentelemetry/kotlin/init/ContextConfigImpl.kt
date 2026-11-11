package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.context.CurrentContextStorage
import io.opentelemetry.kotlin.context.createSingleSlotCurrentContextStorage
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.error.guardOrDefault
import kotlin.concurrent.Volatile

internal class ContextConfigImpl : ContextConfigDsl {

    @Volatile private var storageSupplier: (() -> CurrentContextStorage)? = null

    override fun storage(action: () -> CurrentContextStorage) {
        storageSupplier = action
    }

    /**
     * True if a storage was configured.
     */
    internal val explicitlyConfigured: Boolean
        get() = storageSupplier != null

    /**
     * Invokes the configured supplier. Falls back to single-slot storage if no supplier was
     * configured, or if the supplier throws.
     */
    internal fun generateStorage(sdkErrorHandler: SdkErrorHandler): CurrentContextStorage {
        val supplier = storageSupplier ?: return createSingleSlotCurrentContextStorage()
        return sdkErrorHandler.guardOrDefault(null, "Failed to create CurrentContextStorage, using default") {
            supplier()
        } ?: createSingleSlotCurrentContextStorage()
    }
}
