package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaObservableDoubleMeasurement
import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.attributes.compatAttributes
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.error.sdkGuard

@OptIn(ExperimentalApi::class)
internal class ObservableDoubleMeasurementAdapter(
    private val impl: OtelJavaObservableDoubleMeasurement,
    private val sdkErrorHandler: SdkErrorHandler,
) : ObservableDoubleMeasurement {
    override fun record(
        value: Double,
        attributes: (AttributesMutator.() -> Unit)?,
    ) {
        sdkErrorHandler.sdkGuard("ObservableDoubleMeasurement.record failed") {
            if (attributes == null) {
                impl.record(value)
            } else {
                val container = compatAttributes(attributes)
                impl.record(value, container.otelJavaAttributes())
            }
        }
    }
}
