package io.opentelemetry.kotlin.integration.test.model

import io.opentelemetry.kotlin.InstrumentationScopeInfo
import io.opentelemetry.kotlin.framework.serialization.SerializableEventData
import io.opentelemetry.kotlin.framework.serialization.SerializableInstrumentationScopeInfo
import io.opentelemetry.kotlin.framework.serialization.SerializableLinkData
import io.opentelemetry.kotlin.framework.serialization.SerializableResource
import io.opentelemetry.kotlin.framework.serialization.SerializableSpanContext
import io.opentelemetry.kotlin.framework.serialization.SerializableSpanData
import io.opentelemetry.kotlin.framework.serialization.SerializableSpanStatusData
import io.opentelemetry.kotlin.resource.Resource
import io.opentelemetry.kotlin.tracing.data.SpanEventData
import io.opentelemetry.kotlin.tracing.data.SpanLinkData
import io.opentelemetry.kotlin.tracing.data.StatusData
import io.opentelemetry.kotlin.tracing.model.ReadableSpan
import io.opentelemetry.kotlin.tracing.model.SpanContext
import io.opentelemetry.kotlin.tracing.model.hex

internal fun ReadableSpan.toSerializable(): SerializableSpanData =
    SerializableSpanData(
        name = name,
        kind = spanKind.name,
        statusData = status.toSerializable(),
        spanContext = spanContext.toSerializable(),
        parentSpanContext = spanContext.toSerializable(),
        startTimestamp = startTimestamp,
        attributes = attributes.toSerializable(),
        events = events.map(SpanEventData::toSerializable),
        links = links.map(SpanLinkData::toSerializable),
        endTimestamp = endTimestamp ?: -1,
        ended = hasEnded,
        totalRecordedEvents = events.size,
        totalRecordedLinks = links.size,
        totalAttributeCount = attributes.size,
        resource = resource.toSerializable(),
        instrumentationScopeInfo = instrumentationScopeInfo.toSerializable(),
    )

private fun Map<String, Any>.toSerializable(): Map<String, String> =
    mapValues { it.value.toString() }

private fun StatusData.toSerializable(): SerializableSpanStatusData =
    SerializableSpanStatusData(
        statusCode.name,
        description.toString()
    )

private fun Resource.toSerializable(): SerializableResource =
    SerializableResource(
        schemaUrl.toString(),
        attributes.toSerializable()
    )

private fun InstrumentationScopeInfo.toSerializable(): SerializableInstrumentationScopeInfo =
    SerializableInstrumentationScopeInfo(
        name,
        version.toString(),
        schemaUrl.toString(),
        attributes.toSerializable()
    )

private fun SpanEventData.toSerializable(): SerializableEventData =
    SerializableEventData(
        name,
        attributes.toSerializable(),
        timestamp,
        attributes.size
    )

private fun SpanLinkData.toSerializable(): SerializableLinkData =
    SerializableLinkData(
        spanContext.toSerializable(),
        attributes.toSerializable(),
        attributes.size,
    )

private fun SpanContext.toSerializable(): SerializableSpanContext =
    SerializableSpanContext(
        traceId,
        spanId,
        traceFlags.hex,
        traceState.asMap()
    )
