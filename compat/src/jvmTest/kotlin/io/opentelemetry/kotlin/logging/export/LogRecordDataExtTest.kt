package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.aliases.OtelJavaSdkLoggerProvider
import io.opentelemetry.kotlin.aliases.OtelJavaSeverity
import io.opentelemetry.kotlin.aliases.OtelJavaValueType
import io.opentelemetry.kotlin.attributes.AnyValue
import io.opentelemetry.kotlin.error.NoopSdkErrorHandler
import io.opentelemetry.kotlin.fakes.otel.java.FakeOtelJavaLogRecordData
import io.opentelemetry.kotlin.fakes.otel.java.FakeOtelJavaLogRecordProcessor
import io.opentelemetry.kotlin.logging.LoggerProviderAdapter
import io.opentelemetry.kotlin.logging.OtelJavaLogRecordDataImpl
import io.opentelemetry.kotlin.logging.data.FakeLogRecordData
import io.opentelemetry.kotlin.logging.data.LogRecordDataAdapter
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertSame

internal class LogRecordDataExtTest {

    @Test
    fun testLogRecordDefaultConversions() {
        val record = FakeLogRecordData()
        val observed = record.toOtelJavaLogRecordData()
        assertEquals(record.timestamp, observed.timestampEpochNanos)
        assertEquals(record.observedTimestamp, observed.observedTimestampEpochNanos)
        assertEquals(record.severityText, observed.severityText)
        assertEquals(OtelJavaSeverity.WARN, observed.severity)
        assertEquals(record.body, observed.bodyValue?.asString())
        assertNull(observed.eventName)
    }

    @Test
    fun testLogRecordDroppedAttributesConversion() {
        val record = FakeLogRecordData(droppedAttributesCount = 3)
        val observed = record.toOtelJavaLogRecordData()
        assertEquals(record.attributes.size + 3, observed.totalAttributeCount)
    }

    @Test
    fun testLogRecordEventNameConversion() {
        val record = FakeLogRecordData(eventName = "my_event_name")
        val observed = record.toOtelJavaLogRecordData()
        assertEquals("my_event_name", observed.eventName)
    }

    @Test
    fun testLogRecordStructuredBodyConversion() {
        val structuredBody = mapOf("key" to "value")
        val record = FakeLogRecordData(body = structuredBody)
        val observed = record.toOtelJavaLogRecordData()
        assertEquals(OtelJavaValueType.KEY_VALUE_LIST, observed.bodyValue?.type)
        assertEquals("""{"key":"value"}""", observed.bodyValue?.asString())
    }

    @Test
    fun testLogRecordAnyValueStringBody() {
        val record = FakeLogRecordData(body = AnyValue.StringValue("hello"))
        val observed = record.toOtelJavaLogRecordData()
        assertEquals("hello", observed.bodyValue?.asString())
    }

    @Test
    fun testLogRecordAnyValuePrimitiveBodies() {
        // Primitive variants are unwrapped to their payload rather than rendered via
        // AnyValue.toString(), which would yield e.g. "LongValue(value=3)".
        mapOf<AnyValue, String>(
            AnyValue.LongValue(3) to "3",
            AnyValue.BoolValue(true) to "true",
            AnyValue.DoubleValue(3.14) to "3.14"
        ).forEach { (body, expected) ->
            val observed = FakeLogRecordData(body = body).toOtelJavaLogRecordData()
            assertEquals(expected, observed.bodyValue?.asString())
        }
    }

    @Test
    fun testLogRecordAnyValueNullBody() {
        val record = FakeLogRecordData(body = AnyValue.NullValue)
        val observed = record.toOtelJavaLogRecordData()
        assertNull(observed.bodyValue?.asString())
    }

    @Test
    fun testLogRecordAnyValueMapBody() {
        val map = AnyValue.MapValue(mapOf("k" to AnyValue.StringValue("v")))
        val record = FakeLogRecordData(body = map)
        val observed = record.toOtelJavaLogRecordData()
        assertEquals(OtelJavaValueType.KEY_VALUE_LIST, observed.bodyValue?.type)
        assertEquals("{\"k\":\"v\"}", observed.bodyValue?.asString())
        @Suppress("DEPRECATION")
        assertEquals("{\"k\":\"v\"}", observed.body.asString())
    }

    @Test
    fun testLogRecordAnyValueStructuredBodies() {
        mapOf<AnyValue, String>(
            AnyValue.BytesValue("hello".encodeToByteArray()) to "aGVsbG8=",
            AnyValue.ListValue(listOf(AnyValue.LongValue(1), AnyValue.NullValue)) to "[1,null]",
        ).forEach { (body, expected) ->
            val observed = FakeLogRecordData(body = body).toOtelJavaLogRecordData()
            assertEquals(expected, observed.bodyValue?.asString())
        }
    }

    @Test
    fun testLogRecordNullConversions() {
        val record = FakeLogRecordData(
            timestamp = null,
            observedTimestamp = null,
            severityNumber = null,
            severityText = null,
            body = null,
        )
        val observed = record.toOtelJavaLogRecordData()
        assertEquals(0, observed.timestampEpochNanos)
        assertEquals(0, observed.observedTimestampEpochNanos)
        assertEquals(OtelJavaSeverity.UNDEFINED_SEVERITY_NUMBER, observed.severity)
        assertNull(observed.severityText)
        assertNull(observed.bodyValue?.asString())
        assertNull(observed.eventName)
    }

    @Test
    fun testLogRecordDataAdapterIsUnwrapped() {
        val impl = FakeOtelJavaLogRecordData()
        assertSame(impl, LogRecordDataAdapter(impl).toOtelJavaLogRecordData())
    }

    @Test
    fun testReadWriteLogRecordAdapterIsUnwrapped() {
        val processor = FakeOtelJavaLogRecordProcessor()
        LoggerProviderAdapter(
            OtelJavaSdkLoggerProvider.builder().addLogRecordProcessor(processor).build(),
            NoopSdkErrorHandler,
        ).getLogger("test").emit(body = "log", timestamp = 42)

        val impl = processor.exports.single()
        val observed = ReadWriteLogRecordAdapter(impl).toOtelJavaLogRecordData()
        assertFalse(observed is OtelJavaLogRecordDataImpl)
        assertEquals(impl.toLogRecordData().timestampEpochNanos, observed.timestampEpochNanos)
        assertEquals("log", observed.bodyValue?.asString())
    }
}
