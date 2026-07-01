package io.opentelemetry.kotlin.encode

import io.opentelemetry.kotlin.tracing.StatusData
import io.opentelemetry.kotlin.tracing.data.SpanData
import io.opentelemetry.kotlin.tracing.data.SpanEventData
import io.opentelemetry.kotlin.tracing.data.SpanLinkData
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

internal class JsonSpanEncoder : OtlpJsonEncoder<SpanData> {
    override fun encode(value: SpanData): String =
        Json.encodeToString(value.toSerializable())
}

fun SpanData.toSerializable() =
    SerializableSpanData(
        name = name,
        kind = spanKind.name,
        statusData = status.toSerializable(),
        spanContext = spanContext.toSerializable(),
        parentSpanContext = parent.toSerializable(),
        startTimestamp = startTimestamp,
        attributes = attributes.toSerializable(),
        events = events.map { it.toSerializable() },
        links = links.map { it.toSerializable() },
        endTimestamp = endTimestamp ?: 0,
        ended = hasEnded,
        totalRecordedEvents = events.size,
        totalRecordedLinks = links.size,
        totalAttributeCount = attributes.size,
        resource = resource.toSerializable(),
        instrumentationScopeInfo = instrumentationScopeInfo.toSerializable()
    )

@Serializable
data class SerializableSpanData(
    val name: String,
    val kind: String,
    val statusData: SerializableSpanStatusData,
    val spanContext: SerializableSpanContext,
    val parentSpanContext: SerializableSpanContext,
    val startTimestamp: Long,
    val attributes: Map<String, String>,
    val events: List<SerializableEventData>,
    val links: List<SerializableLinkData>,
    val endTimestamp: Long,
    val ended: Boolean,
    val totalRecordedEvents: Int,
    val totalRecordedLinks: Int,
    val totalAttributeCount: Int,
    val resource: SerializableResource,
    val instrumentationScopeInfo: SerializableInstrumentationScopeInfo
)

@Serializable
data class SerializableSpanStatusData(
    val name: String,
    val description: String,
)

fun StatusData.toSerializable() =
    SerializableSpanStatusData(
        name = statusCode.name,
        description = description.orEmpty(),
    )

@Serializable
data class SerializableEventData(
    val name: String,
    val attributes: Map<String, String>,
    val timestamp: Long,
    val totalAttributesCount: Int,
)

@Serializable
data class SerializableLinkData(
    val spanContext: SerializableSpanContext,
    val attributes: Map<String, String>,
    val totalAttributeCount: Int,
)

fun SpanEventData.toSerializable() =
    SerializableEventData(
        name = name,
        attributes = attributes.toSerializable(),
        timestamp = timestamp,
        totalAttributesCount = attributes.size,
    )

fun SpanLinkData.toSerializable() =
    SerializableLinkData(
        spanContext = spanContext.toSerializable(),
        attributes = attributes.toSerializable(),
        totalAttributeCount = attributes.size,
    )