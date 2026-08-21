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
    fun longAddAcceptsPositiveNegativeAndZero() {
        val counter = meter.createLongUpDownCounter("grocery.customers")
        counter.add(1)
        counter.add(-1)
        counter.add(0)
        counter.add(2) { setStringAttribute("account.type", "commercial") }
    }

    @Test
    fun createUsesNullUnitAndDescriptionByDefault() {
        val counter = meter.createLongUpDownCounter("grocery.customers")
        assertEquals("grocery.customers", counter.name)
        assertEquals(null, counter.unit)
        assertEquals(null, counter.description)
    }
}
