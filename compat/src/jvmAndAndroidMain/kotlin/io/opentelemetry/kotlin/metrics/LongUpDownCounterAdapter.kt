package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaLongUpDownCounter
import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.attributes.compatAttributes
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.error.sdkGuard
import io.opentelemetry.kotlin.error.sdkGuardOrDefault

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
            val container = compatAttributes(attributes)
            impl.add(value, container.otelJavaAttributes())
        }
    }
}
