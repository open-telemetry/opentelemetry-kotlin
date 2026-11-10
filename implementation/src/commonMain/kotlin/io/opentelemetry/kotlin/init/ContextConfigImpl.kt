package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.context.CurrentContextStorage
import io.opentelemetry.kotlin.context.createSingleSlotCurrentContextStorage

internal class ContextConfigImpl : ContextConfigDsl {
    override var storage: CurrentContextStorage? = null

    internal fun generateStorage(): CurrentContextStorage =
        storage ?: createSingleSlotCurrentContextStorage()
}
