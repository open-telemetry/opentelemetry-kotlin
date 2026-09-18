package io.opentelemetry.kotlin.propagation

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * Constructs a [Propagators] instance.
 *
 * The result may optionally be passed to
 * [io.opentelemetry.kotlin.OpenTelemetry] during its construction so that the SDK reuses the
 * same propagators.
 */
@ExperimentalApi
public fun createPropagators(): Propagators = PropagatorsImpl()
