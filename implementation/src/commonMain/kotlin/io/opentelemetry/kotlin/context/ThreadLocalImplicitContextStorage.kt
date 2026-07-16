package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.ThreadLocal

/**
 * An [ImplicitContextStorage] that confines the current context to the thread that set it, or root
 * if none is set. On single-threaded targets (JS) the backing [ThreadLocal] degrades to a single
 * shared slot.
 */
internal class ThreadLocalImplicitContextStorage(
    rootSupplier: () -> Context,
) : ImplicitContextStorage {

    private val root by lazy { rootSupplier() }
    private val current = ThreadLocal<Context>()

    override fun setImplicitContext(context: Context) {
        current.set(context)
    }

    override fun implicitContext(): Context = current.get() ?: root
}
