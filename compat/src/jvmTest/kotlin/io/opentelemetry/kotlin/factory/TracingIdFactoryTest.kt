package io.opentelemetry.kotlin.factory

import kotlin.test.Test
import kotlin.test.assertEquals

internal class TracingIdFactoryTest {

    private val factory = createCompatSdkFactory().tracingIdFactory

    @Test
    fun `test invalid`() {
        assertEquals("00000000000000000000000000000000", factory.invalidTraceId.toHexString())
        assertEquals("0000000000000000", factory.invalidSpanId.toHexString())
    }

    @Test
    fun `test trace ID generation`() {
        val traceId = factory.generateTraceIdBytes()
        assertEquals(32, traceId.toHexString().length)
    }

    @Test
    fun `test span ID generation`() {
        val spanId = factory.generateSpanIdBytes()
        assertEquals(16, spanId.toHexString().length)
    }
}
