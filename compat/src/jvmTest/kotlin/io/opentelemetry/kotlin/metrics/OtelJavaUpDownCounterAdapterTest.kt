package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaAttributes
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
internal class OtelJavaUpDownCounterAdapterTest {

    @Test
    fun upDownCounterBuilderBuildRecordsOnKotlinMeter() {
        val meter = FakeMeter("test")
        val javaCounter = OtelJavaMeterAdapter(meter)
            .upDownCounterBuilder("grocery.customers")
            .setUnit("{customer}")
            .setDescription("customers in store")
            .build()

        assertTrue(javaCounter.isEnabled)
        javaCounter.add(1)
        javaCounter.add(-1, OtelJavaAttributes.builder().put("account.type", "commercial").build())

        val fake = meter.longUpDownCounters.single()
        assertEquals("grocery.customers", fake.name)
        assertEquals("{customer}", fake.unit)
        assertEquals("customers in store", fake.description)
        assertEquals(1L, fake.adds[0].first)
        assertEquals(-1L, fake.adds[1].first)
        assertEquals("commercial", fake.adds[1].second["account.type"])
    }

    @Test
    fun ofDoublesBuildRecordsOnKotlinMeter() {
        val meter = FakeMeter("test")
        OtelJavaMeterAdapter(meter)
            .upDownCounterBuilder("queue.depth")
            .ofDoubles()
            .setUnit("{item}")
            .build()
            .add(-2.5)

        val fake = meter.doubleUpDownCounters.single()
        assertEquals("queue.depth", fake.name)
        assertEquals("{item}", fake.unit)
        assertEquals(-2.5, fake.adds.single().first)
    }

    @Test
    fun builderSettersReturnThis() {
        val builder = OtelJavaLongUpDownCounterBuilderAdapter(FakeMeter("test"), "n")
        assertSame(builder, builder.setUnit("1"))
        assertSame(builder, builder.setDescription("d"))
    }

    @Test
    fun isEnabledDelegates() {
        val fake = FakeLongUpDownCounter("n")
        val adapter = OtelJavaLongUpDownCounterAdapter(fake)
        assertTrue(adapter.isEnabled)
        fake.enabledResult = { false }
        assertFalse(adapter.isEnabled)
    }
}
