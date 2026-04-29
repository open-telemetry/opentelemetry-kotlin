package io.opentelemetry.kotlin.baggage

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.factory.ContextFactoryImpl
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotSame
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
internal class BaggageContextStorageTest {

    private val factory = ContextFactoryImpl()

    @Test
    fun `extractBaggage on root returns empty baggage`() {
        assertSame(BaggageImpl.EMPTY, factory.root().extractBaggage())
    }

    @Test
    fun `storeBaggage then extractBaggage round-trips entries`() {
        val baggage = BaggageImpl.EMPTY.set("user", "alice").set("region", "eu")
        val stored = factory.root().storeBaggage(baggage)

        val extracted = stored.extractBaggage()

        assertEquals("alice", extracted.getValue("user"))
        assertEquals("eu", extracted.getValue("region"))
    }

    @Test
    fun `clearBaggage on populated context yields empty baggage`() {
        val populated = factory.root().storeBaggage(BaggageImpl.EMPTY.set("user", "alice"))

        val cleared = populated.clearBaggage()

        assertSame(BaggageImpl.EMPTY, cleared.extractBaggage())
        assertNull(cleared.extractBaggage().getValue("user"))
    }

    @Test
    fun `storing baggage does not mutate original context`() {
        val root = factory.root()
        val derived = root.storeBaggage(BaggageImpl.EMPTY.set("user", "alice"))

        assertNotSame(root, derived)
        assertSame(BaggageImpl.EMPTY, root.extractBaggage())
        assertEquals("alice", derived.extractBaggage().getValue("user"))
    }

    @Test
    fun `clearBaggage does not mutate original context`() {
        val populated = factory.root().storeBaggage(BaggageImpl.EMPTY.set("user", "alice"))

        val cleared = populated.clearBaggage()

        assertNotSame(populated, cleared)
        assertEquals("alice", populated.extractBaggage().getValue("user"))
    }

    @Test
    fun `clearBaggage on root context still yields empty baggage`() {
        assertTrue(factory.root().clearBaggage().extractBaggage().asMap().isEmpty())
    }
}
