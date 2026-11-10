package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.context.TempContextStandInApi
import io.opentelemetry.kotlin.context.internalCurrentContext
import io.opentelemetry.kotlin.context.internalRootContext

@OptIn(ExperimentalApi::class, TempContextStandInApi::class)
internal object NoopContextFactory : ContextFactory {

    override fun root(): Context = internalRootContext()

    override fun current(): Context = internalCurrentContext()
}
