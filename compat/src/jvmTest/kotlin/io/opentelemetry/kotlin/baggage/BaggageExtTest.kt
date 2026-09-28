package io.opentelemetry.kotlin.baggage

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaBaggage
import io.opentelemetry.kotlin.aliases.OtelJavaBaggageEntryMetadata
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
internal class BaggageExtTest {

    @Test
    fun `kotlin baggage is copied entry-by-entry`() {
        val baggage = createBaggage {
            put("user", "alice")
            put("region", "eu", "meta")
        }

        val converted = baggage.toOtelJavaBaggage()
        assertEquals(2, converted.size())
        assertEquals("alice", converted.getEntryValue("user"))
        assertEquals("eu", converted.getEntryValue("region"))
        assertEquals("meta", converted.asMap().getValue("region").metadata.value)
    }

    @Test
    fun `foreign baggage is copied entry-by-entry`() {
        val baggage = FakeBaggage(
            mapOf(
                "user" to FakeBaggageEntry("alice"),
                "region" to FakeBaggageEntry("eu", FakeBaggageEntryMetadata("meta")),
            )
        )

        val converted = baggage.toOtelJavaBaggage()
        assertEquals(2, converted.size())
        assertEquals("alice", converted.getEntryValue("user"))
        assertEquals("eu", converted.getEntryValue("region"))
        assertEquals("meta", converted.asMap().getValue("region").metadata.value)
    }

    @Test
    fun `empty foreign baggage converts to empty baggage`() {
        assertTrue(FakeBaggage().toOtelJavaBaggage().isEmpty)
    }

    @Test
    fun `java baggage is copied into kotlin baggage`() {
        val javaBaggage = OtelJavaBaggage.builder()
            .put("user", "alice")
            .put("region", "eu", OtelJavaBaggageEntryMetadata.create("meta"))
            .build()

        val converted = javaBaggage.toOtelKotlinBaggage()
        assertEquals(2, converted.asMap().size)
        assertEquals("alice", converted.getValue("user"))
        assertEquals("", converted.asMap().getValue("user").metadata.value)
        assertEquals("meta", converted.asMap().getValue("region").metadata.value)
    }

    @Test
    fun `empty java baggage converts to the shared empty baggage`() {
        assertSame(createBaggage(), OtelJavaBaggage.empty().toOtelKotlinBaggage())
    }

    @Test
    fun `java entries rejected by kotlin validation are dropped`() {
        val javaBaggage = OtelJavaBaggage.builder()
            .put("bad key", "v")
            .put("good", "g")
            .build()

        val converted = javaBaggage.toOtelKotlinBaggage()
        assertNull(converted.getValue("bad key"))
        assertEquals("g", converted.getValue("good"))
    }
}
