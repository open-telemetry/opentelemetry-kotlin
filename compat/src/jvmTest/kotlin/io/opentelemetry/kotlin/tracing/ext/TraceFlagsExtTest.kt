package io.opentelemetry.kotlin.tracing.ext

import io.opentelemetry.kotlin.tracing.FakeTraceFlags
import io.opentelemetry.kotlin.tracing.model.hex
import org.junit.Assert.assertEquals
import org.junit.Test

internal class TraceFlagsExtTest {

    @Test
    fun toOtelJavaTraceFlags() {
        val combinations = listOf(
            FakeTraceFlags(isSampled = false, isRandom = false),
            FakeTraceFlags(isSampled = true, isRandom = false),
            FakeTraceFlags(isSampled = false, isRandom = true),
            FakeTraceFlags(isSampled = true, isRandom = true),
        )
        combinations.forEach { expected ->
            val observed = expected.toOtelJavaTraceFlags()
            assertEquals(expected.hex, observed.asHex())
            assertEquals(expected.isSampled, observed.isSampled)
        }
    }
}
