package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.baggage.Baggage
import io.opentelemetry.kotlin.baggage.FakeBaggage
import io.opentelemetry.kotlin.tracing.FakeSpan
import io.opentelemetry.kotlin.tracing.Span

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

    override fun storeSpan(span: Span): Context = FakeContext(attrs, onAttach, onDetach)

    override fun extractSpan(): Span = FakeSpan()

    override fun storeBaggage(baggage: Baggage): Context = FakeContext(attrs, onAttach, onDetach)

    override fun extractBaggage(): Baggage = FakeBaggage()

    override fun clearBaggage(): Context = FakeContext(attrs, onAttach, onDetach)
}
