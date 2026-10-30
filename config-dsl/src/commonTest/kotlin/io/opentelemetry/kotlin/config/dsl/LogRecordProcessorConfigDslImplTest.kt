package io.opentelemetry.kotlin.config.dsl

import io.opentelemetry.kotlin.behavior.BatchLogRecordProcessorBehavior
import io.opentelemetry.kotlin.behavior.LogRecordProcessorBehavior
import io.opentelemetry.kotlin.behavior.SimpleLogRecordProcessorBehavior
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class LogRecordProcessorConfigDslImplTest {

    @Test
    fun startsUnset() {
        assertNull(LogRecordProcessorConfigDslImpl().toBehavior())
    }

    @Test
    fun mapsSimple() {
        val dsl = LogRecordProcessorConfigDslImpl()
        dsl.simple()
        assertEquals(
            LogRecordProcessorBehavior(simple = SimpleLogRecordProcessorBehavior()),
            dsl.toBehavior(),
        )
    }

    @Test
    fun mapsBatch() {
        val dsl = LogRecordProcessorConfigDslImpl()
        dsl.batch()
        assertEquals(
            LogRecordProcessorBehavior(batch = BatchLogRecordProcessorBehavior()),
            dsl.toBehavior(),
        )
    }

    @Test
    fun mapsSimpleAndBatchTogether() {
        val dsl = LogRecordProcessorConfigDslImpl()
        dsl.simple()
        dsl.batch()
        assertEquals(
            LogRecordProcessorBehavior(
                simple = SimpleLogRecordProcessorBehavior(),
                batch = BatchLogRecordProcessorBehavior(),
            ),
            dsl.toBehavior(),
        )
    }
}
