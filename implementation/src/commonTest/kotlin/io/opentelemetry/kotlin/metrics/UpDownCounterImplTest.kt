package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.attributes.AttributesModel
import io.opentelemetry.kotlin.error.NoopSdkErrorHandler
import io.opentelemetry.kotlin.init.config.MetricsConfig
import io.opentelemetry.kotlin.resource.ResourceImpl
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
internal class UpDownCounterImplTest {

    private lateinit var meter: Meter

    @BeforeTest
    fun setup() {
        meter = MeterProviderImpl(
            MetricsConfig(
                resource = ResourceImpl(AttributesModel(), null),
                sdkErrorHandler = NoopSdkErrorHandler,
            )
        ).getMeter("test")
    }

    @Test
    fun createLongUpDownCounterExposesIdentity() {
        val counter = meter.createLongUpDownCounter(
            name = "store.inventory",
            unit = "{item}",
            description = "items in stock",
        )
        assertEquals("store.inventory", counter.name)
        assertEquals("{item}", counter.unit)
        assertEquals("items in stock", counter.description)
        assertTrue(counter.enabled())
    }

    @Test
    fun createDoubleUpDownCounterExposesIdentity() {
        val counter = meter.createDoubleUpDownCounter(
            name = "queue.depth",
            unit = "{item}",
            description = "queue size",
        )
        assertEquals("queue.depth", counter.name)
        assertEquals("{item}", counter.unit)
        assertEquals("queue size", counter.description)
        assertTrue(counter.enabled())
    }

    @Test
    fun longAddAcceptsPositiveNegativeAndZero() {
        val counter = meter.createLongUpDownCounter("grocery.customers")
        counter.add(1)
        counter.add(-1)
        counter.add(0)
        counter.add(2) { setStringAttribute("account.type", "commercial") }
    }

    @Test
    fun doubleAddAcceptsPositiveNegativeAndZero() {
        val counter = meter.createDoubleUpDownCounter("grocery.customers")
        counter.add(1.5)
        counter.add(-1.5)
        counter.add(0.0)
        counter.add(2.0) { setStringAttribute("account.type", "residential") }
    }

    @Test
    fun createUsesNullUnitAndDescriptionByDefault() {
        val longCounter = meter.createLongUpDownCounter("grocery.customers")
        assertEquals("grocery.customers", longCounter.name)
        assertEquals(null, longCounter.unit)
        assertEquals(null, longCounter.description)

        val doubleCounter = meter.createDoubleUpDownCounter("grocery.customers")
        assertEquals("grocery.customers", doubleCounter.name)
        assertEquals(null, doubleCounter.unit)
        assertEquals(null, doubleCounter.description)
    }
}
