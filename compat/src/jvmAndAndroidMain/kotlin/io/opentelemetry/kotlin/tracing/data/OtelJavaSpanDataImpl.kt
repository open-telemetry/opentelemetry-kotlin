@file:Suppress("TYPEALIAS_EXPANSION_DEPRECATION", "DEPRECATION")

package io.opentelemetry.kotlin.tracing.data

import io.opentelemetry.kotlin.aliases.OtelJavaAttributes
import io.opentelemetry.kotlin.aliases.OtelJavaEventData
import io.opentelemetry.kotlin.aliases.OtelJavaInstrumentationLibraryInfo
import io.opentelemetry.kotlin.aliases.OtelJavaLinkData
import io.opentelemetry.kotlin.aliases.OtelJavaResource
import io.opentelemetry.kotlin.aliases.OtelJavaSpanContext
import io.opentelemetry.kotlin.aliases.OtelJavaSpanData
import io.opentelemetry.kotlin.aliases.OtelJavaSpanKind
import io.opentelemetry.kotlin.aliases.OtelJavaStatusData

/**
 * Implementation of [io.opentelemetry.kotlin.aliases.OtelJavaSpanData] that we can construct new instances of. Required for
 * backwards compatibility with opentelemetry-java exporters.
 */
@Suppress("DEPRECATION")
internal class OtelJavaSpanDataImpl(
    private val nameImpl: String,
    private val kindImpl: OtelJavaSpanKind,
    private val spanContextImpl: OtelJavaSpanContext,
    private val parentSpanContextImpl: OtelJavaSpanContext,
    private val statusImpl: OtelJavaStatusData,
    private val startEpochNanosImpl: Long,
    private val attributesImpl: OtelJavaAttributes,
    private val eventsImpl: List<OtelJavaEventData>,
    private val linksImpl: List<OtelJavaLinkData>,
    private val endEpochNanosImpl: Long,
    private val hasEndedImpl: Boolean,
    private val scopeImpl: OtelJavaInstrumentationLibraryInfo,
    private val resourceImpl: OtelJavaResource,
) : OtelJavaSpanData {

    override fun getName(): String = nameImpl
    override fun getKind(): OtelJavaSpanKind = kindImpl
    override fun getSpanContext(): OtelJavaSpanContext = spanContextImpl
    override fun getParentSpanContext(): OtelJavaSpanContext = parentSpanContextImpl
    override fun getStatus(): OtelJavaStatusData = statusImpl
    override fun getStartEpochNanos(): Long = startEpochNanosImpl
    override fun getAttributes(): OtelJavaAttributes = attributesImpl
    override fun getEvents(): List<OtelJavaEventData> = eventsImpl
    override fun getLinks(): List<OtelJavaLinkData> = linksImpl
    override fun getEndEpochNanos(): Long = endEpochNanosImpl
    override fun hasEnded(): Boolean = hasEndedImpl
    override fun getTotalRecordedEvents(): Int = eventsImpl.size
    override fun getTotalRecordedLinks(): Int = linksImpl.size
    override fun getTotalAttributeCount(): Int = attributesImpl.size()

    @Deprecated("Deprecated in Java")
    override fun getInstrumentationLibraryInfo(): OtelJavaInstrumentationLibraryInfo = scopeImpl
    override fun getResource(): OtelJavaResource = resourceImpl
}
