package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.InstrumentationScopeInfo
import io.opentelemetry.kotlin.aliases.OtelJavaReadWriteLogRecord
import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.attributes.OtelJavaAttributesMutator
import io.opentelemetry.kotlin.attributes.attributeSink
import io.opentelemetry.kotlin.attributes.convertToMap
import io.opentelemetry.kotlin.attributes.toOtelKotlinBody
import io.opentelemetry.kotlin.droppedCount
import io.opentelemetry.kotlin.logging.SeverityNumber
import io.opentelemetry.kotlin.logging.data.LogRecordData
import io.opentelemetry.kotlin.logging.data.LogRecordDataAdapter
import io.opentelemetry.kotlin.logging.model.ReadWriteLogRecord
import io.opentelemetry.kotlin.logging.toOtelKotlinSeverityNumber
import io.opentelemetry.kotlin.resource.Resource
import io.opentelemetry.kotlin.resource.ResourceAdapter
import io.opentelemetry.kotlin.scope.toOtelKotlinInstrumentationScopeInfo
import io.opentelemetry.kotlin.tracing.SpanContext
import io.opentelemetry.kotlin.tracing.toOtelKotlinSpanContext

@Suppress("UNUSED_PARAMETER")
internal class ReadWriteLogRecordAdapter(
    val impl: OtelJavaReadWriteLogRecord,
    private val unsupportedWrites: UnsupportedLogRecordWriteReporter,
) : ReadWriteLogRecord,
    AttributesMutator by OtelJavaAttributesMutator(impl.attributeSink()) {

    override var timestamp: Long?
        get() = impl.timestampEpochNanos.takeIf { it > 0L }
        set(value) {
            unsupportedWrites.report("timestamp")
        }

    override var observedTimestamp: Long?
        get() = impl.observedTimestampEpochNanos
        set(value) {
            unsupportedWrites.report("observedTimestamp")
        }

    override var severityNumber: SeverityNumber?
        get() = impl.severity.toOtelKotlinSeverityNumber()
        set(value) {
            unsupportedWrites.report("severityNumber")
        }

    override var severityText: String?
        get() = impl.severityText
        set(value) {
            unsupportedWrites.report("severityText")
        }

    override var body: Any?
        get() = impl.bodyValue?.toOtelKotlinBody()
        set(value) {
            unsupportedWrites.report("body")
        }

    override var spanContext: SpanContext
        get() = impl.spanContext.toOtelKotlinSpanContext()
        set(value) {
            unsupportedWrites.report("spanContext")
        }

    override var eventName: String?
        get() = impl.eventName
        set(value) {
            unsupportedWrites.report("eventName")
        }

    override val attributes: Map<String, Any>
        get() = impl.attributes.convertToMap()

    override val droppedAttributesCount: Int
        get() = droppedCount(impl.toLogRecordData().totalAttributeCount, impl.attributes.size())

    override val resource: Resource
        get() = ResourceAdapter(impl.toLogRecordData().resource)

    override val instrumentationScopeInfo: InstrumentationScopeInfo
        get() = impl.instrumentationScopeInfo.toOtelKotlinInstrumentationScopeInfo()

    override fun toLogRecordData(): LogRecordData = LogRecordDataAdapter(impl.toLogRecordData())
}
