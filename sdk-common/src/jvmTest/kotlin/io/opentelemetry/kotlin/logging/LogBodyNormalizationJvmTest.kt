package io.opentelemetry.kotlin.logging

import io.opentelemetry.kotlin.attributes.AnyValue
import kotlin.test.Test
import kotlin.test.assertEquals

internal class LogBodyNormalizationJvmTest {

    @Test
    fun testIntegralTypesAreWidened() {
        assertEquals(5L, normalizeLogBody(5))
        assertEquals(5L, normalizeLogBody(5.toShort()))
        assertEquals(5L, normalizeLogBody(5.toByte()))
    }

    @Test
    fun testNestedIntegralTypesAreWidened() {
        val expected = AnyValue.ListValue(
            listOf(AnyValue.LongValue(1), AnyValue.LongValue(2), AnyValue.LongValue(3))
        )
        assertEquals(expected, normalizeLogBody(listOf(1, 2.toShort(), 3.toByte())))
    }
}
