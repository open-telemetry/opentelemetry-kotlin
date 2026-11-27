package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ExperimentalApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

@OptIn(ExperimentalApi::class)
internal class NoopLongGaugeTest {

    @Test
    fun ignoresRecordsAndExposesPlaceholderMetadata() {
        val gauge = NoopMeter.createLongGauge(
            name = "queue.depth",
            unit = "{item}",
            description = "Current queue depth",
        )

        gauge.record(10)
        gauge.record(20) { setStringAttribute("queue", "primary") }

        assertEquals("", gauge.name)
        assertEquals(null, gauge.unit)
        assertEquals(null, gauge.description)
        assertFalse(gauge.enabled())
    }
}
