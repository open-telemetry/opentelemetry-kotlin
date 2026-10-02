@file:Suppress("DEPRECATION", "TYPEALIAS_EXPANSION_DEPRECATION")

package io.opentelemetry.kotlin.tracing.model

import io.opentelemetry.kotlin.ClockProvider
import io.opentelemetry.kotlin.aliases.OtelJavaAttributeKey
import io.opentelemetry.kotlin.aliases.OtelJavaAttributes
import io.opentelemetry.kotlin.aliases.OtelJavaClock
import io.opentelemetry.kotlin.aliases.OtelJavaInstrumentationLibraryInfo
import io.opentelemetry.kotlin.aliases.OtelJavaInstrumentationScopeInfo
import io.opentelemetry.kotlin.aliases.OtelJavaReadableSpan
import io.opentelemetry.kotlin.aliases.OtelJavaSpanContext
import io.opentelemetry.kotlin.aliases.OtelJavaSpanData
import io.opentelemetry.kotlin.aliases.OtelJavaSpanKind
import io.opentelemetry.kotlin.attributes.attrsFromMap
import io.opentelemetry.kotlin.scope.toOtelJavaInstrumentationScopeInfo
import io.opentelemetry.kotlin.tracing.ext.toOtelJavaSpanContext
import io.opentelemetry.kotlin.tracing.ext.toOtelJavaSpanData
import io.opentelemetry.kotlin.tracing.ext.toOtelJavaSpanKind

internal class OtelJavaReadableSpanAdapter(
    private val span: ReadableSpan
) : OtelJavaReadableSpan {

    override fun getSpanContext(): OtelJavaSpanContext = span.spanContext.toOtelJavaSpanContext()

    override fun getParentSpanContext(): OtelJavaSpanContext = span.parent.toOtelJavaSpanContext()

    override fun getName(): String = span.name

    override fun toSpanData(): OtelJavaSpanData = span.toSpanData().toOtelJavaSpanData()

    @Deprecated("Deprecated in Java")
    override fun getInstrumentationLibraryInfo(): OtelJavaInstrumentationLibraryInfo {
        val scope = span.instrumentationScopeInfo
        return OtelJavaInstrumentationLibraryInfo.create(scope.name, scope.version, scope.schemaUrl)
    }

    override fun getInstrumentationScopeInfo(): OtelJavaInstrumentationScopeInfo =
        span.instrumentationScopeInfo.toOtelJavaInstrumentationScopeInfo()

    override fun hasEnded(): Boolean = span.hasEnded

    override fun getLatencyNanos(): Long {
        val end = span.endTimestamp
            ?: (span as? ClockProvider)?.clock?.now()
            ?: OtelJavaClock.getDefault().now()
        return end - span.startTimestamp
    }

    override fun getKind(): OtelJavaSpanKind = span.spanKind.toOtelJavaSpanKind()

    override fun <T> getAttribute(key: OtelJavaAttributeKey<T>): T? = attributes.get(key)

    override fun getAttributes(): OtelJavaAttributes = attrsFromMap(span.attributes)
}
