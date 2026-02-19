package io.opentelemetry.kotlin.tracing.model

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.tracing.FakeTraceFlags
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalApi::class)
internal class TraceFlagsExtTest {

    @Test
    fun testEmptyString() {
        assertEquals("00", FakeTraceFlags().hex)
        assertEquals("01", FakeTraceFlags(isSampled = true).hex)
        assertEquals("02", FakeTraceFlags(isRandom = true).hex)
        assertEquals("03", FakeTraceFlags(isSampled = true, isRandom = true).hex)
    }
}
