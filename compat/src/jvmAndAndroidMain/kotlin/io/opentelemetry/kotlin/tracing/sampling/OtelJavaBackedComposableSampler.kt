package io.opentelemetry.kotlin.tracing.sampling

import io.opentelemetry.kotlin.aliases.OtelJavaAttributes
import io.opentelemetry.kotlin.aliases.OtelJavaComposableSampler
import io.opentelemetry.kotlin.attributes.AttributeContainer
import io.opentelemetry.kotlin.attributes.CompatAttributesModel
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.context.toOtelJavaContext
import io.opentelemetry.kotlin.tracing.SpanKind
import io.opentelemetry.kotlin.tracing.TraceState
import io.opentelemetry.kotlin.tracing.ext.toOtelJavaSpanKind
import io.opentelemetry.kotlin.tracing.ext.toOtelJavaTraceState
import io.opentelemetry.kotlin.tracing.model.SpanLink
import io.opentelemetry.kotlin.tracing.model.TraceStateAdapter

internal class OtelJavaBackedComposableSampler(internal val impl: OtelJavaComposableSampler) : ComposableSampler {

    override fun getSamplingIntent(
        context: Context,
        name: String,
        spanKind: SpanKind,
        attributes: AttributeContainer,
        links: List<SpanLink>,
    ): SamplingIntent {
        val traceId = context.extractSpan().spanContext.traceId
        val javaAttributes = (attributes as? CompatAttributesModel)?.otelJavaAttributes()
            ?: OtelJavaAttributes.empty()
        val javaIntent = impl.getSamplingIntent(
            context.toOtelJavaContext(),
            traceId,
            name,
            spanKind.toOtelJavaSpanKind(),
            javaAttributes,
            emptyList(),
        )

        val javaThreshold = javaIntent.threshold
        val javaTraceStateUpdater = javaIntent.traceStateUpdater
        val javaSamplingAttributes = javaIntent.attributes

        return object : SamplingIntent {
            override val threshold: Long? =
                javaThreshold.takeIf { it in VALID_THRESHOLD_RANGE }
            override val adjustedCountReliable: Boolean = javaIntent.isThresholdReliable
            override val attributesProvider: (() -> AttributeContainer)? = if (javaSamplingAttributes.isEmpty) {
                null
            } else {
                { CompatAttributesModel(javaSamplingAttributes.toBuilder()) }
            }
            override val traceStateProvider: ((TraceState, SamplingResult.Decision) -> TraceState)? =
                javaTraceStateUpdater?.let { updater ->
                    {
                            traceState, _ ->
                        TraceStateAdapter(updater.apply(traceState.toOtelJavaTraceState()))
                    }
                }
        }
    }

    override val description: String
        get() = impl.description

    companion object {
        /** Valid thresholds lie in `[0, 2^56)`; anything outside that range means "never sample". */
        private const val MAX_THRESHOLD: Long = 1L shl 56
        private val VALID_THRESHOLD_RANGE = 0 until MAX_THRESHOLD
    }
}
