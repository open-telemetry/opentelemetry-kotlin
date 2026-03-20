package io.opentelemetry.kotlin.init

import org.junit.Test
import kotlin.test.assertEquals

internal class SpanLimitsConfigImplTest {

    @Test
    fun `test default`() {
        CompatSpanLimitsConfig().apply {
            assertEquals(0, eventCountLimit)
            assertEquals(0, attributeCountLimit)
            assertEquals(0, linkCountLimit)
            assertEquals(0, attributeCountPerLinkLimit)
            assertEquals(0, attributeCountPerEventLimit)
            assertEquals(0, attributeValueLengthLimit)
        }
    }

    @Test
    fun `test span limits`() {
        val cfg = CompatSpanLimitsConfig()
        cfg.apply {
            eventCountLimit = 1
            attributeCountLimit = 2
            linkCountLimit = 3
            attributeCountPerLinkLimit = 4
            attributeCountPerEventLimit = 5
            attributeValueLengthLimit = 6
        }
        val impl = cfg.build()
        assertEquals(1, impl.maxNumberOfEvents)
        assertEquals(2, impl.maxNumberOfAttributes)
        assertEquals(3, impl.maxNumberOfLinks)
        assertEquals(4, impl.maxNumberOfAttributesPerLink)
        assertEquals(5, impl.maxNumberOfAttributesPerEvent)
        assertEquals(6, impl.maxAttributeValueLength)
    }
}
