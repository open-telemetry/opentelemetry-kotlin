package io.opentelemetry.kotlin.framework.serialization

import kotlinx.serialization.Serializable

@Serializable
data class SerializableSpanStatusData(
    val name: String,
    val description: String,
)
