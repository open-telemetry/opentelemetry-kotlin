package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaObservableDoubleMeasurement
import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.attributes.CompatAttributesModel
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.error.sdkGuard
import io.opentelemetry.kotlin.error.userCode

@ExperimentalApi
internal class ObservableDoubleMeasurementAdapter(
    private val impl: OtelJavaObservableDoubleMeasurement,
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
            val container = CompatAttributesModel()
            userCode { attributes(container) }
            impl.record(value, container.otelJavaAttributes())
        }
    }
}
