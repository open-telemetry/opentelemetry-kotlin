package io.opentelemetry.kotlin.tracing.sampling

import io.opentelemetry.kotlin.attributes.AttributeContainer
import io.opentelemetry.kotlin.attributes.AttributesModel
import io.opentelemetry.kotlin.attributes.EmptyAttributeContainer
import io.opentelemetry.kotlin.attributes.typedAttributesOrConvert
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.error.guardOrDefault
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.init.config.DefaultSampler
import io.opentelemetry.kotlin.tracing.SpanKind
import io.opentelemetry.kotlin.tracing.model.SpanLink

internal class GuardedSampler(
    private val delegate: Sampler,
    private val sdkErrorHandler: SdkErrorHandler,
    private val fallback: Sampler = AlwaysOffSampler,
) : Sampler {

    override val description: String
        get() = sdkErrorHandler.guardOrDefault(fallback.description, "Sampler.description failed") {
            delegate.description
        }

    override fun shouldSample(
        context: Context,
        traceIdBytes: ByteArray,
        name: String,
        spanKind: SpanKind,
        attributes: AttributeContainer,
        links: List<SpanLink>,
    ): SamplingResult = sdkErrorHandler.guardOrDefault(null, "Sampler failed, using fallback sampler") {
        val result = delegate.shouldSample(context, traceIdBytes, name, spanKind, attributes, links)
        val decision = result.decision
        val traceState = result.traceState.also { it.asMap() }
        val resultAttributes = when (decision) {
            SamplingResult.Decision.DROP -> EmptyAttributeContainer
            else -> sdkErrorHandler.guardOrDefault(EmptyAttributeContainer, "SamplingResult.attributes failed") {
                AttributesModel(attrs = result.attributes.typedAttributesOrConvert().toMutableMap())
            }
        }
        SamplingResultImpl(decision, resultAttributes, traceState)
    } ?: fallback.shouldSample(context, traceIdBytes, name, spanKind, attributes, links)

    override suspend fun shutdown(): OperationResultCode = delegate.shutdown()
}

internal fun Sampler.guarded(sdkErrorHandler: SdkErrorHandler): Sampler = when {
    this === AlwaysOnSampler || this === AlwaysOffSampler || this === DefaultSampler || this is GuardedSampler -> this
    else -> GuardedSampler(this, sdkErrorHandler)
}
