package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * [Scope] returned when attaching a [Context] did not change the current context.
 */
@OptIn(ExperimentalApi::class)
internal object UnattachedScope : Scope {
    override fun detach(): Boolean = true
}
