package io.opentelemetry.kotlin.encode

import io.opentelemetry.kotlin.InstrumentationScopeInfo
import io.opentelemetry.kotlin.logging.model.ReadableLogRecord
import io.opentelemetry.kotlin.resource.Resource
import io.opentelemetry.kotlin.semconv.ServiceAttributes
import io.opentelemetry.kotlin.semconv.TelemetryAttributes
import io.opentelemetry.kotlin.tracing.SpanContext
import io.opentelemetry.kotlin.tracing.model.hex
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

internal class JsonLogRecordEncoder : OtlpJsonEncoder<ReadableLogRecord> {
    override fun encode(value: ReadableLogRecord): Sequence<String> =
        sequence {
            Json.encodeToString(value.toSerializable())
        }
}

@Serializable
data class SerializableLogRecordData(
    val resource: SerializableResource?,
    val instrumentationScopeInfo: SerializableInstrumentationScopeInfo?,
    val timestampEpochNanos: Long,
    val observedTimestampEpochNanos: Long,
    val spanContext: SerializableSpanContext,
    val severity: String,
    val severityText: String?,
    val body: String?,
    val attributes: Map<String, String>,
    val totalAttributeCount: Int,
    val eventName: String? = null,
)

@Serializable
data class SerializableResource(
    val schemaUrl: String,
    val attributes: Map<String, String>,
)

@Serializable
data class SerializableInstrumentationScopeInfo(
    val name: String,
    val version: String,
    val schemaUrl: String,
    val attributes: Map<String, String>,
)

@Serializable
data class SerializableSpanContext(
    val traceId: String,
    val spanId: String,
    val traceFlags: String,
    val traceState: Map<String, String>,
)

fun ReadableLogRecord.toSerializable() =
    SerializableLogRecordData(
        resource = resource.toSerializable(),
        instrumentationScopeInfo = instrumentationScopeInfo.toSerializable(),
        timestampEpochNanos = timestamp ?: 0,
        observedTimestampEpochNanos = observedTimestamp ?: 0,
        spanContext = spanContext.toSerializable(),
        severity = severityNumber?.name.orEmpty(),
        severityText = severityText,
        body = body?.toString(),
        eventName = eventName,
        attributes = attributes.toSerializable(),
        totalAttributeCount = attributes.size,
    )

fun Resource.toSerializable() =
    SerializableResource(
        schemaUrl = schemaUrl.toString(),
        attributes = attributes.mapValues {
            when (it.key) {
                ServiceAttributes.SERVICE_VERSION -> "UNKNOWN"
                TelemetryAttributes.TELEMETRY_SDK_VERSION -> "UNKNOWN"
                else -> it.value
            }
        }.toSerializable(),
    )

fun Map<String, Any>.toSerializable(): Map<String, String> = mapValues {
    it.value.toString()
}

fun SpanContext.toSerializable() =
    SerializableSpanContext(
        traceId = traceId,
        spanId = spanId,
        traceFlags = traceFlags.hex,
        traceState = traceState.asMap(),
    )

fun InstrumentationScopeInfo.toSerializable() =
    SerializableInstrumentationScopeInfo(
        name = name,
        version = version.toString(),
        schemaUrl = schemaUrl.toString(),
        attributes = attributes.toSerializable(),
    )
