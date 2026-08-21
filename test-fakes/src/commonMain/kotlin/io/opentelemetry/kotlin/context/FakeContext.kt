package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.baggage.Baggage
import io.opentelemetry.kotlin.baggage.FakeBaggage
import io.opentelemetry.kotlin.tracing.FakeSpan
import io.opentelemetry.kotlin.tracing.Span

class FakeContext(
    val attrs: Map<ContextKey<*>, Any?> = emptyMap(),
    private val onAttach: () -> Unit = {},
    private val onDetach: () -> Boolean = { true },
    private val span: Span = FakeSpan(),
) : Context {

    override fun <T> set(key: ContextKey<T>, value: T?): Context {
        return FakeContext(attrs + (key to value), onAttach, onDetach, span)
    }

    override fun <T> get(key: ContextKey<T>): T? {
        return null
    }

    override fun attach(): Scope {
        onAttach()
        return FakeScope(onDetach)
    }

    override fun storeSpan(span: Span): Context = FakeContext(attrs, onAttach, onDetach, span)

    override fun extractSpan(): Span = span

    override fun storeBaggage(baggage: Baggage): Context =
        FakeContext(attrs, onAttach, onDetach, span)

    override fun extractBaggage(): Baggage = FakeBaggage()

    override fun clearBaggage(): Context = FakeContext(attrs, onAttach, onDetach, span)
}
