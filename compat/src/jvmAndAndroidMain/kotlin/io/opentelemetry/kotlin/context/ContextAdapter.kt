@file:Suppress("DiscouragedImport")

package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.aliases.OtelJavaBaggage
import io.opentelemetry.kotlin.aliases.OtelJavaContext
import io.opentelemetry.kotlin.aliases.OtelJavaSpan
import io.opentelemetry.kotlin.baggage.Baggage
import io.opentelemetry.kotlin.baggage.toOtelJavaBaggage
import io.opentelemetry.kotlin.baggage.toOtelKotlinBaggage
import io.opentelemetry.kotlin.factory.DefaultSpanContextFactory
import io.opentelemetry.kotlin.tracing.NonRecordingSpan
import io.opentelemetry.kotlin.tracing.Span
import io.opentelemetry.kotlin.tracing.ext.storeInContext
import io.opentelemetry.kotlin.tracing.ext.toOtelKotlinSpanContext

internal class ContextAdapter(
    val impl: OtelJavaContext,
    private val repository: ContextKeyRepository = ContextKeyRepository.INSTANCE
) : Context {

    override fun <T> set(key: ContextKey<T>, value: T?): Context {
        val ctx = impl.with(repository.get(key), value.asJavaValue())
        return ContextAdapter(ctx, repository)
    }

    /**
     * opentelemetry-java stores null values (clearing the key), but K2 reads its unannotated
     * parameter as non-null. Casting to a non-reified type parameter is erased and allows
     * passing null.
     */
    @Suppress("UNCHECKED_CAST")
    private fun <R> Any?.asJavaValue(): R = this as R

    override fun <T> get(key: ContextKey<T>): T? {
        return impl[repository.get(key)]
    }

    override fun attach(): Scope {
        return ScopeAdapter(impl.makeCurrent())
    }

    override fun storeSpan(span: Span): Context = span.storeInContext(this)

    override fun extractSpan(): Span {
        val javaSpan = OtelJavaSpan.fromContext(impl)
        return NonRecordingSpan(
            DefaultSpanContextFactory.invalid,
            javaSpan.spanContext.toOtelKotlinSpanContext(),
        )
    }

    override fun storeBaggage(baggage: Baggage): Context {
        return ContextAdapter(baggage.toOtelJavaBaggage().storeInContext(impl), repository)
    }

    override fun extractBaggage(): Baggage = OtelJavaBaggage.fromContext(impl).toOtelKotlinBaggage()

    override fun clearBaggage(): Context =
        ContextAdapter(OtelJavaBaggage.empty().storeInContext(impl), repository)
}
