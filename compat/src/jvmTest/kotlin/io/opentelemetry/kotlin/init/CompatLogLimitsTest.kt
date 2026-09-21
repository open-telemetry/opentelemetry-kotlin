package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.aliases.OtelJavaLogLimits
import io.opentelemetry.kotlin.behavior.AttributeLimitsBehavior
import org.junit.Test
import kotlin.test.assertEquals

internal class CompatLogLimitsTest {

    @Test
    fun `unset limits fall through to the Java SDK defaults`() {
        assertEquals(OtelJavaLogLimits.getDefault(), AttributeLimitsBehavior().toOtelJavaLogLimits())
    }

    @Test
    fun `configured limits reach the Java SDK, zero included`() {
        with(AttributeLimitsBehavior(attributeCountLimit = 32, attributeValueLengthLimit = 128).toOtelJavaLogLimits()) {
            assertEquals(32, maxNumberOfAttributes)
            assertEquals(128, maxAttributeValueLength)
        }
        with(AttributeLimitsBehavior(attributeCountLimit = 0, attributeValueLengthLimit = 0).toOtelJavaLogLimits()) {
            assertEquals(0, maxNumberOfAttributes)
            assertEquals(0, maxAttributeValueLength)
        }
    }
}
