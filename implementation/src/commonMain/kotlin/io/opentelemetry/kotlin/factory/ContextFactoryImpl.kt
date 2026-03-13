package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.context.ContextImpl
import io.opentelemetry.kotlin.context.ContextKey
import io.opentelemetry.kotlin.context.ContextKeyImpl
import io.opentelemetry.kotlin.context.DefaultImplicitContextStorage
import io.opentelemetry.kotlin.context.ImplicitContextStorage
import io.opentelemetry.kotlin.tracing.model.Span

internal class ContextFactoryImpl : ContextFactory {

    private val storage: ImplicitContextStorage = DefaultImplicitContextStorage { root }
    private val root by lazy { ContextImpl(storage) }
    internal val spanKey by lazy { createKey<Span>("otel-kotlin-span") }

    override fun root(): Context = root

    override fun storeSpan(context: Context, span: Span): Context {
        return context.set(spanKey, span)
    }

    override fun implicit(): Context = storage.implicitContext()

    override fun <T> createKey(name: String): ContextKey<T> = ContextKeyImpl(name)
}
