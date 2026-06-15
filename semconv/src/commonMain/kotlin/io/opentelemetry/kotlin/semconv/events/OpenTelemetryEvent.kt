package io.opentelemetry.kotlin.semconv.events

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.logging.Logger

@ExperimentalApi
interface OpenTelemetryEvent {
    fun emit(
        logger: Logger,
        attributes: (AttributesMutator.() -> Unit)? = null,
    )
}
