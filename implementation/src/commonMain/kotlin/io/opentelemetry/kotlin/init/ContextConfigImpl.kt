package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.context.DefaultImplicitContextStorage
import io.opentelemetry.kotlin.context.ImplicitContextStorage
import io.opentelemetry.kotlin.context.ImplicitContextStorageMode

internal class ContextConfigImpl : ContextConfigDsl {
    override var storageMode: ImplicitContextStorageMode = ImplicitContextStorageMode.GLOBAL

    private var customStorage: (() -> ImplicitContextStorage)? = null

    override fun storage(action: () -> ImplicitContextStorage) {
        customStorage = action
    }

    internal fun generateStorage(rootSupplier: () -> Context): ImplicitContextStorage {
        customStorage?.let { return it() }
        return when (storageMode) {
            ImplicitContextStorageMode.GLOBAL -> DefaultImplicitContextStorage(rootSupplier)
        }
    }
}
