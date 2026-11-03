package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ExperimentalApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

@OptIn(ExperimentalApi::class)
internal class NoopLongGaugeTest {

    @Test
    fun ignoresRecordsAndPreservesMetadata() {
        val gauge = NoopMeter.createLongGauge(
            name = "queue.depth",
            unit = "{item}",
            description = "Current queue depth",
        )

        gauge.record(10)
        gauge.record(20) { setStringAttribute("queue", "primary") }

        assertEquals("queue.depth", gauge.name)
        assertEquals("{item}", gauge.unit)
        assertEquals("Current queue depth", gauge.description)
        assertFalse(gauge.enabled())
    }
}
