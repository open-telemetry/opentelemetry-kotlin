package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaObservableLongMeasurement
import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.attributes.compatAttributes
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.error.sdkGuard

@OptIn(ExperimentalApi::class)
internal class ObservableLongMeasurementAdapter(
    private val impl: OtelJavaObservableLongMeasurement,
    private val sdkErrorHandler: SdkErrorHandler,
) : ObservableLongMeasurement {
    override fun record(
        value: Long,
        attributes: (AttributesMutator.() -> Unit)?,
    ) {
        sdkErrorHandler.sdkGuard("ObservableLongMeasurement.record failed") {
            if (attributes == null) {
                impl.record(value)
            } else {
                val container = compatAttributes(attributes)
                impl.record(value, container.otelJavaAttributes())
            }
        }
    }
}
