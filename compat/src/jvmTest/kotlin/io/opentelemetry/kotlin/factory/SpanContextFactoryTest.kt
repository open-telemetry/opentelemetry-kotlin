package io.opentelemetry.kotlin.factory

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

internal class SpanContextFactoryTest {

    private val spanContextFactory = CompatSpanContextFactory()
    private val traceFlagsFactory = CompatTraceFlagsFactory()
    private val traceStateFactory = CompatTraceStateFactory()

    @Test
    fun `test invalid`() {
        assertSame(spanContextFactory.invalid, spanContextFactory.invalid)
    }

    @Test
    fun `test valid`() {
        val generator = CompatIdGenerator()
        val traceId = generator.generateTraceIdBytes()
        val spanId = generator.generateSpanIdBytes()
        val traceFlags = traceFlagsFactory.default
        val traceState = traceStateFactory.default
        val spanContext = spanContextFactory.create(
            traceId,
            spanId,
            traceFlags,
            traceState
        )
        assertEquals(traceId.toHexString(), spanContext.traceIdBytes.toHexString())
        assertEquals(spanId.toHexString(), spanContext.spanIdBytes.toHexString())
    }
}
