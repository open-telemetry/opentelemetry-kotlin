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
     * A snapshot of spans that have been exported since the last [reset].
     */
    public val exportedSpans: List<SpanData>

    /**
     * Clears exported spans, leaving previously returned snapshots unchanged.
     * This does not shut down the exporter or reactivate it after shutdown.
     */
    public fun reset()
}
