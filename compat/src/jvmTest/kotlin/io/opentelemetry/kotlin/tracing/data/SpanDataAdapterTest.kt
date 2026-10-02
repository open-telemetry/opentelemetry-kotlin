package io.opentelemetry.kotlin.tracing.data

import io.opentelemetry.kotlin.aliases.OtelJavaAttributeKey
import io.opentelemetry.kotlin.aliases.OtelJavaAttributes
import io.opentelemetry.kotlin.aliases.OtelJavaEventData
import io.opentelemetry.kotlin.aliases.OtelJavaLinkData
import io.opentelemetry.kotlin.aliases.OtelJavaSpanContext
import io.opentelemetry.kotlin.fakes.otel.java.FakeOtelJavaSpanData
import org.junit.Assert.assertEquals
import org.junit.Test

internal class SpanDataAdapterTest {

    private val attrs = OtelJavaAttributes.of(OtelJavaAttributeKey.stringKey("key"), "value")

    @Test
    fun testDroppedAttributesCount() {
        val adapter = SpanDataAdapter(
            FakeOtelJavaSpanData(
                implEventData = listOf(OtelJavaEventData.create(150, "event", attrs, 3)),
                implLinkData = listOf(OtelJavaLinkData.create(OtelJavaSpanContext.getInvalid(), attrs, 4)),
                implTotalAttributeCount = 5,
            )
        )
        assertEquals(4, adapter.droppedAttributesCount)
        assertEquals(2, adapter.events.single().droppedAttributesCount)
        assertEquals(3, adapter.links.single().droppedAttributesCount)
    }

    @Test
    fun testDroppedAttributesCountNeverNegative() {
        val adapter = SpanDataAdapter(
            FakeOtelJavaSpanData(
                implEventData = listOf(OtelJavaEventData.create(150, "event", attrs, 0)),
                implLinkData = listOf(OtelJavaLinkData.create(OtelJavaSpanContext.getInvalid(), attrs, 0)),
                implTotalAttributeCount = 0,
            )
        )
        assertEquals(0, adapter.droppedAttributesCount)
        assertEquals(0, adapter.events.single().droppedAttributesCount)
        assertEquals(0, adapter.links.single().droppedAttributesCount)
    }
}
