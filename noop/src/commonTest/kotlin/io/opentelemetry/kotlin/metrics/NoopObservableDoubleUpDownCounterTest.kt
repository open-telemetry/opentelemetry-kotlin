package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ExperimentalApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

@OptIn(ExperimentalApi::class)
internal class NoopObservableDoubleUpDownCounterTest {

    @Test
    fun ignoresCallbacksAndPreservesInstrumentMetadata() {
        var called = false
        val counter = NoopMeter.createObservableDoubleUpDownCounter(
            name = "process.cpu.load",
            unit = "1",
            description = "Process CPU load",
            callbacks = arrayOf({ called = true }),
        )

        counter.registerCallback { called = true }.close()
        counter.close()

        assertEquals("process.cpu.load", counter.name)
        assertEquals("1", counter.unit)
        assertEquals("Process CPU load", counter.description)
        assertFalse(called)
    }
}
