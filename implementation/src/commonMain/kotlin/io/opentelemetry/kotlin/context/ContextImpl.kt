package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.baggage.Baggage
import io.opentelemetry.kotlin.baggage.BaggageImpl
import io.opentelemetry.kotlin.factory.SpanFactory
import io.opentelemetry.kotlin.tracing.Span

private val BAGGAGE_KEY: ContextKey<Baggage> = ContextKeyImpl("otel-kotlin-baggage")
private val SPAN_KEY: ContextKey<Span> = ContextKeyImpl("otel-kotlin-span")

internal class ContextImpl(
    private val storage: ImplicitContextStorage,
    private val spanFactory: SpanFactory,
    private val impl: Map<ContextKey<*>, Any?> = emptyMap()
) : Context {

    override fun <T> set(
        key: ContextKey<T>,
        value: T?
    ): Context {
        val newValues = impl.plus(Pair(key, value))
        return ContextImpl(storage, spanFactory, newValues)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> get(key: ContextKey<T>): T? {
        return impl[key] as? T
    }

    override fun attach(): Scope {
        if (storage.implicitContext() == this) {
            return DetachedScope
        }
        val current = storage.implicitContext()
        storage.setImplicitContext(this)
        return ScopeImpl.create(current, this, storage)
    }

    override fun storeSpan(span: Span): Context = set(SPAN_KEY, span)

    override fun extractSpan(): Span = get(SPAN_KEY) ?: spanFactory.invalid

    override fun storeBaggage(baggage: Baggage): Context = set(BAGGAGE_KEY, baggage)

    override fun extractBaggage(): Baggage = get(BAGGAGE_KEY) ?: BaggageImpl.EMPTY

    override fun clearBaggage(): Context = set(BAGGAGE_KEY, null)
}
