package io.opentelemetry.kotlin.framework.serialization

import kotlinx.serialization.Serializable

@Serializable
data class SerializableEventData(
    val name: String,
    val attributes: Map<String, String>,
    val timestamp: Long,
    val totalAttributesCount: Int,
)
