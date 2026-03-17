package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.aliases.OtelJavaContext
import io.opentelemetry.kotlin.aliases.OtelJavaContextKey
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.context.ContextKey
import io.opentelemetry.kotlin.context.ContextKeyAdapter
import io.opentelemetry.kotlin.context.toOtelKotlinContext
import io.opentelemetry.kotlin.tracing.ext.storeInContext
import io.opentelemetry.kotlin.tracing.model.Span

internal class CompatContextFactory : ContextFactory {

    override fun root(): Context = OtelJavaContext.root().toOtelKotlinContext()

    override fun storeSpan(
        context: Context,
        span: Span
    ): Context = span.storeInContext(context)

    override fun implicit(): Context = OtelJavaContext.current().toOtelKotlinContext()

    override fun <T> createKey(name: String): ContextKey<T> = ContextKeyAdapter(OtelJavaContextKey.named(name))
}
