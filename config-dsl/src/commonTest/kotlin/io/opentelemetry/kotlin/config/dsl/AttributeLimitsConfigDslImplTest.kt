package io.opentelemetry.kotlin.config.dsl

import io.opentelemetry.kotlin.behavior.AttributeLimitsBehavior
import kotlin.test.Test
import kotlin.test.assertEquals

internal class AttributeLimitsConfigDslImplTest {

    @Test
    fun everyLimitStartsUnset() {
        assertEquals(AttributeLimitsBehavior(), AttributeLimitsConfigDslImpl().toBehavior())
    }

    @Test
    fun mapsEveryConfiguredLimit() {
        val dsl = AttributeLimitsConfigDslImpl().apply {
            attributeCountLimit = 64
            attributeValueLengthLimit = 256
        }
        assertEquals(
            AttributeLimitsBehavior(attributeCountLimit = 64, attributeValueLengthLimit = 256),
            dsl.toBehavior(),
        )
    }

    @Test
    fun preservesALimitOfZero() {
        val dsl = AttributeLimitsConfigDslImpl().apply { attributeCountLimit = 0 }
        assertEquals(0, dsl.toBehavior().attributeCountLimit)
    }

    @Test
    fun leavesNegativeLimitsUnset() {
        val dsl = AttributeLimitsConfigDslImpl().apply {
            attributeCountLimit = -1
            attributeValueLengthLimit = -1
        }
        assertEquals(AttributeLimitsBehavior(), dsl.toBehavior())
    }
}
