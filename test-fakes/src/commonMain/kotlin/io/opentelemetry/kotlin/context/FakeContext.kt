package io.opentelemetry.kotlin.context
class FakeContext(
    val attrs: Map<ContextKey<*>, Any?> = emptyMap(),
    private val onAttach: () -> Unit = {},
    private val onDetach: () -> Boolean = { true },
) : Context {

    override fun <T> set(key: ContextKey<T>, value: T?): Context {
        return FakeContext(attrs + (key to value), onAttach, onDetach)
    }

    override fun <T> get(key: ContextKey<T>): T? {
        return null
    }

    override fun attach(): Scope {
        onAttach()
        return FakeScope(onDetach)
    }
}
