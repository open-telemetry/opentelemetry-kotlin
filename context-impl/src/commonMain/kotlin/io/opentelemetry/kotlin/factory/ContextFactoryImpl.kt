package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.context.ContextImpl
import io.opentelemetry.kotlin.context.CurrentContextStorage
import io.opentelemetry.kotlin.context.SingleSlotCurrentContextStorage
import io.opentelemetry.kotlin.error.NoopSdkErrorHandler
import io.opentelemetry.kotlin.error.SdkErrorHandler

public class ContextFactoryImpl(
    public val sdkErrorHandler: SdkErrorHandler = NoopSdkErrorHandler,
    storageFactory: (supplier: () -> Context) -> CurrentContextStorage = ::SingleSlotCurrentContextStorage,
) : ContextFactory {

    public val storage: CurrentContextStorage = storageFactory { root }
    private val root by lazy { ContextImpl(storage, sdkErrorHandler) }

    override fun root(): Context = root

    override fun current(): Context = storage.current()
}
