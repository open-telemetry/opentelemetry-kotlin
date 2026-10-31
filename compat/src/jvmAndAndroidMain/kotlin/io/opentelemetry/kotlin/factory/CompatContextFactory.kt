package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.aliases.OtelJavaContext
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.context.toOtelKotlinContext

internal class CompatContextFactory : ContextFactory {

    override fun root(): Context = OtelJavaContext.root().toOtelKotlinContext()

    override fun current(): Context = OtelJavaContext.current().toOtelKotlinContext()
}
