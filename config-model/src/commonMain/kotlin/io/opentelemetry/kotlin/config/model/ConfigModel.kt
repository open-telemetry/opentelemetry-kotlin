package io.opentelemetry.kotlin.config.model

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * Configuration supplied by one mechanism, which can be combined with configuration from another.
 */
@ExperimentalApi
interface ConfigModel<T : ConfigModel<T>> {

    /**
     * Returns a copy of this model where every value configured in [higher] wins. Values left unset
     * in [higher] keep whatever this model had, so a higher-precedence layer refines rather than
     * replaces the layer below it.
     */
    fun mergeWith(higher: T): T
}
