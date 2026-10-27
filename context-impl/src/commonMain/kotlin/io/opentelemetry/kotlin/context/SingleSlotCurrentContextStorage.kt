package io.opentelemetry.kotlin.context

import kotlin.concurrent.Volatile

/**
 * A simple implementation of [CurrentContextStorage] that only allows one context at any time,
 * and doesn't use thread locals/coroutine context to distinguish between what is current.
 */
public class SingleSlotCurrentContextStorage(
    rootSupplier: () -> Context
) : CurrentContextStorage {

    private val root by lazy { rootSupplier() }

    @Volatile
    private var current: Context? = null

    override fun setCurrent(context: Context) {
        current = context
    }

    override fun current(): Context = current ?: root
}
