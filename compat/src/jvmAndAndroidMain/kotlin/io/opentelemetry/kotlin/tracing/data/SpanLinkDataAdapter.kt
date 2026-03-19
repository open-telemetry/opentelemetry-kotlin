package io.opentelemetry.kotlin.tracing.data

import io.opentelemetry.kotlin.aliases.OtelJavaLinkData
import io.opentelemetry.kotlin.attributes.convertToMap
import io.opentelemetry.kotlin.tracing.SpanContext
import io.opentelemetry.kotlin.tracing.model.SpanContextAdapter

internal class SpanLinkDataAdapter(
    impl: OtelJavaLinkData,
) : SpanLinkData {
    override val spanContext: SpanContext = SpanContextAdapter(impl.spanContext)
    override val attributes: Map<String, Any> = impl.attributes.convertToMap()
}
