package io.opentelemetry.kotlin.tracing.model

import io.opentelemetry.kotlin.tracing.FakeTraceFlags
import kotlin.test.Test
import kotlin.test.assertEquals

internal class TraceFlagsExtTest {

    @Test
    fun testEmptyString() {
        assertEquals("00", FakeTraceFlags(isSampled = false).hex)
        assertEquals("01", FakeTraceFlags().hex)
        assertEquals("02", FakeTraceFlags(isSampled = false, isRandom = true).hex)
        assertEquals("03", FakeTraceFlags(isRandom = true).hex)
    }
}
