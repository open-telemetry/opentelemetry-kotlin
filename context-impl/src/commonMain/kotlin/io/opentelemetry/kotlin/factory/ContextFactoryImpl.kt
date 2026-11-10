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

    // SHIM (drop when rebasing onto 3c): storages return the :api root when empty, but until 3c
    // the SDK's root is a ContextImpl bound to this storage. Map one to the other so that
    // current() and the ContextImpl/ScopeImpl identity checks keep seeing the SDK root.
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
