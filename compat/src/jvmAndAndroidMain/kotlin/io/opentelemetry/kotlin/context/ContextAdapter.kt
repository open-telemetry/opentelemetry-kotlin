package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.aliases.OtelJavaBaggage
import io.opentelemetry.kotlin.aliases.OtelJavaContext
import io.opentelemetry.kotlin.baggage.Baggage
import io.opentelemetry.kotlin.baggage.BaggageAdapter

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

    override fun storeBaggage(baggage: Baggage): Context {
        return ContextAdapter((baggage as BaggageAdapter).impl.storeInContext(impl), repository)
    }

    override fun extractBaggage(): Baggage = BaggageAdapter(OtelJavaBaggage.fromContext(impl))

    override fun clearBaggage(): Context =
        ContextAdapter(OtelJavaBaggage.empty().storeInContext(impl), repository)
}
