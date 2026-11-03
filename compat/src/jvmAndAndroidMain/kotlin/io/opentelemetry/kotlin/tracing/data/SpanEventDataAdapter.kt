package io.opentelemetry.kotlin.tracing.data

import io.opentelemetry.kotlin.aliases.OtelJavaEventData
import io.opentelemetry.kotlin.attributes.convertToMap
import io.opentelemetry.kotlin.droppedCount

internal class SpanEventDataAdapter(
    impl: OtelJavaEventData,
) : SpanEventData {
    override val name: String = impl.name
    override val timestamp: Long = impl.epochNanos
    override val attributes: Map<String, Any> = impl.attributes.convertToMap()
    override val droppedAttributesCount: Int = droppedCount(impl.totalAttributeCount, impl.attributes.size())
}
