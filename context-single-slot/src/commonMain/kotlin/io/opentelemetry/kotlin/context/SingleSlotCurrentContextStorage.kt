package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.ExperimentalApi
import kotlin.concurrent.Volatile

/**
 * Creates a [CurrentContextStorage] that holds one current context for the whole process, shared
 * by every thread and coroutine. When nothing is attached, [CurrentContextStorage.current] returns
 * the root context. This is the SDK's default storage.
 *
 * Each call returns a new, independent instance.
 */
@ExperimentalApi
public fun createSingleSlotCurrentContextStorage(): CurrentContextStorage =
    SingleSlotCurrentContextStorage()

/**
 * A simple implementation of [CurrentContextStorage] that only allows one context at any time,
 * and doesn't use thread locals/coroutine context to distinguish between what is current.
 */
internal class SingleSlotCurrentContextStorage : CurrentContextStorage {

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

    override fun current(): Context = current ?: rootContext()
}
