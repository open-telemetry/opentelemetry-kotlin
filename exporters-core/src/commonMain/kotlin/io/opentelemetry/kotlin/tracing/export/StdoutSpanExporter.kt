package io.opentelemetry.kotlin.tracing.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.tracing.data.SpanData
import io.opentelemetry.kotlin.tracing.data.StatusData

/**
 * A [SpanExporter] that outputs span data to stdout.
 */
@OptIn(ExperimentalApi::class)
internal class StdoutSpanExporter(
    private val logger: (String) -> Unit = ::println
) : SpanExporter {

    override suspend fun export(telemetry: List<SpanData>): OperationResultCode {
        telemetry.forEach { span ->
            logger(formatSpan(span))
        }
        return OperationResultCode.Success
    }

    override suspend fun forceFlush(): OperationResultCode = OperationResultCode.Success
    override suspend fun shutdown(): OperationResultCode = OperationResultCode.Success

    /**
     * Formats a [SpanData] into a human-readable string representation.
     *
     * @param span The span data to format.
     * @return A formatted string containing the span's key information.
     */
    private fun formatSpan(span: SpanData): String = buildString {
        append("Span: ")
        append(span.name)
        append(" [")
        append(span.spanKind)
        append("]")
        appendLine()

        append("  TraceId: ")
        appendLine(span.spanContext.traceId)

        append("  SpanId: ")
        appendLine(span.spanContext.spanId)

        append("  ParentSpanId: ")
        appendLine(span.parent.spanId)

        append("  Status: ")
        append(span.status.statusCode)
        if (span.status is StatusData.Error && span.status.description != null) {
            append(" - ")
            append(span.status.description)
        }
        appendLine()

        append("  StartTime: ")
        appendLine(span.startTimestamp)

        span.endTimestamp?.let {
            append("  EndTime: ")
            appendLine(it)
            append("  Duration: ")
            append(it - span.startTimestamp)
            appendLine(" ns")
        }

        if (span.attributes.isNotEmpty()) {
            appendLine("  Attributes:")
            span.attributes.forEach { (key, value) ->
                append("    ")
                append(key)
                append(": ")
                appendLine(value)
            }
        }

        if (span.events.isNotEmpty()) {
            appendLine("  Events:")
            span.events.forEach { event ->
                append("    - ")
                append(event.name)
                append(" @ ")
                appendLine(event.timestamp)
            }
        }

        if (span.links.isNotEmpty()) {
            append("  Links: ")
            appendLine(span.links.size)
        }

        append("  Resource: ")
        appendLine(span.resource.attributes)

        append("  InstrumentationScope: ")
        append(span.instrumentationScopeInfo.name)
        span.instrumentationScopeInfo.version?.let {
            append(":")
            append(it)
        }
    }
}
