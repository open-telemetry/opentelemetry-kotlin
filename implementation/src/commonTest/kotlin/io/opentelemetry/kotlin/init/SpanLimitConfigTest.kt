package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.attributes.DEFAULT_ATTRIBUTE_LIMIT
import io.opentelemetry.kotlin.attributes.DEFAULT_ATTRIBUTE_VALUE_LENGTH_LIMIT
import io.opentelemetry.kotlin.behavior.SpanLimitsBehavior
import io.opentelemetry.kotlin.init.config.DEFAULT_EVENT_LIMIT
import io.opentelemetry.kotlin.init.config.DEFAULT_LINK_LIMIT
import io.opentelemetry.kotlin.init.config.SpanLimitConfig
import kotlin.test.Test
import kotlin.test.assertEquals

internal class SpanLimitConfigTest {

    @Test
    fun testUnsetBehaviorUsesDefaults() {
        assertDefaults(SpanLimitConfig(SpanLimitsBehavior()))
    }

    @Test
    fun testNegativeLimitsUseDefaults() {
        val cfg = SpanLimitConfig(
            SpanLimitsBehavior(
                attributeCountLimit = -1,
                attributeValueLengthLimit = -1,
                linkCountLimit = -1,
                eventCountLimit = -1,
                attributeCountPerEventLimit = -1,
                attributeCountPerLinkLimit = -1,
            )
        )
        assertDefaults(cfg)
    }

    @Test
    fun testBehaviorOverridesDefaults() {
        val cfg = SpanLimitConfig(
            SpanLimitsBehavior(
                attributeCountLimit = 300,
                attributeValueLengthLimit = 600,
                linkCountLimit = 100,
                eventCountLimit = 200,
                attributeCountPerEventLimit = 500,
                attributeCountPerLinkLimit = 400,
            )
        )
        with(cfg) {
            assertEquals(300, attributeCountLimit)
            assertEquals(600, attributeValueLengthLimit)
            assertEquals(100, linkCountLimit)
            assertEquals(200, eventCountLimit)
            assertEquals(500, attributeCountPerEventLimit)
            assertEquals(400, attributeCountPerLinkLimit)
        }
    }

    private fun assertDefaults(cfg: SpanLimitConfig) {
        with(cfg) {
            assertEquals(DEFAULT_ATTRIBUTE_LIMIT, attributeCountLimit)
            assertEquals(DEFAULT_ATTRIBUTE_VALUE_LENGTH_LIMIT, attributeValueLengthLimit)
            assertEquals(DEFAULT_LINK_LIMIT, linkCountLimit)
            assertEquals(DEFAULT_EVENT_LIMIT, eventCountLimit)
            assertEquals(DEFAULT_ATTRIBUTE_LIMIT, attributeCountPerEventLimit)
            assertEquals(DEFAULT_ATTRIBUTE_LIMIT, attributeCountPerLinkLimit)
        }
    }
}
