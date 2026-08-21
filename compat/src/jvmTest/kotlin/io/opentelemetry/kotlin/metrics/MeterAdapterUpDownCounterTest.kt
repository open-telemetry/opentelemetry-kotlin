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
    fun createLongUpDownCounterDelegatesAdd() {
        val counter = adapter.createLongUpDownCounter(
            name = "grocery.customers",
            unit = "{customer}",
            description = "customers in store",
        )
        assertEquals("grocery.customers", counter.name)
        assertEquals("{customer}", counter.unit)
        assertEquals("customers in store", counter.description)
        counter.enabled()
        counter.add(1)
        counter.add(-1)
        counter.add(0)
        counter.add(2) { setStringAttribute("account.type", "commercial") }
    }

    @Test
    fun createLongUpDownCounterOmitsNullUnitAndDescription() {
        val counter = adapter.createLongUpDownCounter("grocery.customers")
        assertEquals("grocery.customers", counter.name)
        assertEquals(null, counter.unit)
        assertEquals(null, counter.description)
        counter.add(1)
    }
}
