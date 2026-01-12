package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaContext
import io.opentelemetry.kotlin.aliases.OtelJavaContextKey

@OptIn(ExperimentalApi::class)
internal class ContextAdapter(
    val impl: OtelJavaContext,
    private val repository: ContextKeyRepository = ContextKeyRepository.INSTANCE
) : Context {

    override fun <T> createKey(name: String): ContextKey<T> {
        return ContextKeyAdapter(OtelJavaContextKey.named(name))
    }

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
