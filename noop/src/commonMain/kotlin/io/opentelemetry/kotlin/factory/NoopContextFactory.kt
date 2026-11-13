package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.context.currentContext
import io.opentelemetry.kotlin.context.rootContext

@OptIn(ExperimentalApi::class)
internal object NoopContextFactory : ContextFactory {

    override fun root(): Context = rootContext()

    override fun current(): Context = currentContext()
}
