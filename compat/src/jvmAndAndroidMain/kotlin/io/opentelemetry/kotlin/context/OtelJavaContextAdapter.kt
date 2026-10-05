package io.opentelemetry.kotlin.context

import io.opentelemetry.api.baggage.otelJavaBaggageContextKey
import io.opentelemetry.api.trace.otelJavaSpanContextKey
import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaBaggage
import io.opentelemetry.kotlin.aliases.OtelJavaContext
import io.opentelemetry.kotlin.aliases.OtelJavaContextKey
import io.opentelemetry.kotlin.aliases.OtelJavaScope
import io.opentelemetry.kotlin.aliases.OtelJavaSpan
import io.opentelemetry.kotlin.baggage.toOtelJavaBaggage
import io.opentelemetry.kotlin.baggage.toOtelKotlinBaggage
import io.opentelemetry.kotlin.factory.DefaultSpanContextFactory
import io.opentelemetry.kotlin.tracing.NonRecordingSpan
import io.opentelemetry.kotlin.tracing.Span
import io.opentelemetry.kotlin.tracing.ext.toOtelJavaSpan
import io.opentelemetry.kotlin.tracing.ext.toOtelKotlinSpanContext
import io.opentelemetry.kotlin.tracing.model.OtelJavaSpanAdapter

/**
 * Java keys are wrapped in a fresh [ContextKeyAdapter] on each access. Adapters compare equality
 * by the Java key they wrap, so values are retrievable without a global key mapping.
 */
@ExperimentalApi
internal class OtelJavaContextAdapter(
    internal val impl: Context,
) : OtelJavaContext {

    @Suppress("UNCHECKED_CAST")
    override fun <V : Any?> get(key: OtelJavaContextKey<V>): V? {
        return when {
            key === otelJavaSpanContextKey -> getSpan() as V?
            key === otelJavaBaggageContextKey -> getBaggage() as V?
            else -> impl.get(ContextKeyAdapter(key))
        }
    }

    override fun <V : Any> with(key: OtelJavaContextKey<V>, value: V?): OtelJavaContext {
        val ctx = when {
            key === otelJavaSpanContextKey -> impl.storeSpan((value as? OtelJavaSpan).toOtelKotlinSpan())
            key === otelJavaBaggageContextKey -> when (value) {
                is OtelJavaBaggage -> impl.storeBaggage(value.toOtelKotlinBaggage())
                else -> impl.clearBaggage()
            }
            else -> impl.set(ContextKeyAdapter(key), value)
        }
        return OtelJavaContextAdapter(ctx)
    }

    override fun makeCurrent(): OtelJavaScope {
        val scope = impl.attach()
        return OtelJavaScope { scope.detach() }
    }

    private fun getSpan(): OtelJavaSpan? {
        val span = impl.extractSpan()
        if (!span.spanContext.isValid) {
            return null
        }
        return span.toOtelJavaSpan()
    }

    private fun getBaggage(): OtelJavaBaggage? {
        val baggage = impl.extractBaggage()
        return when {
            baggage.asMap().isEmpty() -> null
            else -> baggage.toOtelJavaBaggage()
        }
    }

    private fun OtelJavaSpan?.toOtelKotlinSpan(): Span = when (this) {
        is OtelJavaSpanAdapter -> span
        null -> NonRecordingSpan(DefaultSpanContextFactory.invalid, DefaultSpanContextFactory.invalid)
        else -> NonRecordingSpan(DefaultSpanContextFactory.invalid, spanContext.toOtelKotlinSpanContext())
    }
}
