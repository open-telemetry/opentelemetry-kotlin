package io.opentelemetry.kotlin.factory

import kotlin.test.Test
import kotlin.test.assertEquals

internal class IdGeneratorTest {

    private val idGenerator = CompatIdGenerator()

    @Test
    fun `test invalid`() {
        assertEquals("00000000000000000000000000000000", idGenerator.invalidTraceId.toHexString())
        assertEquals("0000000000000000", idGenerator.invalidSpanId.toHexString())
    }

    @Test
    fun `test trace ID generation`() {
        val traceId = idGenerator.generateTraceIdBytes()
        assertEquals(32, traceId.toHexString().length)
    }

    @Test
    fun `test span ID generation`() {
        val spanId = idGenerator.generateSpanIdBytes()
        assertEquals(16, spanId.toHexString().length)
    }
}
