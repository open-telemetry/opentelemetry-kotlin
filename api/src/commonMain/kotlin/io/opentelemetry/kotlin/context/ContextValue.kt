package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.baggage.Baggage
import io.opentelemetry.kotlin.baggage.createBaggage
import io.opentelemetry.kotlin.tracing.Span
import io.opentelemetry.kotlin.tracing.SpanContext
import io.opentelemetry.kotlin.tracing.createInvalidSpanContext
import io.opentelemetry.kotlin.tracing.createNonRecordingSpan

@OptIn(ExperimentalApi::class)
private val BAGGAGE_KEY: ContextKey<Baggage> = createContextKey("otel-kotlin-baggage")

@OptIn(ExperimentalApi::class)
private val SPAN_KEY: ContextKey<Span> = createContextKey("otel-kotlin-span")

@OptIn(ExperimentalApi::class)
private val EMPTY_BAGGAGE: Baggage = createBaggage()

@OptIn(ExperimentalApi::class)
private val INVALID_SPAN: Span = createNonRecordingSpan(createInvalidSpanContext())

/**
 * Immutable [Context] backed by a map. Equality is by reference.
 *
 * [attach] dispatches to the storage that [storageSource] supplies. Derived contexts share the
 * same [storageSource] instance.
 */
@OptIn(ExperimentalApi::class)
internal class ContextValue(
    private val storageSource: CurrentContextStorageSource,
    private val values: Map<ContextKey<*>, Any?> = emptyMap(),
) : Context {

    override fun <T> set(key: ContextKey<T>, value: T?): Context =
        ContextValue(storageSource, values + (key to value))

    @Suppress("UNCHECKED_CAST")
    override fun <T> get(key: ContextKey<T>): T? = values[key] as? T

    override fun attach(): Scope = storageSource.installed()?.attach(this) ?: UnattachedScope

    override fun storeSpan(span: Span): Context = set(SPAN_KEY, span)

    override fun storeSpanContext(spanContext: SpanContext): Context =
        storeSpan(createNonRecordingSpan(spanContext))

    override fun extractSpan(): Span = get(SPAN_KEY) ?: INVALID_SPAN

    override fun storeBaggage(baggage: Baggage): Context = set(BAGGAGE_KEY, baggage)

    override fun extractBaggage(): Baggage = get(BAGGAGE_KEY) ?: EMPTY_BAGGAGE

    override fun clearBaggage(): Context = set(BAGGAGE_KEY, null)
}
