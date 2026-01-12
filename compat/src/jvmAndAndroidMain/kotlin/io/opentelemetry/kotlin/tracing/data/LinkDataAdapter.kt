package io.opentelemetry.kotlin.tracing.data

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaLinkData
import io.opentelemetry.kotlin.attributes.convertToMap
import io.opentelemetry.kotlin.tracing.model.SpanContext
import io.opentelemetry.kotlin.tracing.model.SpanContextAdapter

@OptIn(ExperimentalApi::class)
internal class LinkDataAdapter(
    impl: OtelJavaLinkData,
) : LinkData {
    override val spanContext: SpanContext = SpanContextAdapter(impl.spanContext)
    override val attributes: Map<String, Any> = impl.attributes.convertToMap()
}
