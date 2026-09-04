package io.opentelemetry.kotlin.framework.serialization

import kotlinx.serialization.Serializable

@Serializable
data class SerializableSpanContext(
    val traceId: String,
    val spanId: String,
    val traceFlags: String,
    val traceState: Map<String, String>,
)
