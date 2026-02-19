package io.opentelemetry.kotlin.integration.test.model

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.InstrumentationScopeInfo
import io.opentelemetry.kotlin.framework.serialization.SerializableEventData
import io.opentelemetry.kotlin.framework.serialization.SerializableInstrumentationScopeInfo
import io.opentelemetry.kotlin.framework.serialization.SerializableLinkData
import io.opentelemetry.kotlin.framework.serialization.SerializableResource
import io.opentelemetry.kotlin.framework.serialization.SerializableSpanContext
import io.opentelemetry.kotlin.framework.serialization.SerializableSpanData
import io.opentelemetry.kotlin.framework.serialization.SerializableSpanStatusData
import io.opentelemetry.kotlin.resource.Resource
import io.opentelemetry.kotlin.tracing.data.EventData
import io.opentelemetry.kotlin.tracing.data.LinkData
import io.opentelemetry.kotlin.tracing.data.StatusData
import io.opentelemetry.kotlin.tracing.model.ReadableSpan
import io.opentelemetry.kotlin.tracing.model.SpanContext
import io.opentelemetry.kotlin.tracing.model.hex

@OptIn(ExperimentalApi::class)
internal fun ReadableSpan.toSerializable(): SerializableSpanData =
    SerializableSpanData(
        name = name,
        kind = spanKind.name,
        statusData = status.toSerializable(),
        spanContext = spanContext.toSerializable(),
        parentSpanContext = spanContext.toSerializable(),
        startTimestamp = startTimestamp,
        attributes = attributes.toSerializable(),
        events = events.map(EventData::toSerializable),
        links = links.map(LinkData::toSerializable),
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

@OptIn(ExperimentalApi::class)
private fun StatusData.toSerializable(): SerializableSpanStatusData =
    SerializableSpanStatusData(
        statusCode.name,
        description.toString()
    )

@OptIn(ExperimentalApi::class)
private fun Resource.toSerializable(): SerializableResource =
    SerializableResource(
        schemaUrl.toString(),
        attributes.toSerializable()
    )

@OptIn(ExperimentalApi::class)
private fun InstrumentationScopeInfo.toSerializable(): SerializableInstrumentationScopeInfo =
    SerializableInstrumentationScopeInfo(
        name,
        version.toString(),
        schemaUrl.toString(),
        attributes.toSerializable()
    )

@OptIn(ExperimentalApi::class)
private fun EventData.toSerializable(): SerializableEventData =
    SerializableEventData(
        name,
        attributes.toSerializable(),
        timestamp,
        attributes.size
    )

@OptIn(ExperimentalApi::class)
private fun LinkData.toSerializable(): SerializableLinkData =
    SerializableLinkData(
        spanContext.toSerializable(),
        attributes.toSerializable(),
        attributes.size,
    )

@OptIn(ExperimentalApi::class)
private fun SpanContext.toSerializable(): SerializableSpanContext =
    SerializableSpanContext(
        traceId,
        spanId,
        traceFlags.hex,
        traceState.asMap()
    )
