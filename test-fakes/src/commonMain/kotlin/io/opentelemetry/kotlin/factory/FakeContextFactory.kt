package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.context.ContextKey
import io.opentelemetry.kotlin.context.FakeContext
import io.opentelemetry.kotlin.context.FakeContextKey
import io.opentelemetry.kotlin.tracing.model.Span

class FakeContextFactory : ContextFactory {

    override fun root(): Context = FakeContext()

    override fun storeSpan(
        context: Context,
        span: Span
    ): Context = FakeContext()

    override fun implicit(): Context = FakeContext()

    override fun <T> createKey(name: String): ContextKey<T> = FakeContextKey(name)
}
