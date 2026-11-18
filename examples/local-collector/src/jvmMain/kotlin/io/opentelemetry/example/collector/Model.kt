package io.opentelemetry.example.collector

import io.opentelemetry.kotlin.proto.collector.logs.v1.ExportLogsServiceRequest
import io.opentelemetry.kotlin.proto.collector.trace.v1.ExportTraceServiceRequest
import io.opentelemetry.kotlin.proto.common.v1.AnyValue
import io.opentelemetry.kotlin.proto.common.v1.InstrumentationScope
import io.opentelemetry.kotlin.proto.common.v1.KeyValue
import io.opentelemetry.kotlin.proto.resource.v1.Resource
import io.opentelemetry.kotlin.proto.trace.v1.Status
import okio.ByteString
import java.time.Instant

data class ReceivedSpan(
    val name: String,
    val serviceName: String,
    val traceId: String,
    val spanId: String,
    val parentSpanId: String,
    val kind: String,
    val startNanos: Long,
    val endNanos: Long,
    val status: String,
    val attributes: List<Pair<String, String>>,
    val resource: List<Pair<String, String>>,
    val scope: String,
    val events: List<ReceivedEvent>,
    val links: List<String>,
) {
    val durationMs: Double get() = (endNanos - startNanos) / 1_000_000.0
}

data class ReceivedEvent(
    val name: String,
    val timeNanos: Long,
    val attributes: List<Pair<String, String>>,
)

data class ReceivedLog(
    val severity: String,
    val body: String,
    val serviceName: String,
    val eventName: String,
    val timeNanos: Long,
    val traceId: String,
    val spanId: String,
    val attributes: List<Pair<String, String>>,
    val resource: List<Pair<String, String>>,
    val scope: String,
)

fun ExportTraceServiceRequest.toReceivedSpans(): List<ReceivedSpan> =
    resource_spans.flatMap { rs ->
        val resource = rs.resource.toPairs()
        val service = resource.serviceName()
        rs.scope_spans.flatMap { ss ->
            val scope = ss.scope.describe()
            ss.spans.map { span ->
                ReceivedSpan(
                    name = span.name,
                    serviceName = service,
                    traceId = span.trace_id.hexOrEmpty(),
                    spanId = span.span_id.hexOrEmpty(),
                    parentSpanId = span.parent_span_id.hexOrEmpty(),
                    kind = span.kind.name.removePrefix("SPAN_KIND_"),
                    startNanos = span.start_time_unix_nano,
                    endNanos = span.end_time_unix_nano,
                    status = span.status.describe(),
                    attributes = span.attributes.toPairs(),
                    resource = resource,
                    scope = scope,
                    events = span.events.map {
                        ReceivedEvent(it.name, it.time_unix_nano, it.attributes.toPairs())
                    },
                    links = span.links.map { "${it.trace_id.hexOrEmpty()} / ${it.span_id.hexOrEmpty()}" },
                )
            }
        }
    }

fun ExportLogsServiceRequest.toReceivedLogs(): List<ReceivedLog> =
    resource_logs.flatMap { rl ->
        val resource = rl.resource.toPairs()
        val service = resource.serviceName()
        rl.scope_logs.flatMap { sl ->
            val scope = sl.scope.describe()
            sl.log_records.map { log ->
                ReceivedLog(
                    severity = log.severity_text.ifEmpty {
                        log.severity_number.name.removePrefix("SEVERITY_NUMBER_")
                    },
                    body = log.body?.render() ?: "",
                    serviceName = service,
                    eventName = log.event_name,
                    timeNanos = log.time_unix_nano.takeIf { it != 0L } ?: log.observed_time_unix_nano,
                    traceId = log.trace_id.hexOrEmpty(),
                    spanId = log.span_id.hexOrEmpty(),
                    attributes = log.attributes.toPairs(),
                    resource = resource,
                    scope = scope,
                )
            }
        }
    }

fun formatNanos(nanos: Long): String =
    if (nanos == 0L) "-" else Instant.ofEpochSecond(0, nanos).toString()

private fun Resource?.toPairs(): List<Pair<String, String>> = this?.attributes?.toPairs().orEmpty()

private fun List<KeyValue>.toPairs(): List<Pair<String, String>> =
    map { it.key to (it.value_?.render() ?: "") }

private fun List<Pair<String, String>>.serviceName(): String =
    firstOrNull { it.first == "service.name" }?.second ?: "unknown_service"

private fun InstrumentationScope?.describe(): String = when {
    this == null -> ""
    version.isEmpty() -> name
    else -> "$name@$version"
}

private fun Status?.describe(): String = when {
    this == null -> "UNSET"
    message.isEmpty() -> code.name.removePrefix("STATUS_CODE_")
    else -> "${code.name.removePrefix("STATUS_CODE_")}: $message"
}

private fun ByteString.hexOrEmpty(): String = if (size == 0) "" else hex()

private fun AnyValue.render(): String = when {
    string_value != null -> string_value!!
    bool_value != null -> bool_value.toString()
    int_value != null -> int_value.toString()
    double_value != null -> double_value.toString()
    bytes_value != null -> "0x" + bytes_value!!.hex()
    array_value != null -> array_value!!.values.joinToString(prefix = "[", postfix = "]") { it.render() }
    kvlist_value != null -> kvlist_value!!.values.joinToString(prefix = "{", postfix = "}") {
        "${it.key}=${it.value_?.render() ?: ""}"
    }
    else -> ""
}
