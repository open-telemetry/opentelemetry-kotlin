package io.opentelemetry.kotlin

import org.junit.Assert
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
        Assert.assertSame(a, b)
        Assert.assertNotSame(b, c)
        Assert.assertNotSame(c, d)
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
        Assert.assertSame(a, b)
        Assert.assertNotSame(b, c)
        Assert.assertNotSame(c, d)
    }
}
