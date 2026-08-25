package io.opentelemetry.kotlin.config.dsl

import io.opentelemetry.kotlin.behavior.LogLimitsBehavior
import kotlin.test.Test
import kotlin.test.assertEquals

internal class LogLimitsConfigDslImplTest {

    @Test
    fun everyLimitStartsUnset() {
        assertEquals(LogLimitsBehavior(), LogLimitsConfigDslImpl().toBehavior())
    }

    @Test
    fun mapsEveryConfiguredLimit() {
        val dsl = LogLimitsConfigDslImpl().apply {
            attributeCountLimit = 64
            attributeValueLengthLimit = 256
        }

        assertEquals(
            LogLimitsBehavior(attributeCountLimit = 64, attributeValueLengthLimit = 256),
            dsl.toBehavior(),
        )
    }

    @Test
    fun preservesALimitOfZero() {
        val dsl = LogLimitsConfigDslImpl().apply { attributeCountLimit = 0 }

        assertEquals(0, dsl.toBehavior().attributeCountLimit)
    }

    @Test
    fun leavesNegativeLimitsUnset() {
        val dsl = LogLimitsConfigDslImpl().apply {
            attributeCountLimit = -1
            attributeValueLengthLimit = -1
        }

        assertEquals(LogLimitsBehavior(), dsl.toBehavior())
    }
}
