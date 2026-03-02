package io.opentelemetry.kotlin.tracing.ext

import io.opentelemetry.kotlin.tracing.FakeTraceState
import org.junit.Test
import kotlin.test.assertEquals

internal class TraceStateExtTest {

    @Test
    fun toOtelJavaTraceState() {
        val expected = FakeTraceState()
        val observed = expected.toOtelJavaTraceState()
        assertEquals(expected.asMap(), observed.asMap())
    }
}
