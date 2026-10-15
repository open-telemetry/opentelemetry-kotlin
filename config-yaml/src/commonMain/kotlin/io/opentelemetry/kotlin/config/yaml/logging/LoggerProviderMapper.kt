package io.opentelemetry.kotlin.config.yaml.logging

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.behavior.LoggerProviderBehavior
import io.opentelemetry.kotlin.config.schema.model.LoggerProvider

/**
 * Maps`tracer_provider` onto its behavior.
 */
@ExperimentalApi
fun LoggerProvider.toBehavior(): LoggerProviderBehavior? {
    val limits = limits?.toBehavior()
    val processor = processors.toBehavior()
    if (limits == null && processor == null) {
        return null
    }
    return LoggerProviderBehavior(
        logLimits = limits,
        processor = processor,
    )
}
