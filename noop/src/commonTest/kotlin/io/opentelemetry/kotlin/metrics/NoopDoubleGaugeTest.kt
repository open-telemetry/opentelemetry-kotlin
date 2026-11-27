package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ExperimentalApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

@OptIn(ExperimentalApi::class)
internal class NoopDoubleGaugeTest {

    @Test
    fun ignoresRecordsAndExposesPlaceholderMetadata() {
        val gauge = NoopMeter.createDoubleGauge(
            name = "temperature",
            unit = "Cel",
            description = "Current temperature",
        )

        gauge.record(21.5)
        gauge.record(22.0) { setStringAttribute("room", "server") }

        assertEquals("", gauge.name)
        assertEquals(null, gauge.unit)
        assertEquals(null, gauge.description)
        assertFalse(gauge.enabled())
    }
}
