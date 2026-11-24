package io.opentelemetry.kotlin.behavior

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * A configuration mode agnostic implementation that supplies the [Behavior] it captured.
 */
@OptIn(ExperimentalApi::class)
fun interface BehaviorSupplier<T : Behavior<T>> {

    /**
     * Returns the configured behavior.
     */
    fun toBehavior(): T
}
