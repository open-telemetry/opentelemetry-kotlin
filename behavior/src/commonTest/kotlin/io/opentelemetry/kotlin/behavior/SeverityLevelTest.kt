package io.opentelemetry.kotlin.behavior

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SeverityLevelTest {
    @Test
    fun correctlyMapsAllLevels() {
        SeverityLevel.entries.forEach {
            assertEquals(it, it.name.toSeverityLevel())
            assertEquals(it, it.name.lowercase().toSeverityLevel())
        }
    }

    @Test
    fun invalidNameReturnsNull() {
        assertNull("invalid".toSeverityLevel())
    }
}
