package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.context.DefaultImplicitContextStorage
import io.opentelemetry.kotlin.context.ImplicitContextStorage
import io.opentelemetry.kotlin.context.ImplicitContextStorageMode
import io.opentelemetry.kotlin.context.ThreadLocalImplicitContextStorage

internal class ContextConfigImpl : ContextConfigDsl {
    override var storageMode: ImplicitContextStorageMode = ImplicitContextStorageMode.GLOBAL

    private var customStorage: ((() -> Context) -> ImplicitContextStorage)? = null

    override fun storage(action: (rootSupplier: () -> Context) -> ImplicitContextStorage) {
        customStorage = action
    }

    internal fun generateStorage(rootSupplier: () -> Context): ImplicitContextStorage {
        customStorage?.let { return it(rootSupplier) }
        return when (storageMode) {
            ImplicitContextStorageMode.GLOBAL -> DefaultImplicitContextStorage(rootSupplier)
            ImplicitContextStorageMode.THREAD_LOCAL -> ThreadLocalImplicitContextStorage(
                rootSupplier
            )
        }
    }
}
