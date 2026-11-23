package io.opentelemetry.kotlin.behavior

import io.opentelemetry.kotlin.error.SdkErrorSeverity
import io.opentelemetry.kotlin.logging.SeverityNumber
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

    @Test
    fun mapsEveryLevelOntoTheMinimumSdkErrorSeverity() {
        SeverityLevel.entries.forEach {
            val expected = when {
                it.name.startsWith("WARN") -> SdkErrorSeverity.WARNING
                it.name.startsWith("ERROR") || it.name.startsWith("FATAL") -> SdkErrorSeverity.ERROR
                else -> SdkErrorSeverity.INFO
            }
            assertEquals(expected, it.toMinimumSdkErrorSeverity(), it.name)
        }
    }

    @Test
    fun mapsEverySeverityNumberOntoTheMatchingLevel() {
        SeverityNumber.entries.filter { it != SeverityNumber.UNKNOWN }.forEach {
            assertEquals(it.name, it.toSeverityLevel()?.name)
        }
    }

    @Test
    fun unknownSeverityNumberHasNoLevel() {
        assertNull(SeverityNumber.UNKNOWN.toSeverityLevel())
    }
}
