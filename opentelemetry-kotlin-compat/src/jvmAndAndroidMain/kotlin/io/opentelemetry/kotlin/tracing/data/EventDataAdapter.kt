package io.opentelemetry.kotlin.tracing.data

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaEventData
import io.opentelemetry.kotlin.attributes.convertToMap

@OptIn(ExperimentalApi::class)
internal class EventDataAdapter(
    impl: OtelJavaEventData,
) : EventData {
    override val name: String = impl.name
    override val timestamp: Long = impl.epochNanos
    override val attributes: Map<String, Any> = impl.attributes.convertToMap()
}
