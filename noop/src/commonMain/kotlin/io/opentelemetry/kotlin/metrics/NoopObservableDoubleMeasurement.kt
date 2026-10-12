package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.attributes.AttributesMutator

/**
 * No-op implementation of [ObservableDoubleMeasurement].
 */
@ExperimentalApi
internal class NoopObservableDoubleMeasurement : ObservableDoubleMeasurement {
    override fun record(value: Double) = Unit
    override fun record(value: Double, attributes: AttributesMutator.() -> Unit) = Unit
}
