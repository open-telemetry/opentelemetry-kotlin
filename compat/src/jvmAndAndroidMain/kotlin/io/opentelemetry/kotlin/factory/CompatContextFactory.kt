package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.context.TempContextStandInApi
import io.opentelemetry.kotlin.context.internalCurrentContext
import io.opentelemetry.kotlin.context.internalRootContext

/**
 * Returns contexts from the process-wide context storage.
 */
@OptIn(TempContextStandInApi::class)
internal class CompatContextFactory : ContextFactory {

    override fun root(): Context = internalRootContext()

    override fun current(): Context = internalCurrentContext()
}
