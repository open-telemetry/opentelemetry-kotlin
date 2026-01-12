package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.context.ImplicitContextStorageMode

@OptIn(ExperimentalApi::class)
internal class ContextConfigImpl : ContextConfigDsl {
    override var storageMode: ImplicitContextStorageMode = ImplicitContextStorageMode.GLOBAL
}
