package io.opentelemetry.kotlin.config.dsl.logging

import io.opentelemetry.kotlin.behavior.logging.BatchLogRecordProcessorBehavior
import io.opentelemetry.kotlin.behavior.logging.LogRecordProcessorBehavior
import io.opentelemetry.kotlin.behavior.logging.SimpleLogRecordProcessorBehavior
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class LoggerProviderConfigDslImplTest {

    @Test
    fun processorStartsUnset() {
        assertNull(LoggerProviderConfigDslImpl().toBehavior().processor)
    }

    @Test
    fun exportCallSetsProcessor() {
        val dsl = LoggerProviderConfigDslImpl()
        dsl.export { error("behavior mapping does not run the export lambda") }

        assertEquals(
            LogRecordProcessorBehavior(),
            dsl.toBehavior().processor,
        )
    }

    @Test
    fun processorCallSetsSimple() {
        val dsl = LoggerProviderConfigDslImpl()
        dsl.processor { simple {} }

        assertEquals(
            LogRecordProcessorBehavior(simple = SimpleLogRecordProcessorBehavior()),
            dsl.toBehavior().processor,
        )
    }

    @Test
    fun processorCallSetsBatch() {
        val dsl = LoggerProviderConfigDslImpl()
        dsl.processor { batch {} }

        assertEquals(
            LogRecordProcessorBehavior(batch = BatchLogRecordProcessorBehavior()),
            dsl.toBehavior().processor,
        )
    }

    @Test
    fun exportKeepsProcessorAlreadyChosen() {
        val dsl = LoggerProviderConfigDslImpl()
        dsl.processor { simple {} }
        dsl.export { error("behavior mapping does not run the export lambda") }

        assertEquals(
            LogRecordProcessorBehavior(simple = SimpleLogRecordProcessorBehavior()),
            dsl.toBehavior().processor,
        )
    }
}
