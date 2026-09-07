package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.AtomicBoolean
import io.opentelemetry.kotlin.error.SdkError
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.error.SdkErrorSeverity
import io.opentelemetry.kotlin.error.reportError

private const val ALREADY_DETACHED_MSG = "Scope.detach() called on an already-detached scope"
private const val OUT_OF_ORDER_MSG = "Scope.detach() called out of order — context has already changed"

internal class ScopeImpl private constructor(
    private val previousContext: Context,
    private val currentContext: Context,
    private val storage: ImplicitContextStorage,
    private val sdkErrorHandler: SdkErrorHandler,
) : Scope {

    private val detached = AtomicBoolean(false)

    override fun detach(): Boolean {
        if (storage.implicitContext() != currentContext) {
            val message = when {
                detached.get() -> ALREADY_DETACHED_MSG
                else -> OUT_OF_ORDER_MSG
            }
            reportMisuse(message)
            return false
        }
        if (!detached.compareAndSet(expect = false, update = true)) {
            reportMisuse(ALREADY_DETACHED_MSG)
            return false
        }
        storage.setImplicitContext(previousContext)
        return true
    }

    private fun reportMisuse(message: String) {
        sdkErrorHandler.reportError(
            SdkError.ApiMisuse(
                api = "Scope.detach",
                message = message,
                severity = SdkErrorSeverity.WARNING,
            )
        )
    }

    companion object {
        fun create(
            previousContext: Context,
            currentContext: Context,
            storage: ImplicitContextStorage,
            sdkErrorHandler: SdkErrorHandler,
        ): Scope =
            if (previousContext == currentContext) {
                sdkErrorHandler.reportError(
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
