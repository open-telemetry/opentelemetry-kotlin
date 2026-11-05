package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaObservableDoubleMeasurement
import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.attributes.compatAttributes
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.error.sdkGuard

@ExperimentalApi
internal class ObservableDoubleMeasurementAdapter(
    internal val impl: OtelJavaObservableDoubleMeasurement,
    private val sdkErrorHandler: SdkErrorHandler,
) : ObservableDoubleMeasurement {
    override fun record(value: Double) {
        sdkErrorHandler.sdkGuard("ObservableDoubleMeasurement.record failed") {
            impl.record(value)
        }
    }

    override fun record(
        value: Double,
        attributes: AttributesMutator.() -> Unit,
    ) {
        sdkErrorHandler.sdkGuard("ObservableDoubleMeasurement.record failed") {
            val container = compatAttributes(attributes)
            impl.record(value, container.otelJavaAttributes())
        }
    }
}
