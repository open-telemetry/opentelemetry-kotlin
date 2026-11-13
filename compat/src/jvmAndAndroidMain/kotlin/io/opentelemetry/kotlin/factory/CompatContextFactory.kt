package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.context.Context

/**
 * Returns contexts from the process-wide context storage.
 */
internal class CompatContextFactory : ContextFactory {

    // fully qualified so these don't resolve to the ContextFactory extensions on `this`
    override fun root(): Context = io.opentelemetry.kotlin.context.rootContext()

    override fun current(): Context = io.opentelemetry.kotlin.context.currentContext()
}
