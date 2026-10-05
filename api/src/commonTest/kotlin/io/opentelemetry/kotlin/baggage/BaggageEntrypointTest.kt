package io.opentelemetry.kotlin.baggage

import io.opentelemetry.kotlin.ExperimentalApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame

@OptIn(ExperimentalApi::class)
internal class BaggageEntrypointTest {

    @Test
    fun `omitting the action returns the shared empty Baggage`() {
        assertSame(BaggageImpl.EMPTY, createBaggage())
        assertEquals(emptyMap(), createBaggage().asMap())
    }

    @Test
    fun `empty action returns the shared empty Baggage`() {
        assertSame(BaggageImpl.EMPTY, createBaggage { })
    }

    @Test
    fun `creates functional Baggage without an OpenTelemetry instance`() {
        val baggage = createBaggage {
            put("user", "alice", "propagation=public")
            put("region", "eu")
            remove("region")
        }
        assertEquals("alice", baggage.getValue("user"))
        assertEquals("propagation=public", baggage.asMap()["user"]?.metadata?.value)
        assertNull(baggage.getValue("region"))
    }

    @Test
    fun `put adds entries`() {
        val baggage = createBaggage {
            put("user", "alice")
            put("region", "eu")
        }
        assertEquals("alice", baggage.getValue("user"))
        assertEquals("eu", baggage.getValue("region"))
        assertEquals(2, baggage.asMap().size)
    }

    @Test
    fun `put without metadata uses empty metadata`() {
        val baggage = createBaggage {
            put("foo", "bar")
        }
        assertEquals("", baggage.asMap()["foo"]?.metadata?.value)
    }

    @Test
    fun `put replaces existing entry`() {
        val baggage = createBaggage {
            put("k", "old")
            put("k", "new")
        }
        assertEquals("new", baggage.getValue("k"))
        assertEquals(1, baggage.asMap().size)
    }

    @Test
    fun `remove of absent key is a no-op`() {
        val baggage = createBaggage {
            put("k", "v")
            remove("missing")
        }
        assertEquals("v", baggage.getValue("k"))
    }

    @Test
    fun `put with invalid key drops entry`() {
        val baggage = createBaggage {
            put("ok", "v")
            put("bad key", "v")
            put("", "v")
        }
        assertEquals(1, baggage.asMap().size)
        assertEquals("v", baggage.getValue("ok"))
        assertNull(baggage.getValue("bad key"))
    }

    @Test
    fun `put with invalid value drops entry`() {
        val baggage = createBaggage {
            put("ok", "v")
            put("crlf", "bad\rvalue")
            put("nul", "bad\u0000value")
        }
        assertEquals(1, baggage.asMap().size)
        assertNull(baggage.getValue("crlf"))
        assertNull(baggage.getValue("nul"))
    }

    @Test
    fun `put silently drops new entry beyond MAX_ENTRIES`() {
        val baggage = createBaggage {
            repeat(BaggageImpl.MAX_ENTRIES) { idx -> put("k$idx", "v") }
            put("kExtra", "v")
        }
        assertEquals(BaggageImpl.MAX_ENTRIES, baggage.asMap().size)
        assertNull(baggage.getValue("kExtra"))
    }

    @Test
    fun `put replaces existing key when at MAX_ENTRIES cap`() {
        val baggage = createBaggage {
            repeat(BaggageImpl.MAX_ENTRIES) { idx -> put("k$idx", "v") }
            put("k0", "new")
        }
        assertEquals(BaggageImpl.MAX_ENTRIES, baggage.asMap().size)
        assertEquals("new", baggage.getValue("k0"))
    }
}
