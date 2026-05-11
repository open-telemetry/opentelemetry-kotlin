package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.aliases.OtelJavaSdkMeterProvider
import kotlin.test.Test
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertSame

internal class MeterProviderAdapterTest {

    private val adapter = MeterProviderAdapter(OtelJavaSdkMeterProvider.builder().build())

    @Test
    fun testMinimalMeterProvider() {
        assertNotNull(adapter.getMeter(name = ""))
    }

    @Test
    fun testDupeMeterProviderName() {
        val first = adapter.getMeter(name = "name")
        val second = adapter.getMeter(name = "name")
        val third = adapter.getMeter(name = "other")
        assertSame(first, second)
        assertNotEquals(first, third)
    }

    @Test
    fun testDupeMeterProviderVersion() {
        val first = adapter.getMeter(name = "name", version = "0.1.0")
        val second = adapter.getMeter(name = "name", version = "0.1.0")
        val third = adapter.getMeter(name = "name", version = "0.2.0")
        assertSame(first, second)
        assertNotEquals(first, third)
    }

    @Test
    fun testDupeMeterProviderSchemaUrl() {
        val first = adapter.getMeter(name = "name", schemaUrl = "https://example.com/foo")
        val second = adapter.getMeter(name = "name", schemaUrl = "https://example.com/foo")
        val third = adapter.getMeter(name = "name", schemaUrl = "https://example.com/bar")
        assertSame(first, second)
        assertNotEquals(first, third)
    }
}
