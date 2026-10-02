package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.baggage.Baggage
import io.opentelemetry.kotlin.baggage.createBaggage
import io.opentelemetry.kotlin.tracing.Span

/**
 * Minimal immutable [Context] that only supports storing values and baggage.
 */
@OptIn(ExperimentalApi::class)
internal class FakeBaggageContext(
    private val values: Map<ContextKey<*>, Any?> = emptyMap(),
    private val baggage: Baggage = createBaggage(),
) : Context {

    override fun <T> set(key: ContextKey<T>, value: T?): Context =
        FakeBaggageContext(values + (key to value), baggage)

    @Suppress("UNCHECKED_CAST")
    override fun <T> get(key: ContextKey<T>): T? = values[key] as T?

    override fun attach(): Scope = error("Not supported")

    override fun storeSpan(span: Span): Context = error("Not supported")

    override fun extractSpan(): Span = error("Not supported")

    override fun storeBaggage(baggage: Baggage): Context = FakeBaggageContext(values, baggage)

    override fun extractBaggage(): Baggage = baggage

    override fun clearBaggage(): Context = FakeBaggageContext(values)
}
