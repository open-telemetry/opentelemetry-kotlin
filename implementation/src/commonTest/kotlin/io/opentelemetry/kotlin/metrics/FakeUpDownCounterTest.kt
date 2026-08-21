package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ExperimentalApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
internal class FakeUpDownCounterTest {

    @Test
    fun doubleAddRecordsValueAndAttributes() {
        val meter = FakeMeter("test")
        val counter = meter.createDoubleUpDownCounter(
            name = "queue.depth",
            unit = "{item}",
            description = "queue size",
        ) as FakeDoubleUpDownCounter

        counter.add(1.5)
        counter.add(-0.5) { setStringAttribute("material", "steel") }

        assertEquals("queue.depth", counter.name)
        assertEquals("{item}", counter.unit)
        assertEquals("queue size", counter.description)
        assertTrue(counter.enabled())
        assertEquals(listOf(1.5 to emptyMap(), -0.5 to mapOf("material" to "steel")), counter.adds)
        assertEquals(1, meter.doubleUpDownCounters.size)
    }

    @Test
    fun enabledResultCanChange() {
        val counter = FakeDoubleUpDownCounter("n")
        assertTrue(counter.enabled())
        counter.enabledResult = { false }
        assertFalse(counter.enabled())
    }
}
