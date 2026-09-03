package io.opentelemetry.kotlin.behavior

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class LoggerProviderBehaviorTest {

    @Test
    fun logLimitsStartUnset() {
        assertNull(LoggerProviderBehavior().logLimits)
    }

    @Test
    fun mergesLogLimitsWhenBothLayersSuppliedThem() {
        val merged = LoggerProviderBehavior(
            logLimits = LogLimitsBehavior(attributeCountLimit = 1, attributeValueLengthLimit = 3),
        ).mergeWith(
            LoggerProviderBehavior(logLimits = LogLimitsBehavior(attributeValueLengthLimit = 99)),
        )

        assertEquals(1, merged.logLimits?.attributeCountLimit)
        assertEquals(99, merged.logLimits?.attributeValueLengthLimit)
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
