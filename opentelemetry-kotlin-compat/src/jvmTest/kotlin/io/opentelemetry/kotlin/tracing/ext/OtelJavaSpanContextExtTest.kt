package io.opentelemetry.kotlin.tracing.ext

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaSpanContext
import io.opentelemetry.kotlin.aliases.OtelJavaTraceFlags
import io.opentelemetry.kotlin.aliases.OtelJavaTraceState
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalApi::class)
internal class OtelJavaSpanContextExtTest {

    @Test
    fun toOtelKotlinSpanContext() {
        val impl = OtelJavaSpanContext.create(
            "12345678901234567890123456789012",
            "1234567890123456",
            OtelJavaTraceFlags.getDefault(),
            OtelJavaTraceState.getDefault()
        )
        val spanContext = impl.toOtelKotlinSpanContext()
        assertEquals(impl.spanId, spanContext.spanId)
        assertEquals(impl.traceId, spanContext.traceId)
    }
}
