package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaSdkMeterProvider
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalApi::class)
internal class MeterAdapterUpDownCounterTest {

    private val adapter = MeterProviderAdapter(OtelJavaSdkMeterProvider.builder().build())
        .getMeter("test")

    @Test
    fun createDoubleUpDownCounterDelegatesAdd() {
        val counter = adapter.createDoubleUpDownCounter(
            name = "queue.depth",
            unit = "{item}",
            description = "queue size",
        )
        assertEquals("queue.depth", counter.name)
        assertEquals("{item}", counter.unit)
        assertEquals("queue size", counter.description)
        counter.enabled()
        counter.add(1.0)
        counter.add(-1.0)
        counter.add(0.0)
        counter.add(2.5) { setStringAttribute("account.type", "residential") }
    }

    @Test
    fun createDoubleUpDownCounterOmitsNullUnitAndDescription() {
        val counter = adapter.createDoubleUpDownCounter("queue.depth")
        assertEquals("queue.depth", counter.name)
        assertEquals(null, counter.unit)
        assertEquals(null, counter.description)
        counter.add(1.0)
    }
}
