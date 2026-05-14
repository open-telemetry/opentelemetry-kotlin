package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.clock.FakeClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertSame

internal class CompatMeterProviderConfigTest {

    private val clock = FakeClock()

    @Test
    fun `default service name is unknown_service`() {
        val cfg = CompatMeterProviderConfig(clock)
        assertEquals("unknown_service", cfg.serviceName)
    }

    @Test
    fun `serviceName setter updates getter`() {
        val cfg = CompatMeterProviderConfig(clock).apply {
            serviceName = "my-service"
        }
        assertEquals("my-service", cfg.serviceName)
    }

    @Test
    fun `built MeterProvider caches meters by scope name`() {
        val provider = CompatMeterProviderConfig(clock).build(clock)
        val first = provider.getMeter("name")
        val second = provider.getMeter("name")
        val third = provider.getMeter("other")
        assertSame(first, second)
        assertNotEquals(first, third)
    }

    @Test
    fun `built MeterProvider caches meters by version`() {
        val provider = CompatMeterProviderConfig(clock).build(clock)
        val first = provider.getMeter(name = "name", version = "0.1.0")
        val second = provider.getMeter(name = "name", version = "0.1.0")
        val third = provider.getMeter(name = "name", version = "0.2.0")
        assertSame(first, second)
        assertNotEquals(first, third)
    }

    @Test
    fun `built MeterProvider caches meters by schemaUrl`() {
        val provider = CompatMeterProviderConfig(clock).build(clock)
        val first = provider.getMeter(name = "name", schemaUrl = "https://example.com/foo")
        val second = provider.getMeter(name = "name", schemaUrl = "https://example.com/foo")
        val third = provider.getMeter(name = "name", schemaUrl = "https://example.com/bar")
        assertSame(first, second)
        assertNotEquals(first, third)
    }
}
