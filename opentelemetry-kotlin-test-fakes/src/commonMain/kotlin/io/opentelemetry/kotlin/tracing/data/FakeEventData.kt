package io.opentelemetry.kotlin.tracing.data

import io.opentelemetry.kotlin.ExperimentalApi

@OptIn(ExperimentalApi::class)
class FakeEventData(
    override val name: String = "event",
    override val timestamp: Long = 1000,
    override val attributes: Map<String, Any> = mapOf("key" to "value")
) : EventData
