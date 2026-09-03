package io.opentelemetry.kotlin.config.dsl

import io.opentelemetry.kotlin.behavior.SpanProcessorBehavior
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class TracerProviderConfigDslImplTest {

    @Test
    fun processorStartsUnset() {
        assertNull(TracerProviderConfigDslImpl().toBehavior().processor)
    }

    @Test
    fun exportCallSetsProcessor() {
        val dsl = TracerProviderConfigDslImpl()
        dsl.export { error("behavior mapping does not run the export lambda") }

        assertEquals(
            SpanProcessorBehavior(),
            dsl.toBehavior().processor,
        )
    }
}
