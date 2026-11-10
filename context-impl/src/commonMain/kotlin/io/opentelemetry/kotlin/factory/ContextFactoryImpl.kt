package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.context.CurrentContextStorage
import io.opentelemetry.kotlin.context.TempContextStandInApi
import io.opentelemetry.kotlin.context.internalCurrentContext
import io.opentelemetry.kotlin.context.internalRootContext
import io.opentelemetry.kotlin.error.NoopSdkErrorHandler
import io.opentelemetry.kotlin.error.SdkErrorHandler

/**
 * Returns contexts from the process-wide context storage.
 *
 * [installedStorage] is the storage that the owning SDK installed into the process-wide slot, or
 * null if it didn't install one.
 */
@OptIn(TempContextStandInApi::class)
public class ContextFactoryImpl(
    public val sdkErrorHandler: SdkErrorHandler = NoopSdkErrorHandler,
    public val installedStorage: CurrentContextStorage? = null,
) : ContextFactory {

    override fun root(): Context = internalRootContext()

    override fun current(): Context = internalCurrentContext()
}
