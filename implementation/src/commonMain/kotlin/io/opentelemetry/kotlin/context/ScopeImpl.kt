package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.error.SdkError
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.error.SdkErrorSeverity
import kotlin.concurrent.Volatile

internal class ScopeImpl private constructor(
    private val previousContext: Context,
    private val currentContext: Context,
    private val storage: ImplicitContextStorage,
    private val sdkErrorHandler: SdkErrorHandler,
) : Scope {

    @Volatile
    private var detached = false

    override fun detach(): Boolean {
        return if (detached) {
            sdkErrorHandler.onError(
                SdkError.ApiMisuse(
                    api = "Scope.detach",
                    message = "Scope.detach() called on an already-detached scope",
                    severity = SdkErrorSeverity.WARNING,
                )
            )
            false
        } else if (storage.implicitContext() != currentContext) {
            sdkErrorHandler.onError(
                SdkError.ApiMisuse(
                    api = "Scope.detach",
                    message = "Scope.detach() called out of order — context has already changed",
                    severity = SdkErrorSeverity.WARNING,
                )
            )
            false
        } else {
            detached = true
            storage.setImplicitContext(previousContext)
            true
        }
    }

    companion object {
        fun create(
            previousContext: Context,
            currentContext: Context,
            storage: ImplicitContextStorage,
            sdkErrorHandler: SdkErrorHandler,
        ): Scope =
            if (previousContext == currentContext) {
                sdkErrorHandler.onError(
                    SdkError.ApiMisuse(
                        api = "Context.attach",
                        message = "Cannot create scope with two matching contexts",
                        severity = SdkErrorSeverity.WARNING,
                    )
                )
                DetachedScope
            } else {
                ScopeImpl(
                    previousContext = previousContext,
                    currentContext = currentContext,
                    storage = storage,
                    sdkErrorHandler = sdkErrorHandler,
                )
            }
    }
}

/**
 * A [Scope] that is always detached
 */
internal object DetachedScope : Scope {
    override fun detach(): Boolean = true
}
