package io.opentelemetry.kotlin.logging

import kotlin.test.Test
import kotlin.test.assertEquals

internal class SeverityNumberTest {

    @Test
    fun testFromValueKnown() {
        assertEquals(SeverityNumber.INFO, SeverityNumber.fromValue(9))
        SeverityNumber.entries.forEach {
            assertEquals(it, SeverityNumber.fromValue(it.value))
        }
    }

    @Test
    fun testFromValueUnknown() {
        assertEquals(SeverityNumber.UNKNOWN, SeverityNumber.fromValue(99))
        assertEquals(SeverityNumber.UNKNOWN, SeverityNumber.fromValue(25))
        assertEquals(SeverityNumber.UNKNOWN, SeverityNumber.fromValue(-1))
    }
}
