package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaObservableLongMeasurement
import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.attributes.CompatAttributesModel
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.error.sdkGuard
import io.opentelemetry.kotlin.error.userCode

@ExperimentalApi
internal class ObservableLongMeasurementAdapter(
    private val impl: OtelJavaObservableLongMeasurement,
    private val sdkErrorHandler: SdkErrorHandler,
) : ObservableLongMeasurement {
    override fun record(value: Long) {
        sdkErrorHandler.sdkGuard("ObservableLongMeasurement.record failed") {
            impl.record(value)
        }
    }

    override fun record(
        value: Long,
        attributes: AttributesMutator.() -> Unit,
    ) {
        sdkErrorHandler.sdkGuard("ObservableLongMeasurement.record failed") {
            val container = CompatAttributesModel()
            userCode { attributes(container) }
            impl.record(value, container.otelJavaAttributes())
        }
    }
}
