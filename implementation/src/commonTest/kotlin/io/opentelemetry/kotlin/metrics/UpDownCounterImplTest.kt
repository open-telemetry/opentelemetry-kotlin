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
    fun doubleAddAcceptsPositiveNegativeAndZero() {
        val counter = meter.createDoubleUpDownCounter("grocery.customers")
        counter.add(1.5)
        counter.add(-1.5)
        counter.add(0.0)
        counter.add(2.0) { setStringAttribute("account.type", "residential") }
    }

    @Test
    fun createUsesNullUnitAndDescriptionByDefault() {
        val counter = meter.createDoubleUpDownCounter("grocery.customers")
        assertEquals("grocery.customers", counter.name)
        assertEquals(null, counter.unit)
        assertEquals(null, counter.description)
    }
}
