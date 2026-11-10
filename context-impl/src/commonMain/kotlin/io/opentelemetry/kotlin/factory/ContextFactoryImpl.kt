package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.context.ContextImpl
import io.opentelemetry.kotlin.context.CurrentContextStorage
import io.opentelemetry.kotlin.context.Scope
import io.opentelemetry.kotlin.context.TempContextStandInApi
import io.opentelemetry.kotlin.context.createSingleSlotCurrentContextStorage
import io.opentelemetry.kotlin.context.internalRootContext
import io.opentelemetry.kotlin.error.NoopSdkErrorHandler
import io.opentelemetry.kotlin.error.SdkErrorHandler

public class ContextFactoryImpl(
    public val sdkErrorHandler: SdkErrorHandler = NoopSdkErrorHandler,
    public val storage: CurrentContextStorage = createSingleSlotCurrentContextStorage(),
) : ContextFactory {

    // Temp SHIM the built-in storages return the :api root when empty, but until
    // the SDK's root is a ContextImpl bound to this storage, they need mapping to each other so
    // current() and the ContextImpl/ScopeImpl identity checks see the SDK root.
    private val rootMappingStorage: CurrentContextStorage = object : CurrentContextStorage {
        @OptIn(TempContextStandInApi::class)
        override fun current(): Context {
            val current = storage.current()
            return when {
                current === internalRootContext() -> root
                else -> current
            }
        }

        override fun attach(context: Context): Scope = storage.attach(context)
    }
    private val root: Context by lazy { ContextImpl(rootMappingStorage, sdkErrorHandler) }

    override fun root(): Context = root

    override fun current(): Context = rootMappingStorage.current()
}
