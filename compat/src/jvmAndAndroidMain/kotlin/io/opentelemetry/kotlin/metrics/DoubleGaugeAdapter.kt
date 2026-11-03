package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaDoubleGauge
import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.attributes.compatAttributes
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.error.sdkGuard
import io.opentelemetry.kotlin.error.sdkGuardOrDefault

@ExperimentalApi
internal class DoubleGaugeAdapter(
    private val impl: OtelJavaDoubleGauge,
    override val name: String,
    override val unit: String?,
    override val description: String?,
    private val sdkErrorHandler: SdkErrorHandler,
) : DoubleGauge {
    override fun enabled(): Boolean =
        sdkErrorHandler.sdkGuardOrDefault(false, "DoubleGauge.enabled failed") { impl.isEnabled() }

    override fun record(value: Double, attributes: AttributesMutator.() -> Unit) {
        sdkErrorHandler.sdkGuard("DoubleGauge.record failed") {
            val container = compatAttributes(attributes)
            impl.set(value, container.otelJavaAttributes())
        }
    }
}
