package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.baggage.Baggage
import io.opentelemetry.kotlin.baggage.createBaggage
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.tracing.NonRecordingSpan
import io.opentelemetry.kotlin.tracing.Span
import io.opentelemetry.kotlin.tracing.SpanContext
import io.opentelemetry.kotlin.tracing.createInvalidSpanContext

private val BAGGAGE_KEY: ContextKey<Baggage> = ContextKeyImpl("otel-kotlin-baggage")
private val SPAN_KEY: ContextKey<Span> = ContextKeyImpl("otel-kotlin-span")
private val EMPTY_BAGGAGE: Baggage = createBaggage()
private val INVALID_SPAN: Span = NonRecordingSpan(createInvalidSpanContext(), createInvalidSpanContext())

internal class ContextImpl(
    private val storage: ImplicitContextStorage,
    private val sdkErrorHandler: SdkErrorHandler,
    private val impl: Map<ContextKey<*>, Any?> = emptyMap()
) : Context {

    override fun <T> set(
        key: ContextKey<T>,
        value: T?
    ): Context {
        val newValues = impl.plus(Pair(key, value))
        return ContextImpl(storage, sdkErrorHandler, newValues)
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
        if (storage is ScopedImplicitContextStorage) {
            val scope = storage.attach(this)
            return ScopeImpl.create(current, this, storage, sdkErrorHandler) { scope.detach() }
        }
        storage.setImplicitContext(this)
        return ScopeImpl.create(current, this, storage, sdkErrorHandler)
    }

    override fun storeSpan(span: Span): Context = set(SPAN_KEY, span)

    override fun storeSpanContext(spanContext: SpanContext): Context =
        storeSpan(NonRecordingSpan(createInvalidSpanContext(), spanContext))

    override fun extractSpan(): Span = get(SPAN_KEY) ?: INVALID_SPAN

    override fun storeBaggage(baggage: Baggage): Context = set(BAGGAGE_KEY, baggage)

    override fun extractBaggage(): Baggage = get(BAGGAGE_KEY) ?: EMPTY_BAGGAGE

    override fun clearBaggage(): Context = set(BAGGAGE_KEY, null)
}
