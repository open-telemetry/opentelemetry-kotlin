package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.baggage.BaggageImpl
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame

@OptIn(ExperimentalApi::class)
internal class BaggageFactoryImplTest {

    private val factory = BaggageFactoryImpl()

    @Test
    fun `empty returns the shared empty Baggage`() {
        assertSame(BaggageImpl.EMPTY, factory.empty())
        assertEquals(emptyMap(), factory.empty().asMap())
    }

    @Test
    fun `create with no operations returns empty`() {
        assertSame(BaggageImpl.EMPTY, factory.create { })
    }

    @Test
    fun `create put adds entries`() {
        val baggage = factory.create {
            put("user", "alice")
            put("region", "eu")
        }
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

    @Test
    fun `create remove of absent key is a no-op`() {
        val baggage = factory.create {
            put("k", "v")
            remove("missing")
        }
        assertEquals("v", baggage.getValue("k"))
    }
}
