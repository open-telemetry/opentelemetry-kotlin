package io.opentelemetry.kotlin.init

import org.junit.Test
import kotlin.test.assertEquals

internal class LogLimitsConfigImplTest {

    @Test
    fun `test default`() {
        CompatLogLimitsConfig().apply {
            assertEquals(0, attributeCountLimit)
            assertEquals(0, attributeValueLengthLimit)
        }
    }

    @Test
    fun `test span limits`() {
        val cfg = CompatLogLimitsConfig()
        cfg.apply {
            attributeCountLimit = 11
            attributeValueLengthLimit = 111
        }
        val impl = cfg.build()
        assertEquals(11, impl.maxNumberOfAttributes)
        assertEquals(111, impl.maxAttributeValueLength)
    }
}
