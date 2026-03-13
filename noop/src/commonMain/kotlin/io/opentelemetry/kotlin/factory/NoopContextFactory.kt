package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.context.ContextKey
import io.opentelemetry.kotlin.context.NoopContext
import io.opentelemetry.kotlin.context.NoopContextKey
import io.opentelemetry.kotlin.tracing.model.Span

internal object NoopContextFactory : ContextFactory {

    override fun root(): Context = NoopContext

    override fun storeSpan(
        context: Context,
        span: Span
    ): Context = NoopContext

    override fun implicit(): Context = NoopContext

    override fun <T> createKey(name: String): ContextKey<T> = NoopContextKey(name)
}
