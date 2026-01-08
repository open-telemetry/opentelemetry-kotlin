@file:Suppress("DEPRECATION", "TYPEALIAS_EXPANSION_DEPRECATION")

package io.opentelemetry.kotlin.logging

import io.opentelemetry.kotlin.aliases.OtelJavaAttributes
import io.opentelemetry.kotlin.aliases.OtelJavaBody
import io.opentelemetry.kotlin.aliases.OtelJavaInstrumentationScopeInfo
import io.opentelemetry.kotlin.aliases.OtelJavaLogRecordData
import io.opentelemetry.kotlin.aliases.OtelJavaResource
import io.opentelemetry.kotlin.aliases.OtelJavaSeverity
import io.opentelemetry.kotlin.aliases.OtelJavaSpanContext

/**
 * Implementation of [io.opentelemetry.kotlin.aliases.OtelJavaLogRecordData] that we can construct new instances of. Required for
 * backwards compatibility with opentelemetry-java exporters.
 */
internal class OtelJavaLogRecordDataImpl(
    private val resourceImpl: OtelJavaResource,
    private val scopeImpl: OtelJavaInstrumentationScopeInfo,
    private val timestampNanos: Long,
    private val observedTimestampNanos: Long,
    private val spanContextImpl: OtelJavaSpanContext,
    private val severityImpl: OtelJavaSeverity,
    private val severityTextImpl: String?,
    private val bodyImpl: OtelJavaBody,
    private val attributesImpl: OtelJavaAttributes,
) : OtelJavaLogRecordData {

    override fun getResource(): OtelJavaResource = resourceImpl
    override fun getInstrumentationScopeInfo(): OtelJavaInstrumentationScopeInfo = scopeImpl
    override fun getTimestampEpochNanos(): Long = timestampNanos
    override fun getObservedTimestampEpochNanos(): Long = observedTimestampNanos
    override fun getSpanContext(): OtelJavaSpanContext = spanContextImpl
    override fun getSeverity(): OtelJavaSeverity = severityImpl
    override fun getSeverityText(): String? = severityTextImpl

    @Deprecated("Deprecated in Java")
    override fun getBody(): OtelJavaBody = bodyImpl

    override fun getAttributes(): OtelJavaAttributes = attributesImpl
    override fun getTotalAttributeCount(): Int = attributesImpl.size()
}
