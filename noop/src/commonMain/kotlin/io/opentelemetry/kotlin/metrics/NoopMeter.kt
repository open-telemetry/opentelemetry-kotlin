package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * No-op implementation of [Meter].
 *
 * This implementation should induce as close to zero overhead as possible.
 */
@ExperimentalApi
internal object NoopMeter : Meter
