package io.opentelemetry.kotlin.framework.serialization

import kotlinx.serialization.Serializable

@Serializable
data class SerializableLinkData(
    val spanContext: SerializableSpanContext,
    val attributes: Map<String, String>,
    val totalAttributeCount: Int,
)
