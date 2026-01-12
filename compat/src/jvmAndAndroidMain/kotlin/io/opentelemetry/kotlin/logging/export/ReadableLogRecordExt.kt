@file:Suppress("DEPRECATION", "TYPEALIAS_EXPANSION_DEPRECATION")

package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaBody
import io.opentelemetry.kotlin.aliases.OtelJavaLogRecordData
import io.opentelemetry.kotlin.aliases.OtelJavaSeverity
import io.opentelemetry.kotlin.attributes.attrsFromMap
import io.opentelemetry.kotlin.attributes.resourceFromMap
import io.opentelemetry.kotlin.logging.OtelJavaLogRecordDataImpl
import io.opentelemetry.kotlin.logging.model.ReadableLogRecord
import io.opentelemetry.kotlin.logging.toOtelJavaSeverityNumber
import io.opentelemetry.kotlin.scope.toOtelJavaInstrumentationScopeInfo
import io.opentelemetry.kotlin.tracing.ext.toOtelJavaSpanContext

@OptIn(ExperimentalApi::class)
internal fun ReadableLogRecord.toLogRecordData(): OtelJavaLogRecordData {
    return OtelJavaLogRecordDataImpl(
        timestampNanos = timestamp ?: 0,
        observedTimestampNanos = observedTimestamp ?: 0,
        spanContextImpl = spanContext.toOtelJavaSpanContext(),
        severityTextImpl = severityText,
        severityImpl = severityNumber?.toOtelJavaSeverityNumber()
            ?: OtelJavaSeverity.UNDEFINED_SEVERITY_NUMBER,
        bodyImpl = body?.let(OtelJavaBody::string) ?: OtelJavaBody.empty(),
        attributesImpl = attrsFromMap(attributes),
        resourceImpl = resourceFromMap(resource),
        scopeImpl = instrumentationScopeInfo.toOtelJavaInstrumentationScopeInfo()
    )
}
