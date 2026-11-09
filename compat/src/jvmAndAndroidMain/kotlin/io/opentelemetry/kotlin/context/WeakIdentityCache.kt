package io.opentelemetry.kotlin.context

import java.lang.ref.ReferenceQueue
import java.lang.ref.WeakReference

/**
 * A thread-safe cache whose keys are compared by identity and whose keys and values are both
 * held weakly.
 */
internal class WeakIdentityCache<K : Any, V : Any> {

    private val queue = ReferenceQueue<K>()
    private val map = HashMap<IdentityKey<K>, WeakReference<V>>()

    fun get(key: K): V? = synchronized(map) {
        expungeStaleEntries()
        map[IdentityKey(key)]?.get()
    }

    fun getOrPut(key: K, value: V): V = synchronized(map) {
        expungeStaleEntries()
        val identityKey = IdentityKey(key, queue)
        map[identityKey]?.get() ?: value.also { map[identityKey] = WeakReference(it) }
    }

    private fun expungeStaleEntries() {
        while (true) {
            val ref = queue.poll() ?: return
            map.remove(ref)
        }
    }

    private class IdentityKey<K : Any>(key: K, queue: ReferenceQueue<K>? = null) : WeakReference<K>(key, queue) {
        private val hash = System.identityHashCode(key)

        override fun hashCode(): Int = hash

        override fun equals(other: Any?): Boolean {
            if (this === other) {
                return true
            }
            val referent = get() ?: return false
            return other is IdentityKey<*> && other.get() === referent
        }
    }
}
