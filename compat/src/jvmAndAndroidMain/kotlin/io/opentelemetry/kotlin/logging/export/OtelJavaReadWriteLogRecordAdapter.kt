package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.aliases.OtelJavaAttributeKey
import io.opentelemetry.kotlin.aliases.OtelJavaAttributes
import io.opentelemetry.kotlin.aliases.OtelJavaInstrumentationScopeInfo
import io.opentelemetry.kotlin.aliases.OtelJavaLogRecordData
import io.opentelemetry.kotlin.aliases.OtelJavaReadWriteLogRecord
import io.opentelemetry.kotlin.aliases.OtelJavaSeverity
import io.opentelemetry.kotlin.aliases.OtelJavaSpanContext
import io.opentelemetry.kotlin.aliases.OtelJavaValue
import io.opentelemetry.kotlin.attributes.attrsFromMap
import io.opentelemetry.kotlin.attributes.setTypedAttribute
import io.opentelemetry.kotlin.logging.model.ReadWriteLogRecord
import io.opentelemetry.kotlin.logging.toOtelJavaSeverityNumber
import io.opentelemetry.kotlin.scope.toOtelJavaInstrumentationScopeInfo
import io.opentelemetry.kotlin.tracing.ext.toOtelJavaSpanContext

internal class OtelJavaReadWriteLogRecordAdapter(
    private val log: ReadWriteLogRecord
) : OtelJavaReadWriteLogRecord {

    override fun <T> setAttribute(key: OtelJavaAttributeKey<T>, value: T?): OtelJavaReadWriteLogRecord {
        value?.let { log.setTypedAttribute(key.key, it) }
        return this
    }

    override fun toLogRecordData(): OtelJavaLogRecordData = log.toLogRecordData().toOtelJavaLogRecordData()

    override fun getBodyValue(): OtelJavaValue<*>? = log.body.toOtelJavaBodyValue()

    override fun getInstrumentationScopeInfo(): OtelJavaInstrumentationScopeInfo =
        log.instrumentationScopeInfo.toOtelJavaInstrumentationScopeInfo()

    override fun getTimestampEpochNanos(): Long = log.timestamp ?: 0

    override fun getObservedTimestampEpochNanos(): Long = log.observedTimestamp ?: 0

    override fun getSpanContext(): OtelJavaSpanContext = log.spanContext.toOtelJavaSpanContext()

    override fun getSeverity(): OtelJavaSeverity =
        log.severityNumber?.toOtelJavaSeverityNumber() ?: OtelJavaSeverity.UNDEFINED_SEVERITY_NUMBER

    override fun getSeverityText(): String? = log.severityText

    override fun getEventName(): String? = log.eventName

    override fun <T> getAttribute(key: OtelJavaAttributeKey<T>): T? = attributes.get(key)

    override fun getAttributes(): OtelJavaAttributes = attrsFromMap(log.attributes)
}
