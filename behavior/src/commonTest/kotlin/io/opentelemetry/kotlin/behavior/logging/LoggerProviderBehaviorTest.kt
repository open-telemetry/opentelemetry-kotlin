package io.opentelemetry.kotlin.behavior.logging

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class LoggerProviderBehaviorTest {

    @Test
    fun logRecordLimitsStartUnset() {
        assertNull(LoggerProviderBehavior().logRecordLimits)
    }

    @Test
    fun mergesLogRecordLimitsWhenBothLayersSuppliedThem() {
        val merged = LoggerProviderBehavior(
            logRecordLimits = LogRecordLimitsBehavior(attributeCountLimit = 1, attributeValueLengthLimit = 3),
        ).mergeWith(
            LoggerProviderBehavior(logRecordLimits = LogRecordLimitsBehavior(attributeValueLengthLimit = 99)),
        )

        assertEquals(1, merged.logRecordLimits?.attributeCountLimit)
        assertEquals(99, merged.logRecordLimits?.attributeValueLengthLimit)
    }

    @Test
    fun processorStartsUnset() {
        assertNull(LoggerProviderBehavior().processor)
    }

    @Test
    fun staysUnsetWhenNeitherLayerConfiguredProcessor() {
        assertNull(LoggerProviderBehavior().mergeWith(LoggerProviderBehavior()).processor)
    }

    @Test
    fun adoptsProcessorFromWhicheverLayerSuppliedIt() {
        val processor = LogRecordProcessorBehavior()

        assertEquals(
            processor,
            LoggerProviderBehavior().mergeWith(LoggerProviderBehavior(processor = processor)).processor,
        )
        assertEquals(
            processor,
            LoggerProviderBehavior(processor = processor).mergeWith(LoggerProviderBehavior()).processor,
        )
    }
}
