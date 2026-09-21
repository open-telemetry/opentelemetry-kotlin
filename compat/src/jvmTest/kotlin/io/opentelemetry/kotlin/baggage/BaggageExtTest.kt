package io.opentelemetry.kotlin.baggage

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaBaggage
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
internal class BaggageExtTest {

    @Test
    fun `adapter is unwrapped rather than copied`() {
        val impl = OtelJavaBaggage.builder().put("user", "alice").build()
        assertSame(impl, BaggageAdapter(impl).toOtelJavaBaggage())
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
}
