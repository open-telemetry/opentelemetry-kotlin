package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.aliases.OtelJavaIdGenerator
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

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

    @Test
    fun `test randomness declaration mirrors the wrapped generator`() {
        assertTrue(idGenerator.generatesRandomTraceIds)
        assertFalse(CompatIdGenerator(NonRandomOtelJavaIdGenerator).generatesRandomTraceIds)
    }

    @Test
    fun `test randomness declaration is forwarded to opentelemetry-java`() {
        assertTrue(OtelJavaIdGeneratorAdapter(idGenerator).generatesRandomTraceIds())
        assertFalse(
            OtelJavaIdGeneratorAdapter(CompatIdGenerator(NonRandomOtelJavaIdGenerator)).generatesRandomTraceIds()
        )
    }

    /**
     * Inherits opentelemetry-java's default of `generatesRandomTraceIds() == false`.
     */
    private object NonRandomOtelJavaIdGenerator : OtelJavaIdGenerator {
        private val delegate = OtelJavaIdGenerator.random()
        override fun generateSpanId(): String = delegate.generateSpanId()
        override fun generateTraceId(): String = delegate.generateTraceId()
    }
}
