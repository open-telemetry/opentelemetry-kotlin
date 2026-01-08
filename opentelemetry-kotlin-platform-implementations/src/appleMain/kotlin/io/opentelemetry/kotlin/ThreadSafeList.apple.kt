package io.opentelemetry.kotlin

public actual fun <T> threadSafeList(): MutableList<T> = ThreadSafeListImpl()

private class ThreadSafeListImpl<T>(
    private val lock: ReentrantReadWriteLock = ReentrantReadWriteLock(),
    private val impl: MutableList<T> = mutableListOf(),
) : MutableList<T> {

    override val size: Int
        get() = lock.read { impl.size }

    override fun contains(element: T): Boolean = lock.read {
        impl.contains(element)
    }

    override fun containsAll(elements: Collection<T>): Boolean = lock.read {
        impl.containsAll(elements)
    }

    override fun get(index: Int): T = lock.read {
        impl[index]
    }

    override fun indexOf(element: T): Int = lock.read {
        impl.indexOf(element)
    }

    override fun isEmpty(): Boolean = lock.read {
        impl.isEmpty()
    }

    override fun iterator(): MutableIterator<T> = lock.read {
        impl.toMutableList().iterator()
    }

    override fun lastIndexOf(element: T): Int = lock.read {
        impl.lastIndexOf(element)
    }

    override fun add(element: T): Boolean = lock.write {
        impl.add(element)
    }

    override fun add(index: Int, element: T) = lock.write {
        impl.add(index, element)
    }

    override fun addAll(index: Int, elements: Collection<T>): Boolean = lock.write {
        impl.addAll(index, elements)
    }

    override fun addAll(elements: Collection<T>): Boolean = lock.write {
        impl.addAll(elements)
    }

    override fun clear() = lock.write {
        impl.clear()
    }

    override fun listIterator(): MutableListIterator<T> = lock.read {
        impl.toMutableList().listIterator()
    }

    override fun listIterator(index: Int): MutableListIterator<T> = lock.read {
        impl.toMutableList().listIterator(index)
    }

    override fun remove(element: T): Boolean = lock.write {
        impl.remove(element)
    }

    override fun removeAll(elements: Collection<T>): Boolean = lock.write {
        impl.removeAll(elements)
    }

    override fun removeAt(index: Int): T = lock.write {
        impl.removeAt(index)
    }

    override fun retainAll(elements: Collection<T>): Boolean = lock.write {
        impl.retainAll(elements)
    }

    override fun set(index: Int, element: T): T = lock.write {
        impl.set(index, element)
    }

    override fun subList(fromIndex: Int, toIndex: Int): MutableList<T> = lock.read {
        impl.toMutableList().subList(fromIndex, toIndex)
    }
}
