package io.opentelemetry.kotlin.behavior

import io.opentelemetry.kotlin.ExperimentalApi

/**
 * Exporters for the tracer provider. These are independent of the kind of processor (simple or batch) that feeds them.
 *
 * https://opentelemetry.io/docs/specs/otel/trace/sdk/#span-exporter
 */
@ExperimentalApi
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
