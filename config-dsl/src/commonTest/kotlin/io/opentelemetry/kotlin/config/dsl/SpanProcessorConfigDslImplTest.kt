package io.opentelemetry.kotlin.config.dsl

import io.opentelemetry.kotlin.behavior.BatchSpanProcessorBehavior
import io.opentelemetry.kotlin.behavior.SimpleSpanProcessorBehavior
import io.opentelemetry.kotlin.behavior.SpanProcessorBehavior
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class SpanProcessorConfigDslImplTest {

    @Test
    fun startsUnset() {
        assertNull(SpanProcessorConfigDslImpl().toBehavior())
    }

    @Test
    fun mapsSimple() {
        val dsl = SpanProcessorConfigDslImpl()
        dsl.simple()
        assertEquals(
            SpanProcessorBehavior(simple = SimpleSpanProcessorBehavior()),
            dsl.toBehavior(),
        )
    }

    @Test
    fun mapsBatch() {
        val dsl = SpanProcessorConfigDslImpl()
        dsl.batch()
        assertEquals(
            SpanProcessorBehavior(batch = BatchSpanProcessorBehavior()),
            dsl.toBehavior(),
        )
    }

    @Test
    fun mapsSimpleAndBatchTogether() {
        val dsl = SpanProcessorConfigDslImpl()
        dsl.simple()
        dsl.batch()
        assertEquals(
            SpanProcessorBehavior(
                simple = SimpleSpanProcessorBehavior(),
                batch = BatchSpanProcessorBehavior(),
            ),
            dsl.toBehavior(),
        )
    }
}
