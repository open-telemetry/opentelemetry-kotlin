package io.opentelemetry.kotlin.metrics

import io.opentelemetry.kotlin.aliases.OtelJavaSdkMeterProvider
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.fakes.otel.java.FakeOtelJavaMetricReader
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNotSame
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

    @Test
    fun testDupeMeterProviderAttributes() {
        val first = adapter.getMeter(name = "name") {
            setStringAttribute("key", "value")
        }
        val second = adapter.getMeter(name = "name") {
            setStringAttribute("key", "value")
        }
        val third = adapter.getMeter(name = "name") {
            setStringAttribute("foo", "bar")
        }
        assertSame(first, second)
        assertNotEquals(first, third)
    }

    @Test
    fun testScopePropertyBoundaryCollision() {
        val first = adapter.getMeter(name = "ab", version = "c")
        val second = adapter.getMeter(name = "a", version = "bc")
        assertNotSame(first, second)
    }

    @Test
    fun testNullScopePropertyCollision() {
        val first = adapter.getMeter(name = "name")
        val second = adapter.getMeter(name = "name", version = "null")
        assertNotSame(first, second)
    }

    @Test
    fun testForceFlushAndShutdownDelegateToJavaSdkProvider() = runTest {
        val reader = FakeOtelJavaMetricReader()
        val provider = OtelJavaSdkMeterProvider.builder()
            .registerMetricReader(reader)
            .build()
        val adapter = MeterProviderAdapter(provider)

        assertEquals(OperationResultCode.Success, adapter.forceFlush())
        assertEquals(1, reader.flushCount)
        assertEquals(OperationResultCode.Success, adapter.shutdown())
        assertEquals(1, reader.shutdownCount)
    }
}
