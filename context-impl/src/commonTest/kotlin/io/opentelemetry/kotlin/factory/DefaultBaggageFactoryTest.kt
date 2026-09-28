package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.baggage.createBaggage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

@OptIn(ExperimentalApi::class)
internal class DefaultBaggageFactoryTest {

    @Test
    fun `empty returns the shared empty Baggage`() {
        assertSame(createBaggage(), DefaultBaggageFactory.empty())
    }

    @Test
    fun `create delegates to the api implementation`() {
        val baggage = DefaultBaggageFactory.create {
            put("user", "alice", "meta")
        }
        assertEquals("alice", baggage.getValue("user"))
        assertEquals("meta", baggage.asMap().getValue("user").metadata.value)
    }
}
