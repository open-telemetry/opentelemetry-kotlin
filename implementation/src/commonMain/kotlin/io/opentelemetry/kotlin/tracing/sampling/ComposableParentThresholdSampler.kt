package io.opentelemetry.kotlin.tracing.sampling

import io.opentelemetry.kotlin.attributes.AttributeContainer
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.tracing.SpanKind
import io.opentelemetry.kotlin.tracing.model.SpanLink

/**
 * A [ComposableSampler] that honors the parent's sampling threshold when present, falling back
 * to [root] when there is no valid parent.
 *
 * https://opentelemetry.io/docs/specs/otel/trace/sdk/#composableparentthreshold
 */
public class ComposableParentThresholdSampler(private val root: ComposableSampler) : ComposableSampler {

    override fun getSamplingIntent(
        context: Context,
        name: String,
        spanKind: SpanKind,
        attributes: AttributeContainer,
        links: List<SpanLink>
    ): SamplingIntent {
        val parent = context.extractSpan().spanContext
        if (!parent.isValid) {
            return root.getSamplingIntent(context, name, spanKind, attributes, links)
        }

        val parentThreshold = parent.traceState.get(KnownTraceState.OT)?.let(OtelTraceState::parse)?.th
        if (parentThreshold != null) {
            return SamplingIntentImpl(threshold = parentThreshold, adjustedCountReliable = true)
        }

        if (parent.traceFlags.isSampled) {
            return SamplingIntentImpl(threshold = 0, adjustedCountReliable = false)
        }
        return SamplingIntentImpl(threshold = null, adjustedCountReliable = false)
    }

    override val description: String
        get() = "ComposableParentThresholdSampler{root:${root.description}}"
}
