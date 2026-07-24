package io.opentelemetry.kotlin.tracing.sampling

import io.opentelemetry.kotlin.attributes.AttributeContainer
import io.opentelemetry.kotlin.attributes.AttributesModel
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.tracing.SpanKind
import io.opentelemetry.kotlin.tracing.model.SpanLink
import kotlin.random.Random

/**
 * A [Sampler] that delegates its sampling preference to a [ComposableSampler], then applies the
 * consistent probability sampling (R >= T) algorithm to reach a final [SamplingResult].
 *
 * https://opentelemetry.io/docs/specs/otel/trace/sdk/#compositesampler
 */
public class CompositeSampler(
    private val delegate: ComposableSampler,
    private val random: Random = Random.Default,
) : Sampler {

    override fun shouldSample(
        context: Context,
        traceId: String,
        name: String,
        spanKind: SpanKind,
        attributes: AttributeContainer,
        links: List<SpanLink>
    ): SamplingResult {
        val traceState = context.extractSpan().spanContext.traceState
        val otelTraceState = OtelTraceState.parse(traceState.get(KnownTraceState.OT))

        val intent = delegate.getSamplingIntent(context, name, spanKind, attributes, links)
        // this unlocks smart-cast
        val intentThreshold = intent.threshold

        val adjustableThreshold = intentThreshold != null && intent.adjustedCountReliable
        val sampled = intentThreshold?.let {
            val randomVal = if (adjustableThreshold) {
                otelTraceState.rv ?: randomnessFromTraceId(traceId)
            } else {
                // Use last 56 bits of random number
                random.nextLong() and 0x00FFFFFFFFFFFFFFL
            }
            intentThreshold <= randomVal
        } ?: false

        val decision = if (sampled) {
            SamplingResult.Decision.RECORD_AND_SAMPLE
        } else {
            SamplingResult.Decision.DROP
        }

        val derivedOtelTraceState = intent.traceStateProvider
            ?.invoke(traceState, decision)
            ?.get(KnownTraceState.OT)
            ?.let(OtelTraceState::parse)
            ?: otelTraceState
        if (sampled && adjustableThreshold) {
            intentThreshold?.let(derivedOtelTraceState::setThreshold)
        } else {
            derivedOtelTraceState.eraseThreshold()
        }

        val derivedTraceState = if (derivedOtelTraceState.encode().isEmpty()) {
            traceState.remove(KnownTraceState.OT)
        } else {
            traceState.put(KnownTraceState.OT, derivedOtelTraceState.encode())
        }
        val attr = if (sampled) {
            intent.attributesProvider?.invoke() ?: AttributesModel()
        } else {
            AttributesModel()
        }

        return SamplingResultImpl(decision, attr, derivedTraceState)
    }

    override val description: String
        get() = "CompositeSampler{${delegate.description}}"
}
