package io.opentelemetry.kotlin.framework.serialization.conversion

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.framework.serialization.SerializableLogRecordData
import io.opentelemetry.kotlin.logging.model.ReadableLogRecord

@OptIn(ExperimentalApi::class)
fun ReadableLogRecord.toSerializable() =
    SerializableLogRecordData(
        resource = resource.toSerializable(),
        instrumentationScopeInfo = instrumentationScopeInfo.toSerializable(),
        timestampEpochNanos = timestamp ?: 0,
        observedTimestampEpochNanos = observedTimestamp ?: 0,
        spanContext = spanContext.toSerializable(),
        severity = severityNumber?.name.orEmpty(),
        severityText = severityText,
        body = body,
        eventName = eventName,
        attributes = attributes.toSerializable(),
        totalAttributeCount = attributes.size,
    )
