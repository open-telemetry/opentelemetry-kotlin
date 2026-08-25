package io.opentelemetry.kotlin.logging.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.FakeInstrumentationScopeInfo
import io.opentelemetry.kotlin.context.FakeContext
import io.opentelemetry.kotlin.export.FakeLogExportConfig
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.logging.SeverityNumber
import io.opentelemetry.kotlin.logging.model.FakeReadWriteLogRecord
import io.opentelemetry.kotlin.tracing.FakeSpan
import io.opentelemetry.kotlin.tracing.FakeSpanContext
import io.opentelemetry.kotlin.tracing.SpanContext
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
internal class SpanEventBridgeLogRecordProcessorTest {

    private val otherSpanContext: SpanContext = FakeSpanContext(
        traceIdBytes = ByteArray(16) { 1 },
        spanIdBytes = ByteArray(8) { 2 },
    )

    @Test
    fun testEventBridgedToSpanEvent() {
        val span = FakeSpan(spanContext = FakeSpanContext.VALID)
        val log = eventLogRecord(
            timestamp = 150,
            observedTimestamp = 300,
            attributes = mapOf(
                "string_key" to "value",
                "long_key" to 42L,
                "boolean_key" to true,
                "string_list_key" to listOf("a", "b"),
            ),
        )

        SpanEventBridgeLogRecordProcessor().onEmit(log, FakeContext(span = span))

        val event = span.events.single()
        assertEquals("my_event", event.name)
        assertEquals(150, event.timestamp)
        assertEquals("value", event.attributes["string_key"])
        assertEquals(42L, event.attributes["long_key"])
        assertEquals(true, event.attributes["boolean_key"])
        assertEquals(listOf("a", "b"), event.attributes["string_list_key"])
    }

    @Test
    fun testTimestampFallsBackToObservedTimestamp() {
        val span = FakeSpan(spanContext = FakeSpanContext.VALID)
        val log = eventLogRecord(timestamp = null, observedTimestamp = 300)

        SpanEventBridgeLogRecordProcessor().onEmit(log, FakeContext(span = span))

        assertEquals(300, span.events.single().timestamp)
    }

    @Test
    fun testTimestampOmittedWhenNeitherIsSet() {
        val span = FakeSpan(spanContext = FakeSpanContext.VALID)
        val log = eventLogRecord(timestamp = null, observedTimestamp = null)

        SpanEventBridgeLogRecordProcessor().onEmit(log, FakeContext(span = span))

        assertEquals(0, span.events.single().timestamp)
    }

    @Test
    fun testEventBridgedWhenIdsMatchByContentNotIdentity() {
        val span = FakeSpan(spanContext = FakeSpanContext.VALID)
        val log = eventLogRecord(
            spanContext = FakeSpanContext(
                traceIdBytes = FakeSpanContext.VALID.traceIdBytes.copyOf(),
                spanIdBytes = FakeSpanContext.VALID.spanIdBytes.copyOf(),
            ),
        )

        SpanEventBridgeLogRecordProcessor().onEmit(log, FakeContext(span = span))

        assertEquals("my_event", span.events.single().name)
    }

    @Test
    fun testLogRecordWithoutEventNameNotBridged() {
        assertNotBridged(eventLogRecord(eventName = null))
    }

    @Test
    fun testLogRecordWithEmptyEventNameNotBridged() {
        assertNotBridged(eventLogRecord(eventName = ""))
    }

    @Test
    fun testLogRecordWithInvalidSpanContextNotBridged() {
        assertNotBridged(eventLogRecord(spanContext = FakeSpanContext.INVALID))
    }

    @Test
    fun testLogRecordWithMismatchedSpanNotBridged() {
        assertNotBridged(eventLogRecord(spanContext = otherSpanContext))
    }

    @Test
    fun testNonRecordingSpanNotBridged() {
        val span = FakeSpan(spanContext = FakeSpanContext.VALID)
        span.end()

        SpanEventBridgeLogRecordProcessor().onEmit(eventLogRecord(), FakeContext(span = span))

        assertTrue(span.events.isEmpty())
    }

    @Test
    fun testLogRecordNotAlteredByBridging() {
        val span = FakeSpan(spanContext = FakeSpanContext.VALID)
        val attributes = mapOf<String, Any>("string_key" to "value")
        val log = eventLogRecord(attributes = attributes)

        SpanEventBridgeLogRecordProcessor().onEmit(log, FakeContext(span = span))

        assertEquals("my_event", log.eventName)
        assertEquals("my_body", log.body)
        assertEquals(FakeSpanContext.VALID, log.spanContext)
        assertEquals(attributes, log.attributes)
    }

    @Test
    fun testOnEmitNoOpAfterShutdown() = runTest {
        val span = FakeSpan(spanContext = FakeSpanContext.VALID)
        val processor = SpanEventBridgeLogRecordProcessor()
        processor.shutdown()

        processor.onEmit(eventLogRecord(), FakeContext(span = span))

        assertTrue(span.events.isEmpty())
    }

    @Test
    fun testShutdownReturnsSuccessOnSecondCall() = runTest {
        val processor = SpanEventBridgeLogRecordProcessor()
        assertEquals(OperationResultCode.Success, processor.shutdown())
        assertEquals(OperationResultCode.Success, processor.shutdown())
    }

    @Test
    fun testForceFlushSucceedsBeforeAndAfterShutdown() = runTest {
        val processor = SpanEventBridgeLogRecordProcessor()

        assertEquals(OperationResultCode.Success, processor.forceFlush())
        processor.shutdown()
        assertEquals(OperationResultCode.Success, processor.forceFlush())
    }

    @Test
    fun testEnabledForEventNameWithRecordingSpan() {
        val context = FakeContext(span = FakeSpan(spanContext = FakeSpanContext.VALID))
        assertTrue(SpanEventBridgeLogRecordProcessor().isEnabled(context, "my_event"))
    }

    @Test
    fun testEnabledFalseWithoutEventName() {
        val context = FakeContext(span = FakeSpan(spanContext = FakeSpanContext.VALID))
        val processor = SpanEventBridgeLogRecordProcessor()
        assertFalse(processor.isEnabled(context, null))
        assertFalse(processor.isEnabled(context, ""))
    }

    @Test
    fun testEnabledFalseForNonRecordingSpan() {
        val span = FakeSpan(spanContext = FakeSpanContext.VALID)
        span.end()

        assertFalse(
            SpanEventBridgeLogRecordProcessor().isEnabled(FakeContext(span = span), "my_event"),
        )
    }

    @Test
    fun testEnabledFalseAfterShutdown() = runTest {
        val context = FakeContext(span = FakeSpan(spanContext = FakeSpanContext.VALID))
        val processor = SpanEventBridgeLogRecordProcessor()
        processor.shutdown()
        assertFalse(processor.isEnabled(context, "my_event"))
    }

    @Test
    fun testProcessorCreatedFromDsl() {
        val span = FakeSpan(spanContext = FakeSpanContext.VALID)
        val processor = FakeLogExportConfig().spanEventBridgeLogRecordProcessor()

        processor.onEmit(eventLogRecord(), FakeContext(span = span))

        assertEquals("my_event", span.events.single().name)
    }

    private fun assertNotBridged(log: FakeReadWriteLogRecord) {
        val span = FakeSpan(spanContext = FakeSpanContext.VALID)

        SpanEventBridgeLogRecordProcessor().onEmit(log, FakeContext(span = span))

        assertTrue(span.events.isEmpty())
    }

    private fun LogRecordProcessor.isEnabled(context: FakeContext, eventName: String?): Boolean =
        enabled(context, FakeInstrumentationScopeInfo(), SeverityNumber.INFO, eventName)

    private fun eventLogRecord(
        eventName: String? = "my_event",
        spanContext: SpanContext = FakeSpanContext.VALID,
        timestamp: Long? = 150,
        observedTimestamp: Long? = 300,
        attributes: Map<String, Any> = emptyMap(),
    ) = FakeReadWriteLogRecord(
        timestamp = timestamp,
        observedTimestamp = observedTimestamp,
        body = "my_body",
        eventName = eventName,
        spanContext = spanContext,
        attributes = attributes,
    )
}
