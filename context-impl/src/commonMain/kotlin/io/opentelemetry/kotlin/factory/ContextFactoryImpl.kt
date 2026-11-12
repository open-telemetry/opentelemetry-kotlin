package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.context.CurrentContextStorage
import io.opentelemetry.kotlin.error.NoopSdkErrorHandler
import io.opentelemetry.kotlin.error.SdkErrorHandler

/**
 * Returns contexts from the process-wide context storage.
 *
 * [installedStorage] is the storage that the owning SDK installed into the process-wide slot, or
 * null if it didn't install one.
 */
public class ContextFactoryImpl(
    public val sdkErrorHandler: SdkErrorHandler = NoopSdkErrorHandler,
    public val installedStorage: CurrentContextStorage? = null,
) : ContextFactory {

    // fully qualified so these don't resolve to the ContextFactory extensions on `this`
    override fun root(): Context = io.opentelemetry.kotlin.context.rootContext()

    override fun current(): Context = io.opentelemetry.kotlin.context.currentContext()
}
