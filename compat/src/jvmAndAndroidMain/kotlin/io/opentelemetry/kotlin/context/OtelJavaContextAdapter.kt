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
import io.opentelemetry.kotlin.tracing.Span
import io.opentelemetry.kotlin.tracing.createInvalidSpanContext
import io.opentelemetry.kotlin.tracing.createNonRecordingSpan
import io.opentelemetry.kotlin.tracing.ext.asOtelJavaSpan
import io.opentelemetry.kotlin.tracing.ext.toOtelKotlinSpanContext
import io.opentelemetry.kotlin.tracing.model.OtelJavaSpanAdapter
import java.util.WeakHashMap

/**
 * A Java view of a Kotlin [Context], so that `makeCurrent()` from Java reaches the installed Kotlin storage.
 *
 * Java's span and baggage keys map onto the Kotlin span and baggage. Any other Java key maps to a
 * Kotlin key that is created on first use and held for as long as the Java key is reachable.
 */
@OptIn(ExperimentalApi::class)
internal class OtelJavaContextAdapter(
    internal val impl: Context,
) : OtelJavaContext {

    @Suppress("UNCHECKED_CAST")
    override fun <V : Any?> get(key: OtelJavaContextKey<V>): V? {
        return when {
            key === otelJavaSpanContextKey -> getSpan() as V?
            key === otelJavaBaggageContextKey -> getBaggage() as V?
            else -> impl.get(key.toKotlinKey())
        }
    }

    override fun <V : Any> with(key: OtelJavaContextKey<V>, value: V?): OtelJavaContext {
        val ctx = when {
            key === otelJavaSpanContextKey -> impl.storeSpan((value as? OtelJavaSpan).toOtelKotlinSpan())
            key === otelJavaBaggageContextKey -> when (value) {
                is OtelJavaBaggage -> impl.storeBaggage(value.toOtelKotlinBaggage())
                else -> impl.clearBaggage()
            }
            else -> impl.set(key.toKotlinKey(), value)
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
        return span.asOtelJavaSpan()
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
        null -> createNonRecordingSpan(createInvalidSpanContext())
        else -> createNonRecordingSpan(spanContext.toOtelKotlinSpanContext())
    }

    override fun equals(other: Any?): Boolean = other is OtelJavaContextAdapter && other.impl == impl

    override fun hashCode(): Int = impl.hashCode()

    private companion object {
        // java context keys compare by identity, and the Kotlin keys don't reference them
        private val keys = WeakHashMap<OtelJavaContextKey<*>, ContextKey<*>>()

        @Suppress("UNCHECKED_CAST")
        fun <V> OtelJavaContextKey<V>.toKotlinKey(): ContextKey<V> = synchronized(keys) {
            keys.getOrPut(this) { createContextKey<V>(toString()) }
        } as ContextKey<V>
    }
}
