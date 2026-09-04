package io.opentelemetry.kotlin.framework.serialization

import kotlinx.serialization.Serializable

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
