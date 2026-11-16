package io.opentelemetry.kotlin.behavior.tracing

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.behavior.Behavior
import io.opentelemetry.kotlin.behavior.ConsoleExporterBehavior
import io.opentelemetry.kotlin.behavior.mergeNode

/**
 * Exporters for the tracer provider. These are independent of the kind of processor (simple or batch) that feeds them.
 *
 * https://opentelemetry.io/docs/specs/otel/trace/sdk/#span-exporter
 */
@OptIn(ExperimentalApi::class)
data class SpanExporterBehavior(
    /**
     * Console span exporter.
     * */
    val console: ConsoleExporterBehavior? = null,
    /**
     * HTTP span exporter.
     * */
    val http: OtlpHttpSpanExporterBehavior? = null,
) : Behavior<SpanExporterBehavior> {
    override fun mergeWith(higher: SpanExporterBehavior): SpanExporterBehavior {
        return SpanExporterBehavior(
            console = mergeNode(console, higher.console),
            http = mergeNode(http, higher.http),
        )
    }
}
