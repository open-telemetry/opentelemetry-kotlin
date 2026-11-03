package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaLongGauge
import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.attributes.CompatAttributesModel
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.error.sdkGuard
import io.opentelemetry.kotlin.error.sdkGuardOrDefault
import io.opentelemetry.kotlin.error.userCode

@ExperimentalApi
internal class LongGaugeAdapter(
    private val impl: OtelJavaLongGauge,
    override val name: String,
    override val unit: String?,
    override val description: String?,
    private val sdkErrorHandler: SdkErrorHandler,
) : LongGauge {
    override fun enabled(): Boolean =
        sdkErrorHandler.sdkGuardOrDefault(false, "LongGauge.enabled failed") { impl.isEnabled() }

    override fun record(value: Long, attributes: AttributesMutator.() -> Unit) {
        sdkErrorHandler.sdkGuard("LongGauge.record failed") {
            val container = CompatAttributesModel()
            userCode { attributes(container) }
            impl.set(value, container.otelJavaAttributes())
        }
    }
}
