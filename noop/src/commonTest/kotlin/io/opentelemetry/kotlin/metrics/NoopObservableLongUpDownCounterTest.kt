package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ExperimentalApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

@OptIn(ExperimentalApi::class)
internal class NoopObservableLongUpDownCounterTest {

    @Test
    fun ignoresCallbacksAndPreservesInstrumentMetadata() {
        var called = false
        val counter = NoopMeter.createObservableLongUpDownCounter(
            name = "process.memory.working_set",
            unit = "By",
            description = "Process working set size",
            callbacks = arrayOf({ called = true }),
        )

        counter.registerCallback { called = true }.close()
        counter.close()

        assertEquals("process.memory.working_set", counter.name)
        assertEquals("By", counter.unit)
        assertEquals("Process working set size", counter.description)
        assertFalse(called)
    }
}
