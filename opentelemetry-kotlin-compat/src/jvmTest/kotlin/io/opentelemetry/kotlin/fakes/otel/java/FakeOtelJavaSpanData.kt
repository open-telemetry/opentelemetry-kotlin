@file:Suppress("DEPRECATION", "TYPEALIAS_EXPANSION_DEPRECATION")

package io.opentelemetry.kotlin.fakes.otel.java

import io.opentelemetry.kotlin.aliases.OtelJavaAttributeKey
import io.opentelemetry.kotlin.aliases.OtelJavaAttributes
import io.opentelemetry.kotlin.aliases.OtelJavaEventData
import io.opentelemetry.kotlin.aliases.OtelJavaInstrumentationLibraryInfo
import io.opentelemetry.kotlin.aliases.OtelJavaLinkData
import io.opentelemetry.kotlin.aliases.OtelJavaResource
import io.opentelemetry.kotlin.aliases.OtelJavaSpanContext
import io.opentelemetry.kotlin.aliases.OtelJavaSpanData
import io.opentelemetry.kotlin.aliases.OtelJavaSpanKind
import io.opentelemetry.kotlin.aliases.OtelJavaStatusCode
import io.opentelemetry.kotlin.aliases.OtelJavaStatusData

internal class FakeOtelJavaSpanData(
    val implName: String = "fake_span",
    val implSpanKind: OtelJavaSpanKind = OtelJavaSpanKind.INTERNAL,
    val implSpanContext: OtelJavaSpanContext = OtelJavaSpanContext.getInvalid(),
    val implParentSpanContext: OtelJavaSpanContext = OtelJavaSpanContext.getInvalid(),
    val implAttributes: OtelJavaAttributes = OtelJavaAttributes.of(OtelJavaAttributeKey.stringKey("key"), "value"),
    val implEventData: List<OtelJavaEventData> = listOf(OtelJavaEventData.create(150, "event", implAttributes)),
    val implLinkData: List<OtelJavaLinkData> = listOf(
        OtelJavaLinkData.create(OtelJavaSpanContext.getInvalid(), implAttributes)
    ),
    val implStartNs: Long = 100,
    val implEndNs: Long = implStartNs + 100,
    val implEnded: Boolean = true,
    val implStatusData: OtelJavaStatusData = OtelJavaStatusData.create(OtelJavaStatusCode.ERROR, "Whoops"),
    val implResource: OtelJavaResource = OtelJavaResource.create(implAttributes, "http://example.com/foo"),
) : OtelJavaSpanData {

    override fun getName(): String = implName
    override fun getKind(): OtelJavaSpanKind = implSpanKind
    override fun getSpanContext(): OtelJavaSpanContext = implSpanContext
    override fun getParentSpanContext(): OtelJavaSpanContext = implParentSpanContext
    override fun getStatus(): OtelJavaStatusData = implStatusData
    override fun getStartEpochNanos(): Long = implStartNs
    override fun getAttributes(): OtelJavaAttributes = implAttributes
    override fun getEvents(): List<OtelJavaEventData> = implEventData
    override fun getLinks(): List<OtelJavaLinkData> = implLinkData
    override fun getEndEpochNanos(): Long = implEndNs
    override fun hasEnded(): Boolean = implEnded
    override fun getTotalRecordedEvents(): Int = events.size
    override fun getTotalRecordedLinks(): Int = links.size
    override fun getTotalAttributeCount(): Int = implAttributes.size()

    @Deprecated("Deprecated in Java")
    override fun getInstrumentationLibraryInfo(): OtelJavaInstrumentationLibraryInfo = OtelJavaInstrumentationLibraryInfo.empty()
    override fun getResource(): OtelJavaResource = implResource
}
