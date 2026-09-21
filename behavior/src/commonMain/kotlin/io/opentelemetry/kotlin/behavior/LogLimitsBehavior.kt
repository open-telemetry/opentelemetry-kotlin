package io.opentelemetry.kotlin.behavior

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * Limits on log record data capture, which the spec defines as the same set of attribute limits the
 * SDK applies globally.
 *
 * https://opentelemetry.io/docs/specs/otel/logs/sdk/#logrecord-limits
 */
@ExperimentalApi
typealias LogLimitsBehavior = AttributeLimitsBehavior
