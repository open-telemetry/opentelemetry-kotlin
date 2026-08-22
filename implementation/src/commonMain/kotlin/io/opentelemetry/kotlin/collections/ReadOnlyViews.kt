package io.opentelemetry.kotlin.collections

internal class ReadOnlyListView<T>(
    private val delegate: List<T>
) : AbstractList<T>() {
    override val size: Int
        get() = delegate.size

    override fun get(index: Int): T = delegate[index]
}

internal class ReadOnlyMapView<K, V>(
    private val delegate: Map<K, V>
) : AbstractMap<K, V>() {
    override val entries: Set<Map.Entry<K, V>>
        get() = delegate.entries
}
