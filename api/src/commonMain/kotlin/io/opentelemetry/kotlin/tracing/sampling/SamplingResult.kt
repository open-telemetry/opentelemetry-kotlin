package io.opentelemetry.kotlin.tracing.sampling

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.attributes.AttributeContainer
import io.opentelemetry.kotlin.tracing.model.TraceState

/**
 * The result of a decision on whether a [io.opentelemetry.kotlin.tracing.model.Span] should
 * be sampled or not.
 */
@ExperimentalApi
public interface SamplingResult {

    /**
     * The sampling decision
     */
    public enum class Decision {

        /**
         * The span will not be recorded and it will not be sampled.
         */
        DROP,

        /**
         * The span will be recorded but it will not be sampled.
         */
        RECORD_ONLY,

        /**
         * The span will be recorded and it will be sampled.
         */
        RECORD_AND_SAMPLE,
    }

    /**
     * The attributes that were added to the span.
     */
    public val attributes: AttributeContainer

    /**
     * The [TraceState] that will be associated with the [io.opentelemetry.kotlin.tracing.model.Span]
     * through the new [io.opentelemetry.kotlin.tracing.model.SpanContext].
     */
    public val traceState: TraceState
}
