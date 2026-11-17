package io.opentelemetry.kotlin.behavior.tracing

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.behavior.Behavior
import io.opentelemetry.kotlin.behavior.ConsoleExporterBehavior
import io.opentelemetry.kotlin.behavior.Exporter
import io.opentelemetry.kotlin.behavior.ExporterDelegate
import io.opentelemetry.kotlin.behavior.OtlpExporter
import io.opentelemetry.kotlin.behavior.mergeNode

/**
 * Exporters for the tracer provider. These are independent of the kind of processor (simple or batch) that feeds them.
 *
 * https://opentelemetry.io/docs/specs/otel/trace/sdk/#span-exporter
 */
@OptIn(ExperimentalApi::class)
data class SpanExporterBehavior(
    val delegate: ExporterDelegate<OtlpHttpSpanExporterBehavior> = ExporterDelegate(),
) : Behavior<SpanExporterBehavior>, Exporter by delegate {
    constructor(
        console: ConsoleExporterBehavior? = null,
        http: OtlpHttpSpanExporterBehavior? = null,
    ):this(ExporterDelegate(console = console, http = http))

    override fun mergeWith(higher: SpanExporterBehavior): SpanExporterBehavior = copy(
            delegate = delegate.copy(
                console = mergeNode(console, higher.console),
                http = mergeNode(
                    http as OtlpHttpSpanExporterBehavior?,
                    higher.http as OtlpHttpSpanExporterBehavior?
                ),
            )
        )
}
