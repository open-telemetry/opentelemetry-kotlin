package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.ExperimentalApi

@OptIn(ExperimentalApi::class)
internal class ContextImpl(
    private val storage: ImplicitContextStorage,
    private val impl: Map<ContextKey<*>, Any?> = emptyMap()
) : Context {

    override fun <T> createKey(name: String): ContextKey<T> = ContextKeyImpl(name)

    override fun <T> set(
        key: ContextKey<T>,
        value: T?
    ): Context {
        val newValues = impl.plus(Pair(key, value))
        return ContextImpl(storage, newValues)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> get(key: ContextKey<T>): T? {
        return impl[key] as? T
    }

    override fun attach(): Scope {
        if (storage.implicitContext() == this) {
            return NoopScope
        }
        val current = storage.implicitContext()
        storage.setImplicitContext(this)
        return ScopeImpl(current, this, storage)
    }

    private object NoopScope : Scope {
        override fun detach() {
        }
    }
}
