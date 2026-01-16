package io.opentelemetry.kotlin.tracing.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.tracing.data.SpanData

/**
 * A span exporter that stores telemetry in memory. This is intended for development/testing
 * rather than production use.
 */
@ExperimentalApi
public interface InMemorySpanExporter : SpanExporter {
    /**
     * A list of spans that have been exported.
     */
    public val exportedSpans: List<SpanData>
}
