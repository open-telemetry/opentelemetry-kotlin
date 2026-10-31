package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaLongUpDownCounter
import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.attributes.CompatAttributesModel
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.error.sdkGuard
import io.opentelemetry.kotlin.error.sdkGuardOrDefault
import io.opentelemetry.kotlin.error.userCode

@ExperimentalApi
internal class LongUpDownCounterAdapter(
    private val impl: OtelJavaLongUpDownCounter,
    override val name: String,
    override val unit: String?,
    override val description: String?,
    private val sdkErrorHandler: SdkErrorHandler,
) : LongUpDownCounter {
    override fun enabled(): Boolean =
        sdkErrorHandler.sdkGuardOrDefault(false, "LongUpDownCounter.enabled failed") {
            impl.isEnabled()
        }

    override fun add(value: Long, attributes: AttributesMutator.() -> Unit) {
        sdkErrorHandler.sdkGuard("LongUpDownCounter.add failed") {
            val container = CompatAttributesModel()
            userCode { attributes(container) }
            impl.add(value, container.otelJavaAttributes())
        }
    }
}
