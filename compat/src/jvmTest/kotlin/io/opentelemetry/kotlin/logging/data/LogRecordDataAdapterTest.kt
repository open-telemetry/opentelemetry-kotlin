package io.opentelemetry.kotlin.logging.data

import io.opentelemetry.kotlin.aliases.OtelJavaAttributeKey
import io.opentelemetry.kotlin.aliases.OtelJavaAttributes
import io.opentelemetry.kotlin.aliases.OtelJavaSeverity
import io.opentelemetry.kotlin.fakes.otel.java.FakeOtelJavaLogRecordData
import io.opentelemetry.kotlin.logging.SeverityNumber
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class LogRecordDataAdapterTest {

    @Test
    fun testConversion() {
        val adapter = LogRecordDataAdapter(
            FakeOtelJavaLogRecordData(
                implTimestamp = 5,
                implObservedTimestamp = 10,
                implSeverity = OtelJavaSeverity.ERROR,
                implAttributes = OtelJavaAttributes.of(OtelJavaAttributeKey.stringKey("key"), "value"),
            )
        )
        assertEquals(5, adapter.timestamp)
        assertEquals(10, adapter.observedTimestamp)
        assertEquals(SeverityNumber.ERROR, adapter.severityNumber)
        assertEquals("warning", adapter.severityText)
        assertEquals("Hello, world", adapter.body)
        assertEquals(mapOf("key" to "value"), adapter.attributes)
        assertEquals(0, adapter.droppedAttributesCount)
    }

    @Test
    fun testUnsetTimestampIsNull() {
        assertNull(LogRecordDataAdapter(FakeOtelJavaLogRecordData(implTimestamp = 0)).timestamp)
    }
}
