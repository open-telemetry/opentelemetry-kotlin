@file:Suppress("DEPRECATION", "TYPEALIAS_EXPANSION_DEPRECATION")

package io.opentelemetry.kotlin.fakes.otel.java

import io.opentelemetry.kotlin.aliases.OtelJavaAttributeKey
import io.opentelemetry.kotlin.aliases.OtelJavaAttributes
import io.opentelemetry.kotlin.aliases.OtelJavaInstrumentationLibraryInfo
import io.opentelemetry.kotlin.aliases.OtelJavaReadableSpan
import io.opentelemetry.kotlin.aliases.OtelJavaSpanContext
import io.opentelemetry.kotlin.aliases.OtelJavaSpanData
import io.opentelemetry.kotlin.aliases.OtelJavaSpanKind

internal class FakeOtelJavaReadableSpan(
    var otelJavaSpanData: OtelJavaSpanData = FakeOtelJavaSpanData()
) : OtelJavaReadableSpan {

    override fun getSpanContext(): OtelJavaSpanContext = otelJavaSpanData.spanContext

    override fun getParentSpanContext(): OtelJavaSpanContext = otelJavaSpanData.parentSpanContext

    override fun getName(): String = otelJavaSpanData.name

    @Deprecated("Deprecated in Java")
    override fun getInstrumentationLibraryInfo(): OtelJavaInstrumentationLibraryInfo = otelJavaSpanData.instrumentationLibraryInfo

    override fun hasEnded(): Boolean = otelJavaSpanData.hasEnded()

    override fun getLatencyNanos(): Long = otelJavaSpanData.endEpochNanos - otelJavaSpanData.startEpochNanos

    override fun getKind(): OtelJavaSpanKind? = otelJavaSpanData.kind

    override fun getAttributes(): OtelJavaAttributes = otelJavaSpanData.attributes

    override fun <T> getAttribute(key: OtelJavaAttributeKey<T>): T? = otelJavaSpanData.attributes.get(key)

    override fun toSpanData(): OtelJavaSpanData = otelJavaSpanData
}
