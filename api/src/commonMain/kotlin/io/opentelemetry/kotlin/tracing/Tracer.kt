package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.ThreadSafe
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.tracing.model.Span
import io.opentelemetry.kotlin.tracing.model.SpanKind
import io.opentelemetry.kotlin.tracing.model.SpanRelationships

/**
 * A Tracer is responsible for creating spans.
 *
 * https://opentelemetry.io/docs/specs/otel/trace/api/#tracerprovider
 */
@ExperimentalApi
@ThreadSafe
public interface Tracer {

    /**
     * Creates a new span.
     */
    @ThreadSafe
    public fun createSpan(
        name: String,
        parentContext: Context? = null,
        spanKind: SpanKind = SpanKind.INTERNAL,
        startTimestamp: Long? = null,
        action: (SpanRelationships.() -> Unit)? = null
    ): Span
}
