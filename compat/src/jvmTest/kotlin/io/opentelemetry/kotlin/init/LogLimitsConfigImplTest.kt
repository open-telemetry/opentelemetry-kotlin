package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.aliases.OtelJavaLogLimits
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class LogLimitsConfigImplTest {

    @Test
    fun `test default`() {
        CompatLogLimitsConfig().apply {
            assertNull(attributeCountLimit)
            assertNull(attributeValueLengthLimit)
        }
    }

    @Test
    fun `unset limits fall through to the Java SDK defaults`() {
        assertEquals(OtelJavaLogLimits.getDefault(), CompatLogLimitsConfig().build())
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
