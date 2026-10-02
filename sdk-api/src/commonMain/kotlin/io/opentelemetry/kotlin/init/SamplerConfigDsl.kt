package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.factory.SpanFactory
import io.opentelemetry.kotlin.tracing.sampling.ComposableSampler
import io.opentelemetry.kotlin.tracing.sampling.Sampler

/**
 * Configures how traces are sampled.
 */
@ExperimentalApi
@ConfigDsl
public interface SamplerConfigDsl {

    /**
     * The [SpanFactory] implementation that will be used by the OpenTelemetry implementation.
     */
    public val spanFactory: SpanFactory

    /**
     * Configures sampling so that spans are always recorded and sampled.
     *
     * https://opentelemetry.io/docs/specs/otel/trace/sdk/#alwayson
     */
    public fun alwaysOn(): Sampler

    /**
     * Configures sampling so that spans are never recorded and sampled.
     *
     * https://opentelemetry.io/docs/specs/otel/trace/sdk/#alwaysoff
     */
    public fun alwaysOff(): Sampler

    /**
     * Configures sampling so that spans are always recorded, even if the delegate sampler
     * would otherwise drop them.
     *
     * https://opentelemetry.io/docs/specs/otel/trace/sdk/#alwaysrecord
     */
    public fun alwaysRecord(root: Sampler): Sampler

    /**
     * Configures sampling based on the parent span's sampling decision.
     *
     * https://opentelemetry.io/docs/specs/otel/trace/sdk/#parentbased
     */
    public fun parentBased(
        root: Sampler,
        remoteParentSampled: Sampler = alwaysOn(),
        remoteParentNotSampled: Sampler = alwaysOff(),
        localParentSampled: Sampler = alwaysOn(),
        localParentNotSampled: Sampler = alwaysOff(),
    ): Sampler

    /**
     * Configures sampling by delegating to a [ComposableSampler], using consistent probability
     * sampling over the OpenTelemetry TraceState `ot` `th`/`rv` sub-keys.
     *
     * https://opentelemetry.io/docs/specs/otel/trace/sdk/#compositesampler
     */
    public fun composite(block: SamplerConfigDsl.() -> ComposableSampler): Sampler

    /**
     * A [ComposableSampler] that always samples, regardless of parent trace state.
     *
     * https://opentelemetry.io/docs/specs/otel/trace/sdk/#composablealwayson
     */
    public fun composableAlwaysOn(): ComposableSampler

    /**
     * A [ComposableSampler] that never samples.
     *
     * https://opentelemetry.io/docs/specs/otel/trace/sdk/#composablealwaysoff
     */
    public fun composableAlwaysOff(): ComposableSampler

    /**
     * A [ComposableSampler] that samples spans with the given probability [ratio].
     *
     * https://opentelemetry.io/docs/specs/otel/trace/sdk/#composableprobability
     */
    public fun composableProbability(ratio: Double): ComposableSampler

    /**
     * A [ComposableSampler] that honors the parent's sampling threshold when present, falling back
     * to [root] when there is no valid parent.
     *
     * https://opentelemetry.io/docs/specs/otel/trace/sdk/#composableparentthreshold
     */
    public fun composableParentThreshold(root: ComposableSampler): ComposableSampler

    /**
     * A [ComposableSampler] that leaves the sampling decision to [delegate] but adds attributes to
     * spans that are sampled.
     *
     * https://opentelemetry.io/docs/specs/otel/trace/sdk/#composableannotating
     */
    public fun composableAnnotating(
        delegate: ComposableSampler,
        attributes: AttributesMutator.() -> Unit,
    ): ComposableSampler

    /**
     * A [ComposableSampler] that evaluates rules in the order they are declared, delegating to the
     * first matching rule's sampler. Spans that match no rule are not sampled.
     *
     * https://opentelemetry.io/docs/specs/otel/trace/sdk/#composablerulebased
     */
    public fun composableRuleBased(block: ComposableRuleBasedConfigDsl.() -> Unit): ComposableSampler
}
