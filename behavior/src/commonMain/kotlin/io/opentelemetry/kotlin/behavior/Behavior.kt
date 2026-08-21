package io.opentelemetry.kotlin.behavior

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * Behavior supplied by one configuration mechanism, which can be combined with the behavior
 * supplied by another.
 */
@ExperimentalApi
interface Behavior<T : Behavior<T>> {

    /**
     * Returns a copy of this behavior where every value configured in [higher] wins. Values left
     * unset in [higher] keep whatever this behavior had, so a higher-precedence layer refines rather
     * than replaces the layer below it.
     */
    fun mergeWith(higher: T): T
}
