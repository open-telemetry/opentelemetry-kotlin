package io.opentelemetry.kotlin

import io.opentelemetry.kotlin.factory.CompatBaggageFactory
import org.junit.Assert
import org.junit.Assert.assertEquals
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
    fun `retrieve baggage factory`() {
        val sdk = createCompatOpenTelemetry()
        Assert.assertTrue(sdk.baggage is CompatBaggageFactory)

        val baggage = sdk.baggage.create {
            put("user", "alice")
            put("region", "eu", metadata = "secure")
        }
        assertEquals("alice", baggage.getValue("user"))
        assertEquals("eu", baggage.getValue("region"))
        assertEquals("secure", baggage.asMap()["region"]?.metadata?.value)
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
}
