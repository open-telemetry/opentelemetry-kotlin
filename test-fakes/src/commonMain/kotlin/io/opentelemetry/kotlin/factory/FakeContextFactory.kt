package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.context.FakeContext
import io.opentelemetry.kotlin.tracing.model.Span

@OptIn(ExperimentalApi::class)
internal class FakeContextFactory : ContextFactory {

    override fun root(): Context = FakeContext()

    override fun storeSpan(
        context: Context,
        span: Span
    ): Context = FakeContext()

    override fun implicitContext(): Context = FakeContext()
}
