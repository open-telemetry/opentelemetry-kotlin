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
internal class ComposableParentThresholdSampler(private val root: ComposableSampler) : ComposableSampler {

    override fun getSamplingIntent(
        context: Context,
        name: String,
        spanKind: SpanKind,
        attributes: AttributeContainer,
        links: List<SpanLink>
    ): SamplingIntent {
        val parent = context.span.spanContext
        val parentOt = parent.traceState.get(KnownTraceState.OT)?.let(OtelTraceState::parse)
        // A threshold that disagrees with the parent's sampled flag is erased, and the parent's
        // sampled flag decides instead.
        val parentThreshold = parentOt?.th?.takeIf { threshold ->
            val randomness = parentOt.rv ?: randomnessFromTraceIdBytes(parent.traceIdBytes)
            parent.traceFlags.isSampled == (randomness >= threshold)
        }

        return when {
            !parent.isValid -> root.getSamplingIntent(context, name, spanKind, attributes, links)
            parentThreshold != null -> SamplingIntentImpl(threshold = parentThreshold, adjustedCountReliable = true)
            parent.traceFlags.isSampled -> SAMPLED_PARENT_WITHOUT_THRESHOLD
            else -> NOT_SAMPLED
        }
    }

    override val description: String
        get() = "ComposableParentThresholdSampler{root:${root.description}}"

    private companion object {
        private val SAMPLED_PARENT_WITHOUT_THRESHOLD =
            SamplingIntentImpl(threshold = 0, adjustedCountReliable = false)
        private val NOT_SAMPLED =
            SamplingIntentImpl(threshold = null, adjustedCountReliable = false)
    }
}
