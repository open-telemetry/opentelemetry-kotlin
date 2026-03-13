package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.aliases.OtelJavaContext

internal class ContextAdapter(
    val impl: OtelJavaContext,
    private val repository: ContextKeyRepository = ContextKeyRepository.INSTANCE
) : Context {

    @Suppress("UNCHECKED_CAST")
    override fun <T> set(key: ContextKey<T>, value: T?): Context {
        val ctx = impl.with(repository.get(key), value as (T & Any))
        return ContextAdapter(ctx, repository)
    }

    override fun <T> get(key: ContextKey<T>): T? {
        return impl[repository.get(key)]
    }

    override fun attach(): Scope {
        return ScopeAdapter(impl.makeCurrent())
    }
}
