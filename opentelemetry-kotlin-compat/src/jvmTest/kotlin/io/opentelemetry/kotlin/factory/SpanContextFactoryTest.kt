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
        assertSame(factory.spanContextFactory.invalid, factory.spanContextFactory.invalid)
    }

    @Test
    fun `test valid`() {
        val generator = CompatTracingIdFactory()
        val traceId = generator.generateTraceIdBytes()
        val spanId = generator.generateSpanIdBytes()
        val traceFlags = factory.traceFlagsFactory.default
        val traceState = factory.traceStateFactory.default
        val spanContext = factory.spanContextFactory.create(
            traceId,
            spanId,
            traceFlags,
            traceState
        )
        assertEquals(traceId.toHexString(), spanContext.traceIdBytes.toHexString())
        assertEquals(spanId.toHexString(), spanContext.spanIdBytes.toHexString())
    }
}
