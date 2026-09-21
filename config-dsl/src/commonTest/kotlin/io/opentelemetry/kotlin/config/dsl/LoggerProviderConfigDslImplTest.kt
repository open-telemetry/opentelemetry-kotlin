package io.opentelemetry.kotlin.config.dsl

import io.opentelemetry.kotlin.behavior.LogRecordProcessorBehavior
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
}
