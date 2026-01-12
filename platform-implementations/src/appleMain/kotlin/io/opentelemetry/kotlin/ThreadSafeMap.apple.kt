package io.opentelemetry.kotlin

public actual fun <K, V> threadSafeMap(): MutableMap<K, V> = ThreadSafeMapImpl()

private class ThreadSafeMapImpl<K, V>(
    private val lock: ReentrantReadWriteLock = ReentrantReadWriteLock(),
    private val impl: MutableMap<K, V> = mutableMapOf(),
) : MutableMap<K, V> {

    override val size: Int
        get() = lock.read {
            impl.size
        }

    override fun isEmpty(): Boolean = lock.read {
        impl.isEmpty()
    }

    override fun containsKey(key: K): Boolean = lock.read {
        impl.containsKey(key)
    }

    override fun containsValue(value: V): Boolean = lock.read {
        impl.containsValue(value)
    }

    override fun get(key: K): V? = lock.read {
        impl[key]
    }

    override val entries: MutableSet<MutableMap.MutableEntry<K, V>>
        get() = lock.read {
            impl.entries.toMutableSet()
        }

    override val keys: MutableSet<K>
        get() = lock.read {
            impl.keys.toMutableSet()
        }

    override val values: MutableCollection<V>
        get() = lock.read {
            impl.values.toMutableList()
        }

    override fun put(key: K, value: V): V? = lock.write {
        impl.put(key, value)
    }

    override fun putAll(from: Map<out K, V>) = lock.write {
        impl.putAll(from)
    }

    override fun remove(key: K): V? = lock.write {
        impl.remove(key)
    }

    override fun clear() = lock.write {
        impl.clear()
    }
}
