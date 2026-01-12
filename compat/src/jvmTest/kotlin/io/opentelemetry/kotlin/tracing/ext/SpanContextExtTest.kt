package io.opentelemetry.kotlin.tracing.ext

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.tracing.FakeSpanContext
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalApi::class)
internal class SpanContextExtTest {

    @Test
    fun toOtelJavaSpanContext() {
        val impl = FakeSpanContext.INVALID
        val spanContext = impl.toOtelJavaSpanContext()
        assertEquals(impl.spanId, spanContext.spanId)
        assertEquals(impl.traceId, spanContext.traceId)
    }
}
