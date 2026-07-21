package io.opentelemetry.kotlin.logging

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * Immutable model of how a [Logger] should behave.
 */
@ExperimentalApi
public interface LoggerConfig {

    /**
     * Whether the logger is enabled.
     */
    public val enabled: Boolean
}
