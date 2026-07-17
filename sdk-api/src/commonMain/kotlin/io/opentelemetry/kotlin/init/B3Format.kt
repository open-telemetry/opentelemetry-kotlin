package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * Injection format for the B3 propagator.
 *
 * https://github.com/openzipkin/b3-propagation
 */
@ExperimentalApi
public enum class B3Format {
    /** Injects a single `b3` header. */
    SINGLE,

    /** Injects separate `X-B3-*` headers. */
    MULTI,
}
