package io.opentelemetry.kotlin

import org.junit.Assert.assertNotSame
import org.junit.Assert.assertSame
import org.junit.Test

@OptIn(ExperimentalApi::class)
internal class OpenTelemetrySdkTest {

    @Test
    fun `retrieve tracer provider`() {
        val sdk = createCompatOpenTelemetry()
        val provider = sdk.tracerProvider
        val a = provider.getTracer("test")
        val b = provider.getTracer("test")
        val c = provider.getTracer("test", "1.0.0") {
            setStringAttribute("key", "value")
        }
        val d = provider.getTracer("another")
        assertSame(a, b)
        assertNotSame(b, c)
        assertNotSame(c, d)
    }

    @Test
    fun `retrieve logger provider`() {
        val sdk = createCompatOpenTelemetry()
        val provider = sdk.loggerProvider
        val a = provider.getLogger("test")
        val b = provider.getLogger("test")
        val c = provider.getLogger("test", "1.0.0") {
            setStringAttribute("key", "value")
        }
        val d = provider.getLogger("another")
        assertSame(a, b)
        assertNotSame(b, c)
        assertNotSame(c, d)
    }

    @Test
    fun `retrieve meter provider`() {
        val sdk = createCompatOpenTelemetry()
        val provider = sdk.meterProvider
        val a = provider.getMeter("test")
        val b = provider.getMeter("test")
        val c = provider.getMeter("test", "1.0.0")
        val d = provider.getMeter("another")
        assertSame(a, b)
        assertNotSame(b, c)
        assertNotSame(c, d)
    }
}
