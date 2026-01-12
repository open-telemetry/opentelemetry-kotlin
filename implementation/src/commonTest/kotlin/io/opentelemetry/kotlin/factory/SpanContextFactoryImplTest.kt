package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.ExperimentalApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
internal class SpanContextFactoryImplTest {

    private val idFactory = TracingIdFactoryImpl()
    private val traceFlagsFactory = TraceFlagsFactoryImpl()
    private val traceStateFactory = TraceStateFactoryImpl()
    private val factory = SpanContextFactoryImpl(idFactory, traceFlagsFactory, traceStateFactory)

    @Test
    internal fun testInvalidProperty() {
        val spanContext = factory.invalid

        assertEquals("00000000000000000000000000000000", spanContext.traceId)
        assertEquals("0000000000000000", spanContext.spanId)
        assertEquals(traceFlagsFactory.default, spanContext.traceFlags)
        assertEquals(traceStateFactory.default, spanContext.traceState)
        assertFalse(spanContext.isValid)
        assertFalse(spanContext.isRemote)
    }

    @Test
    internal fun testValidSpanContext() {
        val traceId = "12345678901234567890123456789012"
        val spanId = "1234567890123456"
        val traceFlags = traceFlagsFactory.default
        val traceState = traceStateFactory.default

        val spanContext = factory.create(traceId, spanId, traceFlags, traceState)

        assertEquals(traceId, spanContext.traceId)
        assertEquals(spanId, spanContext.spanId)
        assertEquals(traceFlags, spanContext.traceFlags)
        assertEquals(traceState, spanContext.traceState)
        assertTrue(spanContext.isValid)
        assertFalse(spanContext.isRemote)
    }

    @Test
    internal fun testInvalidTraceId() {
        val traceId = "00000000000000000000000000000000" // all zeros
        val spanId = "1234567890123456"
        val traceFlags = traceFlagsFactory.default
        val traceState = traceStateFactory.default

        val spanContext = factory.create(traceId, spanId, traceFlags, traceState)

        assertEquals(traceId, spanContext.traceId)
        assertEquals(spanId, spanContext.spanId)
        assertEquals(traceFlags, spanContext.traceFlags)
        assertEquals(traceState, spanContext.traceState)
        assertFalse(spanContext.isValid) // invalid because trace ID is all zeros
    }

    @Test
    internal fun testInvalidSpanId() {
        val traceId = "12345678901234567890123456789012"
        val spanId = "0000000000000000" // all zeros
        val traceFlags = traceFlagsFactory.default
        val traceState = traceStateFactory.default

        val spanContext = factory.create(traceId, spanId, traceFlags, traceState)

        assertEquals(traceId, spanContext.traceId)
        assertEquals(spanId, spanContext.spanId)
        assertEquals(traceFlags, spanContext.traceFlags)
        assertEquals(traceState, spanContext.traceState)
        assertFalse(spanContext.isValid) // invalid because span ID is all zeros
    }

    @Test
    internal fun testInvalidLengthIds() {
        val shortTraceId = "123"
        val shortSpanId = "456"
        val traceFlags = traceFlagsFactory.default
        val traceState = traceStateFactory.default

        val spanContext = factory.create(shortTraceId, shortSpanId, traceFlags, traceState)

        assertEquals("00000000000000000000000000000000", spanContext.traceId)
        assertEquals("0000000000000000", spanContext.spanId)
        assertEquals(traceFlags, spanContext.traceFlags)
        assertEquals(traceState, spanContext.traceState)
        assertFalse(spanContext.isValid) // invalid because of wrong lengths
    }

    @Test
    internal fun testInvalidHexChars() {
        val invalidTraceId = "zzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzz" // 32 chars but not hex
        val invalidSpanId = "gggggggggggggggg" // 16 chars but not hex
        val traceFlags = traceFlagsFactory.default
        val traceState = traceStateFactory.default

        val spanContext = factory.create(invalidTraceId, invalidSpanId, traceFlags, traceState)

        assertEquals("00000000000000000000000000000000", spanContext.traceId)
        assertEquals("0000000000000000", spanContext.spanId)
        assertEquals(traceFlags, spanContext.traceFlags)
        assertEquals(traceState, spanContext.traceState)
        assertFalse(spanContext.isValid) // invalid because of non-hex characters
    }

    @Test
    internal fun testUppercaseHex() {
        val traceId = "ABCDEF1234567890ABCDEF1234567890" // uppercase hex
        val spanId = "ABCDEF1234567890" // uppercase hex
        val traceFlags = traceFlagsFactory.default
        val traceState = traceStateFactory.default

        val spanContext = factory.create(traceId, spanId, traceFlags, traceState)

        assertEquals(traceId.lowercase(), spanContext.traceId)
        assertEquals(spanId.lowercase(), spanContext.spanId)
        assertEquals(traceFlags, spanContext.traceFlags)
        assertEquals(traceState, spanContext.traceState)
        assertTrue(spanContext.isValid) // valid because uppercase hex is accepted
    }

    @Test
    internal fun testLowercaseHex() {
        val traceId = "abcdef1234567890abcdef1234567890" // lowercase hex
        val spanId = "abcdef1234567890" // lowercase hex
        val traceFlags = traceFlagsFactory.default
        val traceState = traceStateFactory.default

        val spanContext = factory.create(traceId, spanId, traceFlags, traceState)

        assertEquals(traceId, spanContext.traceId)
        assertEquals(spanId, spanContext.spanId)
        assertEquals(traceFlags, spanContext.traceFlags)
        assertEquals(traceState, spanContext.traceState)
        assertTrue(spanContext.isValid) // valid hex
    }

    @Test
    internal fun testCreateWithCustomTraceFlagsAndState() {
        val traceId = "12345678901234567890123456789012"
        val spanId = "1234567890123456"
        val customTraceFlags = traceFlagsFactory.create(sampled = true, random = true)
        val customTraceState = traceStateFactory.default
            .put("key1", "value1")
            .put("key2", "value2")

        val spanContext = factory.create(traceId, spanId, customTraceFlags, customTraceState)

        assertEquals(traceId, spanContext.traceId)
        assertEquals(spanId, spanContext.spanId)
        assertEquals(customTraceFlags, spanContext.traceFlags)
        assertEquals(customTraceState, spanContext.traceState)
        assertTrue(spanContext.isValid)
        assertFalse(spanContext.isRemote)
    }

    @Test
    internal fun testStatePreservedWithInvalidIds() {
        val invalidTraceId = "invalid"
        val invalidSpanId = "bad"
        val customTraceFlags = traceFlagsFactory.create(sampled = true, random = false)
        val customTraceState = traceStateFactory.default.put("key", "value")

        val spanContext = factory.create(invalidTraceId, invalidSpanId, customTraceFlags, customTraceState)

        assertEquals("00000000000000000000000000000000", spanContext.traceId)
        assertEquals("0000000000000000", spanContext.spanId)
        assertEquals(customTraceFlags, spanContext.traceFlags)
        assertEquals(customTraceState, spanContext.traceState)
        assertFalse(spanContext.isValid)
        assertFalse(spanContext.isRemote)
    }
}
