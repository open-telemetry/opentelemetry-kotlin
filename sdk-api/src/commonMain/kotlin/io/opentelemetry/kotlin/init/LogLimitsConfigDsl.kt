package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * Defines limits on how much data should be captured in log records.
 */
@ExperimentalApi
@ConfigDsl
public interface LogLimitsConfigDsl : AttributeLimitsConfigDsl
