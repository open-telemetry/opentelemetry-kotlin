package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.ExperimentalApi

@OptIn(ExperimentalApi::class)
class FakeContext(
    val attrs: Map<ContextKey<*>, Any?> = emptyMap(),
    private val onAttach: () -> Unit = {},
    private val onDetach: () -> Unit = {},
) : Context {

    override fun <T> createKey(name: String): ContextKey<T> = FakeContextKey(name)

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
