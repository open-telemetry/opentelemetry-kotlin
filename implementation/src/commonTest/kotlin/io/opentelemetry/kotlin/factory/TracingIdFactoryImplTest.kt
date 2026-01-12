package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.ExperimentalApi
import kotlin.random.Random
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
internal class TracingIdFactoryImplTest {

    private companion object {
        private const val SPAN_ID_PATTERN = "^[0-9a-f]{16}$"
        private const val TRACE_ID_PATTERN = "^[0-9a-f]{32}$"
    }

    private lateinit var factory: TracingIdFactory

    @BeforeTest
    fun setUp() {
        factory = TracingIdFactoryImpl(Random(0))
    }

    @Test
    fun testInvalidId() {
        assertEquals("00000000000000000000000000000000", factory.invalidTraceId.toHexString())
        assertEquals("0000000000000000", factory.invalidSpanId.toHexString())
    }

    @Test
    fun testValidSpanId() {
        val spanId = factory.generateSpanIdBytes()
        assertEquals(16, spanId.toHexString().length)
        assertTrue(spanId.toHexString().matches(SPAN_ID_PATTERN.toRegex()))
    }

    @Test
    fun testValidTraceId() {
        val traceId = factory.generateTraceIdBytes().toHexString()
        assertEquals(32, traceId.length)
        assertTrue(traceId.matches(TRACE_ID_PATTERN.toRegex()))
    }

    @Test
    fun testDistinctTraceIds() {
        assertEquals(
            "2cc2b48c50aefe53b3974ed91e6b4ea9",
            factory.generateTraceIdBytes().toHexString()
        )
        assertEquals(
            "e77bcc2f537f0b02efe86030ac2c3153",
            factory.generateTraceIdBytes().toHexString()
        )
        assertEquals(
            "c0a79602f9a51310c8eed9889d46ef3b",
            factory.generateTraceIdBytes().toHexString()
        )
    }

    @Test
    fun testDistinctSpanIds() {
        assertEquals("2cc2b48c50aefe53", factory.generateSpanIdBytes().toHexString())
        assertEquals("1e6b4ea924f9baa8", factory.generateSpanIdBytes().toHexString())
        assertEquals("537f0b02efe86030", factory.generateSpanIdBytes().toHexString())
    }
}
