package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaDoubleUpDownCounter
import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.attributes.CompatAttributesModel
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.error.sdkGuard
import io.opentelemetry.kotlin.error.sdkGuardOrDefault
import io.opentelemetry.kotlin.error.userCode

@ExperimentalApi
internal class DoubleUpDownCounterAdapter(
    private val impl: OtelJavaDoubleUpDownCounter,
    override val name: String,
    override val unit: String?,
    override val description: String?,
    private val sdkErrorHandler: SdkErrorHandler,
) : DoubleUpDownCounter {
    override fun enabled(): Boolean =
        sdkErrorHandler.sdkGuardOrDefault(false, "DoubleUpDownCounter.enabled failed") {
            impl.isEnabled()
        }

    override fun add(value: Double, attributes: AttributesMutator.() -> Unit) {
        sdkErrorHandler.sdkGuard("DoubleUpDownCounter.add failed") {
            val container = CompatAttributesModel()
            userCode { attributes(container) }
            impl.add(value, container.otelJavaAttributes())
        }
    }
}
