package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.ExperimentalApi
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

@OptIn(ExperimentalApi::class)
internal class SpanContextFactoryTest {

    private val factory = createCompatSdkFactory()

    @Test
    fun `test invalid`() {
        assertSame(factory.spanContext.invalid, factory.spanContext.invalid)
    }

    @Test
    fun `test valid`() {
        val generator = CompatTracingIdFactory()
        val traceId = generator.generateTraceIdBytes()
        val spanId = generator.generateSpanIdBytes()
        val traceFlags = factory.traceFlags.default
        val traceState = factory.traceState.default
        val spanContext = factory.spanContext.create(
            traceId,
            spanId,
            traceFlags,
            traceState
        )
        assertEquals(traceId.toHexString(), spanContext.traceIdBytes.toHexString())
        assertEquals(spanId.toHexString(), spanContext.spanIdBytes.toHexString())
    }
}
