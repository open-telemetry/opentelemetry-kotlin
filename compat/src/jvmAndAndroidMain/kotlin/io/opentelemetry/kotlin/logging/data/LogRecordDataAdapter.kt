package io.opentelemetry.kotlin.logging.data

import io.opentelemetry.kotlin.InstrumentationScopeInfo
import io.opentelemetry.kotlin.aliases.OtelJavaLogRecordData
import io.opentelemetry.kotlin.attributes.convertToMap
import io.opentelemetry.kotlin.attributes.toOtelKotlinBody
import io.opentelemetry.kotlin.logging.SeverityNumber
import io.opentelemetry.kotlin.logging.toOtelKotlinSeverityNumber
import io.opentelemetry.kotlin.resource.Resource
import io.opentelemetry.kotlin.resource.ResourceAdapter
import io.opentelemetry.kotlin.scope.toOtelKotlinInstrumentationScopeInfo
import io.opentelemetry.kotlin.tracing.SpanContext
import io.opentelemetry.kotlin.tracing.ext.toOtelKotlinSpanContext

internal class LogRecordDataAdapter(
    val impl: OtelJavaLogRecordData,
) : LogRecordData {
    override val timestamp: Long? = impl.timestampEpochNanos.takeIf { it > 0L }
    override val observedTimestamp: Long? = impl.observedTimestampEpochNanos
    override val severityNumber: SeverityNumber? = impl.severity.toOtelKotlinSeverityNumber()
    override val severityText: String? = impl.severityText
    override val body: Any? = impl.bodyValue?.toOtelKotlinBody()
    override val eventName: String? = impl.eventName
    override val spanContext: SpanContext = impl.spanContext.toOtelKotlinSpanContext()
    override val attributes: Map<String, Any> = impl.attributes.convertToMap()
    override val resource: Resource = ResourceAdapter(impl.resource)
    override val instrumentationScopeInfo: InstrumentationScopeInfo =
        impl.instrumentationScopeInfo.toOtelKotlinInstrumentationScopeInfo()
    override val droppedAttributesCount: Int = (impl.totalAttributeCount - impl.attributes.size()).coerceAtLeast(0)
}
