package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.context.ContextImpl
import io.opentelemetry.kotlin.context.ContextKey
import io.opentelemetry.kotlin.context.ContextKeyImpl
import io.opentelemetry.kotlin.context.DefaultImplicitContextStorage
import io.opentelemetry.kotlin.context.ImplicitContextStorage
import io.opentelemetry.kotlin.error.NoopSdkErrorHandler
import io.opentelemetry.kotlin.error.SdkErrorHandler

internal class ContextFactoryImpl(
    private val spanFactory: SpanFactory,
    private val sdkErrorHandler: SdkErrorHandler = NoopSdkErrorHandler,
    storageFactory: (supplier: () -> Context) -> ImplicitContextStorage = ::DefaultImplicitContextStorage,
) : ContextFactory {

    private val storage: ImplicitContextStorage = storageFactory { root }
    private val root by lazy { ContextImpl(storage, spanFactory, sdkErrorHandler) }

    override fun root(): Context = root

    override fun implicit(): Context = storage.implicitContext()

    override fun <T> createKey(name: String): ContextKey<T> = ContextKeyImpl(name)
}
