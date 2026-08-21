package io.opentelemetry.kotlin.behavior

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class LimitsTest {

    @Test
    fun keepsNonNegativeValues() {
        assertEquals(0, limitOrUnset(0))
        assertEquals(128, limitOrUnset(128))
        assertEquals(0, limitOrUnset(0L))
        assertEquals(128, limitOrUnset(128L))
        assertEquals(Int.MAX_VALUE, limitOrUnset(Int.MAX_VALUE.toLong()))
    }

    @Test
    fun leavesNegativeValuesUnset() {
        assertNull(limitOrUnset(-1))
        assertNull(limitOrUnset(-1L))
    }

    @Test
    fun leavesValuesTooLargeForAnIntUnset() {
        assertNull(limitOrUnset(Int.MAX_VALUE.toLong() + 1))
        assertNull(limitOrUnset(Long.MAX_VALUE))
    }

    @Test
    fun leavesUnsetValuesUnset() {
        assertNull(limitOrUnset(null as Int?))
        assertNull(limitOrUnset(null as Long?))
    }
}
