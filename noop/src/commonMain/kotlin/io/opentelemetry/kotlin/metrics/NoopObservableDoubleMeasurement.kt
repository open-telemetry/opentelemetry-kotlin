package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.attributes.AttributesMutator

/**
 * No-op implementation of [ObservableDoubleMeasurement].
 */
@OptIn(ExperimentalApi::class)
internal class NoopObservableDoubleMeasurement : ObservableDoubleMeasurement {
    override fun record(value: Double, attributes: (AttributesMutator.() -> Unit)?) = Unit
}
