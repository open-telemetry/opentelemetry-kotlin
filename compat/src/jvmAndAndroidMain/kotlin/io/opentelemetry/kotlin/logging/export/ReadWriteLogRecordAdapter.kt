package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.InstrumentationScopeInfo
import io.opentelemetry.kotlin.aliases.OtelJavaAttributeKey
import io.opentelemetry.kotlin.aliases.OtelJavaReadWriteLogRecord
import io.opentelemetry.kotlin.attributes.convertToMap
import io.opentelemetry.kotlin.logging.model.ReadWriteLogRecord
import io.opentelemetry.kotlin.logging.model.SeverityNumber
import io.opentelemetry.kotlin.logging.toOtelKotlinSeverityNumber
import io.opentelemetry.kotlin.resource.Resource
import io.opentelemetry.kotlin.resource.ResourceAdapter
import io.opentelemetry.kotlin.scope.toOtelKotlinInstrumentationScopeInfo
import io.opentelemetry.kotlin.tracing.model.SpanContext
import io.opentelemetry.kotlin.tracing.model.SpanContextAdapter

@Suppress("UNUSED_PARAMETER")
@OptIn(ExperimentalApi::class)
internal class ReadWriteLogRecordAdapter(
    val impl: OtelJavaReadWriteLogRecord
) : ReadWriteLogRecord {

    override var timestamp: Long?
        get() = impl.timestampEpochNanos
        set(value) {
        }

    override var observedTimestamp: Long?
        get() = impl.observedTimestampEpochNanos
        set(value) {
        }

    override var severityNumber: SeverityNumber?
        get() = impl.severity.toOtelKotlinSeverityNumber()
        set(value) {}

    override var severityText: String?
        get() = impl.severityText
        set(value) {
        }

    override var body: String?
        get() = impl.bodyValue?.asString()
        set(value) {
        }

    override var eventName: String?
        get() = impl.eventName
        set(value) {
        }

    override fun setBooleanAttribute(key: String, value: Boolean) {
        impl.setAttribute(OtelJavaAttributeKey.booleanKey(key), value)
    }

    override fun setStringAttribute(key: String, value: String) {
        impl.setAttribute(OtelJavaAttributeKey.stringKey(key), value)
    }

    override fun setLongAttribute(key: String, value: Long) {
        impl.setAttribute(OtelJavaAttributeKey.longKey(key), value)
    }

    override fun setDoubleAttribute(key: String, value: Double) {
        impl.setAttribute(OtelJavaAttributeKey.doubleKey(key), value)
    }

    override fun setBooleanListAttribute(key: String, value: List<Boolean>) {
        impl.setAttribute(OtelJavaAttributeKey.booleanArrayKey(key), value)
    }

    override fun setStringListAttribute(key: String, value: List<String>) {
        impl.setAttribute(OtelJavaAttributeKey.stringArrayKey(key), value)
    }

    override fun setLongListAttribute(key: String, value: List<Long>) {
        impl.setAttribute(OtelJavaAttributeKey.longArrayKey(key), value)
    }

    override fun setDoubleListAttribute(key: String, value: List<Double>) {
        impl.setAttribute(OtelJavaAttributeKey.doubleArrayKey(key), value)
    }

    override val attributes: Map<String, Any>
        get() = impl.attributes.convertToMap()

    override val spanContext: SpanContext
        get() = SpanContextAdapter(impl.spanContext)

    override val resource: Resource
        get() = ResourceAdapter(impl.toLogRecordData().resource)

    override val instrumentationScopeInfo: InstrumentationScopeInfo
        get() = impl.instrumentationScopeInfo.toOtelKotlinInstrumentationScopeInfo()
}
