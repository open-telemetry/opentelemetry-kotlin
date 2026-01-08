package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.ExperimentalApi
import org.junit.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalApi::class)
internal class SpanLimitsConfigImplTest {

    @Test
    fun `test default`() {
        CompatSpanLimitsConfig().apply {
            assertEquals(0, eventCountLimit)
            assertEquals(0, attributeCountLimit)
            assertEquals(0, linkCountLimit)
            assertEquals(0, attributeCountPerLinkLimit)
            assertEquals(0, attributeCountPerEventLimit)
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
        }
        val impl = cfg.build()
        assertEquals(1, impl.maxNumberOfEvents)
        assertEquals(2, impl.maxNumberOfAttributes)
        assertEquals(3, impl.maxNumberOfLinks)
        assertEquals(4, impl.maxNumberOfAttributesPerLink)
        assertEquals(5, impl.maxNumberOfAttributesPerEvent)
    }
}
