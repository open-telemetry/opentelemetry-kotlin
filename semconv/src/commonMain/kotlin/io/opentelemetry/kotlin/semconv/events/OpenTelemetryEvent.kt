package io.opentelemetry.kotlin.semconv.events

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.logging.Logger

@OptIn(ExperimentalApi::class)
interface OpenTelemetryEvent {
    fun emit(logger: Logger)
}