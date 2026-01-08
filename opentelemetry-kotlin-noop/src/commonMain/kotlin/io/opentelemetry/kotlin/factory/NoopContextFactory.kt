package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.context.NoopContext
import io.opentelemetry.kotlin.tracing.model.Span

@OptIn(ExperimentalApi::class)
internal object NoopContextFactory : ContextFactory {

    override fun root(): Context = NoopContext

    override fun storeSpan(
        context: Context,
        span: Span
    ): Context = NoopContext

    override fun implicitContext(): Context = NoopContext
}
