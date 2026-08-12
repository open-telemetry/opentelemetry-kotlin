package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.ThreadSafe

/**
 * Super-interface for observing measurements from an asynchronous instrument's callback.
 *
 * https://opentelemetry.io/docs/specs/otel/metrics/api/#measurement
 */
@ExperimentalApi
@ThreadSafe
public interface ObservableMeasurement
