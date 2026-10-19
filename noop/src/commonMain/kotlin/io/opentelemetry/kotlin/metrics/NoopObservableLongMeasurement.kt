package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.attributes.AttributesMutator

/**
 * No-op implementation of [ObservableLongMeasurement].
 */
@ExperimentalApi
internal class NoopObservableLongMeasurement : ObservableLongMeasurement {
    override fun record(value: Long) = Unit
    override fun record(value: Long, attributes: AttributesMutator.() -> Unit) = Unit
}
