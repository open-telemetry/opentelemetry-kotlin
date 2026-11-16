package io.opentelemetry.kotlin.behavior.logging

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.behavior.AttributeLimitsBehavior

/**
 * Limits on log record data capture, which the spec defines as the same set of attribute limits the
 * SDK applies globally.
 *
 * https://opentelemetry.io/docs/specs/otel/logs/sdk/#logrecord-limits
 */
@OptIn(ExperimentalApi::class)
typealias LogLimitsBehavior = AttributeLimitsBehavior
