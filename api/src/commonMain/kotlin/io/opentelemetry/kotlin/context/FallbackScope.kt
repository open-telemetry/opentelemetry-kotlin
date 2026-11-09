package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * The [Scope] returned when the underlying storage failed to attach.
 */
@OptIn(ExperimentalApi::class)
internal object FallbackScope : Scope {
    override fun detach(): Boolean = true
}
