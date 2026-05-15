package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.platformLog
import kotlin.concurrent.Volatile

internal class ScopeImpl private constructor(
    private val previousContext: Context,
    private val currentContext: Context,
    private val storage: ImplicitContextStorage,
) : Scope {

    @Volatile
    private var detached = false

    override fun detach(): Boolean {
        return if (detached) {
            platformLog("Scope.detach() called on an already-detached scope")
            false
        } else if (storage.implicitContext() != currentContext) {
            platformLog("Scope.detach() called out of order — context has already changed")
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
        ): Scope =
            if (previousContext == currentContext) {
                platformLog("Cannot create scope with two matching contexts")
                DetachedScope
            } else {
                ScopeImpl(
                    previousContext = previousContext,
                    currentContext = currentContext,
                    storage = storage
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
