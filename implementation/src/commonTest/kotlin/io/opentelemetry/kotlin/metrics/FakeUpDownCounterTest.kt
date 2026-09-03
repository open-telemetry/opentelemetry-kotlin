package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ExperimentalApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
internal class FakeUpDownCounterTest {

    @Test
    fun longAddRecordsValueAndAttributes() {
        val meter = FakeMeter("test")
        val counter = meter.createLongUpDownCounter(
            name = "store.inventory",
            unit = "{item}",
            description = "items in stock",
        ) as FakeLongUpDownCounter

        counter.add(1)
        counter.add(-1) { setStringAttribute("color", "red") }

        assertEquals("store.inventory", counter.name)
        assertEquals("{item}", counter.unit)
        assertEquals("items in stock", counter.description)
        assertTrue(counter.enabled())
        assertEquals(listOf(1L to emptyMap(), -1L to mapOf("color" to "red")), counter.adds)
        assertEquals(1, meter.longUpDownCounters.size)
    }

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
        val longCounter = FakeLongUpDownCounter("n")
        assertTrue(longCounter.enabled())
        longCounter.enabledResult = { false }
        assertFalse(longCounter.enabled())

        val doubleCounter = FakeDoubleUpDownCounter("n")
        assertTrue(doubleCounter.enabled())
        doubleCounter.enabledResult = { false }
        assertFalse(doubleCounter.enabled())
    }
}
