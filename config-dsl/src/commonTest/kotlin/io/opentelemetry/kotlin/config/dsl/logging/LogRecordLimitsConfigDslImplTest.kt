package io.opentelemetry.kotlin.config.dsl.logging

import io.opentelemetry.kotlin.behavior.logging.LogRecordLimitsBehavior
import kotlin.test.Test
import kotlin.test.assertEquals

internal class LogRecordLimitsConfigDslImplTest {

    @Test
    fun everyLimitStartsUnset() {
        assertEquals(LogRecordLimitsBehavior(), LogRecordLimitsConfigDslImpl().toBehavior())
    }

    @Test
    fun mapsEveryConfiguredLimit() {
        val dsl = LogRecordLimitsConfigDslImpl().apply {
            attributeCountLimit = 64
            attributeValueLengthLimit = 256
        }

        assertEquals(
            LogRecordLimitsBehavior(attributeCountLimit = 64, attributeValueLengthLimit = 256),
            dsl.toBehavior(),
        )
    }

    @Test
    fun preservesALimitOfZero() {
        val dsl = LogRecordLimitsConfigDslImpl().apply { attributeCountLimit = 0 }

        assertEquals(0, dsl.toBehavior().attributeCountLimit)
    }

    @Test
    fun leavesNegativeLimitsUnset() {
        val dsl = LogRecordLimitsConfigDslImpl().apply {
            attributeCountLimit = -1
            attributeValueLengthLimit = -1
        }

        assertEquals(LogRecordLimitsBehavior(), dsl.toBehavior())
    }
}
