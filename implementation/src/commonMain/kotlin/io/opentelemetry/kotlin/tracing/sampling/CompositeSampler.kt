package io.opentelemetry.kotlin.tracing.sampling

import io.opentelemetry.kotlin.attributes.AttributeContainer
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
    ): SamplingResult = TODO()

    override val description: String
        get() = "CompositeSampler{${delegate.description}}"
}
