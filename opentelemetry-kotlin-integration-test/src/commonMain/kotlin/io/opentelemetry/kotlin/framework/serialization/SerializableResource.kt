package io.opentelemetry.kotlin.framework.serialization

import kotlinx.serialization.Serializable

@Serializable
data class SerializableResource(
    val schemaUrl: String,
    val attributes: Map<String, String>,
)
