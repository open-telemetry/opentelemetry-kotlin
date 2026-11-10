package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.ThreadLocal

/**
 * Creates a [CurrentContextStorage] that confines the current context to the thread that set it.
 * When nothing is attached on a thread, [CurrentContextStorage.current] returns the root context.
 * On single-threaded targets (JS) it degrades to a single shared slot.
 *
 * Each call returns a new, independent instance: contexts attached to one are never visible
 * through another.
 */
@ExperimentalApi
public fun createThreadLocalCurrentContextStorage(): CurrentContextStorage =
    ThreadLocalCurrentContextStorage()

/**
 * An [CurrentContextStorage] that confines the current context to the thread that set it, or root
 * if none is set. On single-threaded targets (JS) the backing [ThreadLocal] degrades to a single
 * shared slot.
 */
@OptIn(TempContextStandInApi::class)
internal class ThreadLocalCurrentContextStorage : CurrentContextStorage {

    private val current = ThreadLocal<Context>()

    override fun attach(context: Context): Scope {
        val previous = current.get()
        current.set(context)
        return object : Scope {
            override fun detach(): Boolean {
                current.set(previous)
                return true
            }
        }
    }

    override fun current(): Context = current.get() ?: internalRootContext()
}
