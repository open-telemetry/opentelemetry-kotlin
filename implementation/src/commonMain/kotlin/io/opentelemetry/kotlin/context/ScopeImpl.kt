package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.platformLog
import kotlin.concurrent.Volatile

internal class ScopeImpl(
    private val previousContext: Context,
    private val currentContext: Context,
    private val storage: ImplicitContextStorage,
) : Scope {

    init {
        if (previousContext == currentContext) {
            error("Cannot create scope with two matching contexts")
        }
    }

    @Volatile
    private var detached = false

    override fun detach(): Boolean {
        if (detached) {
            platformLog("OpenTelemetry: Scope.detach() called on an already-detached scope")
            return false
        }
        if (storage.implicitContext() != currentContext) {
            platformLog("OpenTelemetry: Scope.detach() called out of order — context has already changed")
            return false
        }
        detached = true
        storage.setImplicitContext(previousContext)
        return true
    }
}
