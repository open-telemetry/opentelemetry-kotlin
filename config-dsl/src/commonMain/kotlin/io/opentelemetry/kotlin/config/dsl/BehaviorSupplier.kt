package io.opentelemetry.kotlin.config.dsl

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.behavior.Behavior

/**
 * A configuration DSL implementation that supplies the [Behavior] it captured.
 */
@ExperimentalApi
fun interface BehaviorSupplier<T : Behavior<T>> {

    /**
     * Returns the behavior configured via the DSL.
     */
    fun toBehavior(): T
}
