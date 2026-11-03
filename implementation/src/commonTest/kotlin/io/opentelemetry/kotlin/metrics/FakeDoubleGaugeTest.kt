package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ExperimentalApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
internal class FakeDoubleGaugeTest {

    @Test
    fun recordStoresValueAndAttributes() {
        val meter = FakeMeter("test")
        val gauge = meter.createDoubleGauge(
            name = "temperature",
            unit = "Cel",
            description = "current temperature",
        ) as FakeDoubleGauge

        gauge.record(21.5)
        gauge.record(22.0) { setStringAttribute("room", "server") }

        assertEquals("temperature", gauge.name)
        assertEquals("Cel", gauge.unit)
        assertEquals("current temperature", gauge.description)
        assertTrue(gauge.enabled())
        assertEquals(
            listOf(21.5 to emptyMap(), 22.0 to mapOf("room" to "server")),
            gauge.records,
        )
        assertEquals(1, meter.doubleGauges.size)
    }
}
