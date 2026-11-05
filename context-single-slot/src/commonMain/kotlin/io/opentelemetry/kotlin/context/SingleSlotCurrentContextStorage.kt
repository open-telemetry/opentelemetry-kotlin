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

    override fun attach(context: Context): Scope {
        val previous = current
        current = context
        return object : Scope {
            override fun detach(): Boolean {
                current = previous
                return true
            }
        }
    }

    override fun current(): Context = current ?: root
}
