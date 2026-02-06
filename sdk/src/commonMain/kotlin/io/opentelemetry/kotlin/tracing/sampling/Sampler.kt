package io.opentelemetry.kotlin.tracing.sampling

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.attributes.AttributeContainer
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.tracing.model.Link
import io.opentelemetry.kotlin.tracing.model.SpanKind

/**
 * Decides whether a [io.opentelemetry.kotlin.tracing.model.Span] should be sampled or not
 * by using information just before it's created to return a [SamplingResult].
 *
 * https://opentelemetry.io/docs/specs/otel/trace/sdk/#sampler
 */
@ExperimentalApi
public interface Sampler {

    /**
     * Returns whether the span should be sampled or not.
     *
     * @param context A context containing the parent span
     * @param traceId The traceId of the span to be created
     * @param name The name of the span to be created
     * @param spanKind The spanKind of the span to be created
     * @param attributes The initial set of attributes of the span to be created
     * @param links A collection of links that will be associated with the created span
     */
    public fun shouldSample(
        context: Context,
        traceId: String,
        name: String,
        spanKind: SpanKind,
        attributes: AttributeContainer,
        links: List<Link>,
    ): SamplingResult

    /**
     * The name of the sampler or a short description that may be displayed in debug pages/logs.
     */
    public val description: String
}
