package io.opentelemetry.kotlin.framework.serialization

import kotlinx.serialization.Serializable

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
