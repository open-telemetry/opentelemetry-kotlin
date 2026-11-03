package io.opentelemetry.kotlin.baggage

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.context.rootContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotSame
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
internal class BaggageContextStorageTest {

    @Test
    fun `extractBaggage on root returns empty baggage`() {
        assertSame(createBaggage(), rootContext().extractBaggage())
    }

    @Test
    fun `storeBaggage then extractBaggage round-trips entries`() {
        val baggage = createBaggage().set("user", "alice").set("region", "eu")
        val stored = rootContext().storeBaggage(baggage)

        val extracted = stored.extractBaggage()

        assertEquals("alice", extracted.getValue("user"))
        assertEquals("eu", extracted.getValue("region"))
    }

    @Test
    fun `clearBaggage on populated context yields empty baggage`() {
        val populated = rootContext().storeBaggage(createBaggage().set("user", "alice"))

        val cleared = populated.clearBaggage()

        assertSame(createBaggage(), cleared.extractBaggage())
        assertNull(cleared.extractBaggage().getValue("user"))
    }

    @Test
    fun `storing baggage does not mutate original context`() {
        val root = rootContext()
        val derived = root.storeBaggage(createBaggage().set("user", "alice"))

        assertNotSame(root, derived)
        assertSame(createBaggage(), root.extractBaggage())
        assertEquals("alice", derived.extractBaggage().getValue("user"))
    }

    @Test
    fun `clearBaggage does not mutate original context`() {
        val populated = rootContext().storeBaggage(createBaggage().set("user", "alice"))

        val cleared = populated.clearBaggage()

        assertNotSame(populated, cleared)
        assertEquals("alice", populated.extractBaggage().getValue("user"))
    }

    @Test
    fun `clearBaggage on root context still yields empty baggage`() {
        assertTrue(rootContext().clearBaggage().extractBaggage().asMap().isEmpty())
    }
}
