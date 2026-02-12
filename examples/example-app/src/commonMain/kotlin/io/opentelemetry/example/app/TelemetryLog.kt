package io.opentelemetry.example.app

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.logging.model.SeverityNumber
import io.opentelemetry.kotlin.tracing.model.SpanKind

enum class AttributeType {
    STRING, LONG, DOUBLE, BOOLEAN
}

data class AttributeEntry(
    val key: String = "",
    val value: String = "",
    val type: AttributeType = AttributeType.STRING,
)

data class EventEntry(
    val name: String = "",
    val timestamp: String = "",
    val attributes: List<AttributeEntry> = emptyList(),
)

@OptIn(ExperimentalApi::class)
data class SpanFormState(
    val name: String = "example-span",
    val spanKind: String = SpanKind.INTERNAL.name,
    val startTimestamp: String = "",
    val attributes: List<AttributeEntry> = emptyList(),
    val events: List<EventEntry> = emptyList(),
    val setAsImplicitContext: Boolean = false,
)

@OptIn(ExperimentalApi::class)
data class LogFormState(
    val body: String = "Hello, world!",
    val severityNumber: String = SeverityNumber.INFO.name,
    val severityText: String = "",
    val timestamp: String = "",
    val observedTimestamp: String = "",
    val attributes: List<AttributeEntry> = emptyList(),
)
