package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.ThreadLocal

/**
 * An [CurrentContextStorage] that confines the current context to the thread that set it, or root
 * if none is set. On single-threaded targets (JS) the backing [ThreadLocal] degrades to a single
 * shared slot.
 */
public class ThreadLocalCurrentContextStorage(
    rootSupplier: () -> Context,
) : CurrentContextStorage {

    private val root by lazy { rootSupplier() }
    private val current = ThreadLocal<Context>()

    override fun attach(context: Context): Scope {
        val previous = current.get()
        current.set(context)
        return RestoringScope { current.set(previous) }
    }

    override fun current(): Context = current.get() ?: root
}
