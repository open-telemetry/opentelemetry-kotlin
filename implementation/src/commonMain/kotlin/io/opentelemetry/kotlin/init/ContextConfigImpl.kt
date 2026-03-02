package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.context.ImplicitContextStorageMode

internal class ContextConfigImpl : ContextConfigDsl {
    override var storageMode: ImplicitContextStorageMode = ImplicitContextStorageMode.GLOBAL
}
