package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaBaggage
import io.opentelemetry.kotlin.baggage.BaggageAdapter
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
internal class CompatBaggageFactoryTest {

    private val factory = CompatBaggageFactory()

    @Test
    fun `empty wraps OtelJavaBaggage empty`() {
        val baggage = factory.empty()
        assertTrue(baggage is BaggageAdapter)
        assertEquals(OtelJavaBaggage.empty(), baggage.impl)
        assertEquals(emptyMap(), baggage.asMap())
    }

    @Test
    fun `create put adds entries via java builder`() {
        val baggage = factory.create {
            put("user", "alice")
            put("region", "eu")
        }
        assertTrue(baggage is BaggageAdapter)
        assertEquals("alice", baggage.getValue("user"))
        assertEquals("eu", baggage.getValue("region"))
        assertEquals(2, baggage.asMap().size)
    }

    @Test
    fun `create put with metadata propagates string`() {
        val baggage = factory.create {
            put("foo", "bar", metadata = "secure")
        }
        assertEquals("secure", baggage.asMap()["foo"]?.metadata?.value)
    }

    @Test
    fun `create put without metadata uses empty metadata`() {
        val baggage = factory.create {
            put("foo", "bar")
        }
        assertEquals("", baggage.asMap()["foo"]?.metadata?.value)
    }

    @Test
    fun `create put replaces existing entry`() {
        val baggage = factory.create {
            put("k", "old")
            put("k", "new")
        }
        assertEquals("new", baggage.getValue("k"))
        assertEquals(1, baggage.asMap().size)
    }

    @Test
    fun `create remove deletes prior put`() {
        val baggage = factory.create {
            put("k", "v")
            remove("k")
        }
        assertNull(baggage.getValue("k"))
    }
}
