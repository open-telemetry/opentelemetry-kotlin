@file:Suppress("DEPRECATION", "TYPEALIAS_EXPANSION_DEPRECATION")

package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.aliases.OtelJavaLogRecordData
import io.opentelemetry.kotlin.aliases.OtelJavaSeverity
import io.opentelemetry.kotlin.aliases.OtelJavaValue
import io.opentelemetry.kotlin.aliases.OtelJavaValueType
import io.opentelemetry.kotlin.attributes.attrsFromMap
import io.opentelemetry.kotlin.attributes.resourceFromMap
import io.opentelemetry.kotlin.attributes.toOtelJavaValue
import io.opentelemetry.kotlin.logging.OtelJavaLogRecordDataImpl
import io.opentelemetry.kotlin.logging.data.LogRecordData
import io.opentelemetry.kotlin.logging.toOtelJavaSeverityNumber
import io.opentelemetry.kotlin.scope.toOtelJavaInstrumentationScopeInfo
import io.opentelemetry.kotlin.tracing.ext.toOtelJavaSpanContext

internal fun LogRecordData.toOtelJavaLogRecordData(): OtelJavaLogRecordData {
    return OtelJavaLogRecordDataImpl(
        timestampNanos = timestamp ?: 0,
        observedTimestampNanos = observedTimestamp ?: 0,
        spanContextImpl = spanContext.toOtelJavaSpanContext(),
        severityTextImpl = severityText,
        severityImpl = severityNumber?.toOtelJavaSeverityNumber()
            ?: OtelJavaSeverity.UNDEFINED_SEVERITY_NUMBER,
        bodyValueImpl = body.toOtelJavaBodyValue(),
        attributesImpl = attrsFromMap(attributes),
        totalAttributeCountImpl = attributes.size + droppedAttributesCount,
        eventNameImpl = eventName,
        resourceImpl = resourceFromMap(resource),
        scopeImpl = instrumentationScopeInfo.toOtelJavaInstrumentationScopeInfo()
    )
}

internal fun Any?.toOtelJavaBodyValue(): OtelJavaValue<*>? = try {
    this?.toOtelJavaValue()?.takeIf { it.type != OtelJavaValueType.EMPTY }
} catch (ignored: Throwable) {
    null
}
