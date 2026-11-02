package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.context.CurrentContextStorage
import io.opentelemetry.kotlin.context.CurrentContextStorageMode
import io.opentelemetry.kotlin.context.SingleSlotCurrentContextStorage
import io.opentelemetry.kotlin.context.ThreadLocalCurrentContextStorage

internal val DEFAULT_STORAGE_MODE = CurrentContextStorageMode.SINGLE_SLOT

internal class ContextConfigImpl : ContextConfigDsl {
    override var storageMode: CurrentContextStorageMode? = null

    private var customStorage: ((() -> Context) -> CurrentContextStorage)? = null

    override fun storage(action: (rootSupplier: () -> Context) -> CurrentContextStorage) {
        customStorage = action
    }

    internal fun generateStorage(rootSupplier: () -> Context): CurrentContextStorage {
        customStorage?.let { return it(rootSupplier) }
        return when (storageMode ?: DEFAULT_STORAGE_MODE) {
            CurrentContextStorageMode.SINGLE_SLOT -> SingleSlotCurrentContextStorage(rootSupplier)
            CurrentContextStorageMode.THREAD_LOCAL -> ThreadLocalCurrentContextStorage(
                rootSupplier
            )
        }
    }
}
