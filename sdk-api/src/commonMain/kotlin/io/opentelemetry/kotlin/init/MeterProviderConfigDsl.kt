package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * Defines configuration for the [io.opentelemetry.kotlin.metrics.MeterProvider].
 */
@ExperimentalApi
@ConfigDsl
public interface MeterProviderConfigDsl : ResourceConfigDsl
