package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.aliases.OtelJavaSpanLimits
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class SpanLimitsConfigImplTest {

    @Test
    fun `test default`() {
        CompatSpanLimitsConfig().apply {
            assertNull(eventCountLimit)
            assertNull(attributeCountLimit)
            assertNull(linkCountLimit)
            assertNull(attributeCountPerLinkLimit)
            assertNull(attributeCountPerEventLimit)
            assertNull(attributeValueLengthLimit)
        }
    }

    @Test
    fun `unset limits fall through to the Java SDK defaults`() {
        assertEquals(OtelJavaSpanLimits.getDefault(), CompatSpanLimitsConfig().build())
    }

    @Test
    fun `the limits the adapters enforce themselves resolve to a default`() {
        CompatSpanLimitsConfig().apply {
            assertEquals(DEFAULT_ATTR_LIMIT, effectiveAttributeCountLimit)
            assertEquals(DEFAULT_LINK_LIMIT, effectiveLinkCountLimit)
            assertEquals(DEFAULT_EVENT_LIMIT, effectiveEventCountLimit)
        }
    }

    @Test
    fun `a configured zero is not treated as unset`() {
        CompatSpanLimitsConfig().apply {
            attributeCountLimit = 0
            linkCountLimit = 0
            eventCountLimit = 0
            assertEquals(0, effectiveAttributeCountLimit)
            assertEquals(0, effectiveLinkCountLimit)
            assertEquals(0, effectiveEventCountLimit)
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
