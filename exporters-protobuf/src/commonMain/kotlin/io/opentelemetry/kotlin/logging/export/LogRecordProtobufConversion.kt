package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.InstrumentationScopeInfo
import io.opentelemetry.kotlin.export.conversion.DeserializedSpanContext
import io.opentelemetry.kotlin.export.conversion.createKeyValues
import io.opentelemetry.kotlin.export.conversion.toAttributeMap
import io.opentelemetry.kotlin.export.conversion.toFlagsInt
import io.opentelemetry.kotlin.logging.model.ReadableLogRecord
import io.opentelemetry.kotlin.logging.model.SeverityNumber
import io.opentelemetry.kotlin.resource.Resource
import io.opentelemetry.kotlin.tracing.model.SpanContext
import io.opentelemetry.proto.common.v1.AnyValue
import io.opentelemetry.proto.logs.v1.LogRecord
import io.opentelemetry.proto.logs.v1.SeverityNumber.SEVERITY_NUMBER_DEBUG
import io.opentelemetry.proto.logs.v1.SeverityNumber.SEVERITY_NUMBER_DEBUG2
import io.opentelemetry.proto.logs.v1.SeverityNumber.SEVERITY_NUMBER_DEBUG3
import io.opentelemetry.proto.logs.v1.SeverityNumber.SEVERITY_NUMBER_DEBUG4
import io.opentelemetry.proto.logs.v1.SeverityNumber.SEVERITY_NUMBER_ERROR
import io.opentelemetry.proto.logs.v1.SeverityNumber.SEVERITY_NUMBER_ERROR2
import io.opentelemetry.proto.logs.v1.SeverityNumber.SEVERITY_NUMBER_ERROR3
import io.opentelemetry.proto.logs.v1.SeverityNumber.SEVERITY_NUMBER_ERROR4
import io.opentelemetry.proto.logs.v1.SeverityNumber.SEVERITY_NUMBER_FATAL
import io.opentelemetry.proto.logs.v1.SeverityNumber.SEVERITY_NUMBER_FATAL2
import io.opentelemetry.proto.logs.v1.SeverityNumber.SEVERITY_NUMBER_FATAL3
import io.opentelemetry.proto.logs.v1.SeverityNumber.SEVERITY_NUMBER_FATAL4
import io.opentelemetry.proto.logs.v1.SeverityNumber.SEVERITY_NUMBER_INFO
import io.opentelemetry.proto.logs.v1.SeverityNumber.SEVERITY_NUMBER_INFO2
import io.opentelemetry.proto.logs.v1.SeverityNumber.SEVERITY_NUMBER_INFO3
import io.opentelemetry.proto.logs.v1.SeverityNumber.SEVERITY_NUMBER_INFO4
import io.opentelemetry.proto.logs.v1.SeverityNumber.SEVERITY_NUMBER_TRACE
import io.opentelemetry.proto.logs.v1.SeverityNumber.SEVERITY_NUMBER_TRACE2
import io.opentelemetry.proto.logs.v1.SeverityNumber.SEVERITY_NUMBER_TRACE3
import io.opentelemetry.proto.logs.v1.SeverityNumber.SEVERITY_NUMBER_TRACE4
import io.opentelemetry.proto.logs.v1.SeverityNumber.SEVERITY_NUMBER_UNSPECIFIED
import io.opentelemetry.proto.logs.v1.SeverityNumber.SEVERITY_NUMBER_WARN
import io.opentelemetry.proto.logs.v1.SeverityNumber.SEVERITY_NUMBER_WARN2
import io.opentelemetry.proto.logs.v1.SeverityNumber.SEVERITY_NUMBER_WARN3
import io.opentelemetry.proto.logs.v1.SeverityNumber.SEVERITY_NUMBER_WARN4
import okio.ByteString.Companion.toByteString


@OptIn(ExperimentalApi::class)
internal fun ReadableLogRecord.toProtobuf(): LogRecord = LogRecord(
    trace_id = spanContext.traceIdBytes.toByteString(),
    span_id = spanContext.spanIdBytes.toByteString(),
    flags = spanContext.traceFlags.toFlagsInt(),
    time_unix_nano = timestamp ?: 0L,
    observed_time_unix_nano = observedTimestamp ?: 0L,
    severity_number = severityNumber?.convertSeverityNumber() ?: SEVERITY_NUMBER_UNSPECIFIED,
    severity_text = severityText ?: "",
    body = body?.let { AnyValue(string_value = it) },
    attributes = attributes.createKeyValues(),
    event_name = eventName ?: "",
)

@OptIn(ExperimentalApi::class)
internal fun LogRecord.toReadableLogRecord(
    resource: Resource,
    instrumentationScopeInfo: InstrumentationScopeInfo
): ReadableLogRecord = DeserializedReadableLogRecord(
    timestamp = time_unix_nano,
    observedTimestamp = observed_time_unix_nano,
    severityNumber = severity_number.toSeverityNumber(),
    severityText = severity_text.ifEmpty { null },
    body = body?.string_value,
    eventName = event_name.ifEmpty { null },
    attributes = attributes.toAttributeMap(),
    spanContext = DeserializedSpanContext(
        traceIdBytes = trace_id.toByteArray(),
        spanIdBytes = span_id.toByteArray(),
        flags = flags,
    ),
    resource = resource,
    instrumentationScopeInfo = instrumentationScopeInfo
)

@OptIn(ExperimentalApi::class)
private fun SeverityNumber.convertSeverityNumber(): io.opentelemetry.proto.logs.v1.SeverityNumber =
    when (this) {
        SeverityNumber.UNKNOWN -> SEVERITY_NUMBER_UNSPECIFIED
        SeverityNumber.TRACE -> SEVERITY_NUMBER_TRACE
        SeverityNumber.TRACE2 -> SEVERITY_NUMBER_TRACE2
        SeverityNumber.TRACE3 -> SEVERITY_NUMBER_TRACE3
        SeverityNumber.TRACE4 -> SEVERITY_NUMBER_TRACE4
        SeverityNumber.DEBUG -> SEVERITY_NUMBER_DEBUG
        SeverityNumber.DEBUG2 -> SEVERITY_NUMBER_DEBUG2
        SeverityNumber.DEBUG3 -> SEVERITY_NUMBER_DEBUG3
        SeverityNumber.DEBUG4 -> SEVERITY_NUMBER_DEBUG4
        SeverityNumber.INFO -> SEVERITY_NUMBER_INFO
        SeverityNumber.INFO2 -> SEVERITY_NUMBER_INFO2
        SeverityNumber.INFO3 -> SEVERITY_NUMBER_INFO3
        SeverityNumber.INFO4 -> SEVERITY_NUMBER_INFO4
        SeverityNumber.WARN -> SEVERITY_NUMBER_WARN
        SeverityNumber.WARN2 -> SEVERITY_NUMBER_WARN2
        SeverityNumber.WARN3 -> SEVERITY_NUMBER_WARN3
        SeverityNumber.WARN4 -> SEVERITY_NUMBER_WARN4
        SeverityNumber.ERROR -> SEVERITY_NUMBER_ERROR
        SeverityNumber.ERROR2 -> SEVERITY_NUMBER_ERROR2
        SeverityNumber.ERROR3 -> SEVERITY_NUMBER_ERROR3
        SeverityNumber.ERROR4 -> SEVERITY_NUMBER_ERROR4
        SeverityNumber.FATAL -> SEVERITY_NUMBER_FATAL
        SeverityNumber.FATAL2 -> SEVERITY_NUMBER_FATAL2
        SeverityNumber.FATAL3 -> SEVERITY_NUMBER_FATAL3
        SeverityNumber.FATAL4 -> SEVERITY_NUMBER_FATAL4
    }

@OptIn(ExperimentalApi::class)
private fun io.opentelemetry.proto.logs.v1.SeverityNumber.toSeverityNumber(): SeverityNumber? =
    when (this) {
        SEVERITY_NUMBER_UNSPECIFIED -> null
        SEVERITY_NUMBER_TRACE -> SeverityNumber.TRACE
        SEVERITY_NUMBER_TRACE2 -> SeverityNumber.TRACE2
        SEVERITY_NUMBER_TRACE3 -> SeverityNumber.TRACE3
        SEVERITY_NUMBER_TRACE4 -> SeverityNumber.TRACE4
        SEVERITY_NUMBER_DEBUG -> SeverityNumber.DEBUG
        SEVERITY_NUMBER_DEBUG2 -> SeverityNumber.DEBUG2
        SEVERITY_NUMBER_DEBUG3 -> SeverityNumber.DEBUG3
        SEVERITY_NUMBER_DEBUG4 -> SeverityNumber.DEBUG4
        SEVERITY_NUMBER_INFO -> SeverityNumber.INFO
        SEVERITY_NUMBER_INFO2 -> SeverityNumber.INFO2
        SEVERITY_NUMBER_INFO3 -> SeverityNumber.INFO3
        SEVERITY_NUMBER_INFO4 -> SeverityNumber.INFO4
        SEVERITY_NUMBER_WARN -> SeverityNumber.WARN
        SEVERITY_NUMBER_WARN2 -> SeverityNumber.WARN2
        SEVERITY_NUMBER_WARN3 -> SeverityNumber.WARN3
        SEVERITY_NUMBER_WARN4 -> SeverityNumber.WARN4
        SEVERITY_NUMBER_ERROR -> SeverityNumber.ERROR
        SEVERITY_NUMBER_ERROR2 -> SeverityNumber.ERROR2
        SEVERITY_NUMBER_ERROR3 -> SeverityNumber.ERROR3
        SEVERITY_NUMBER_ERROR4 -> SeverityNumber.ERROR4
        SEVERITY_NUMBER_FATAL -> SeverityNumber.FATAL
        SEVERITY_NUMBER_FATAL2 -> SeverityNumber.FATAL2
        SEVERITY_NUMBER_FATAL3 -> SeverityNumber.FATAL3
        SEVERITY_NUMBER_FATAL4 -> SeverityNumber.FATAL4
    }

@OptIn(ExperimentalApi::class)
private class DeserializedReadableLogRecord(
    override val timestamp: Long?,
    override val observedTimestamp: Long?,
    override val severityNumber: SeverityNumber?,
    override val severityText: String?,
    override val body: String?,
    override val eventName: String?,
    override val attributes: Map<String, Any>,
    override val spanContext: SpanContext,
    override val resource: Resource,
    override val instrumentationScopeInfo: InstrumentationScopeInfo
) : ReadableLogRecord
