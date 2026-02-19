package io.opentelemetry.kotlin.tracing.ext

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.tracing.FakeTraceFlags
import io.opentelemetry.kotlin.tracing.model.hex
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalApi::class)
internal class TraceFlagsExtTest {

    @Test
    fun toOtelJavaTraceFlags() {
        val expected = FakeTraceFlags()
        val observed = expected.toOtelJavaTraceFlags()
        assertEquals(expected.hex, observed.asHex())
    }
}
