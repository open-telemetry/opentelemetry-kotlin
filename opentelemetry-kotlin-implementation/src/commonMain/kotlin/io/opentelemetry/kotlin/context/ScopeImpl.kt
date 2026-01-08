package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.ExperimentalApi
import kotlin.concurrent.Volatile

@OptIn(ExperimentalApi::class)
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

    override fun detach() {
        if (!detached) {
            if (storage.implicitContext() == currentContext) {
                detached = true
                storage.setImplicitContext(previousContext)
            }
        }
    }
}
