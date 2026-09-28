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
}
