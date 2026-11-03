package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ExperimentalApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
internal class FakeLongGaugeTest {

    @Test
    fun recordStoresValueAndAttributes() {
        val meter = FakeMeter("test")
        val gauge = meter.createLongGauge(
            name = "queue.depth",
            unit = "{item}",
            description = "current queue depth",
        ) as FakeLongGauge

        gauge.record(3)
        gauge.record(7) { setStringAttribute("queue", "primary") }

        assertEquals("queue.depth", gauge.name)
        assertEquals("{item}", gauge.unit)
        assertEquals("current queue depth", gauge.description)
        assertTrue(gauge.enabled())
        assertEquals(
            listOf(3L to emptyMap(), 7L to mapOf("queue" to "primary")),
            gauge.records,
        )
        assertEquals(1, meter.longGauges.size)
    }
}
