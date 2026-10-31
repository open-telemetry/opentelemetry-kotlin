package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.context.NoopContext

internal object NoopContextFactory : ContextFactory {

    override fun root(): Context = NoopContext

    override fun current(): Context = NoopContext
}
