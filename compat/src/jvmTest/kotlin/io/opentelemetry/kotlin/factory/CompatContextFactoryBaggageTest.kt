package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaBaggage
import io.opentelemetry.kotlin.baggage.BaggageAdapter
import io.opentelemetry.kotlin.baggage.FakeBaggage
import io.opentelemetry.kotlin.baggage.FakeBaggageEntry
import io.opentelemetry.kotlin.baggage.FakeBaggageEntryMetadata
import io.opentelemetry.kotlin.context.toOtelJavaContext
import io.opentelemetry.kotlin.context.toOtelKotlinContext
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
internal class CompatContextFactoryBaggageTest {

    private val factory = CompatContextFactory()

    @Test
    fun `extractBaggage on root returns empty baggage`() {
        assertTrue(factory.root().extractBaggage().asMap().isEmpty())
    }

    @Test
    fun `storeBaggage then extractBaggage round-trips entries`() {
        val baggage = BaggageAdapter(
            OtelJavaBaggage.builder().put("user", "alice").put("region", "eu").build()
        )

        val extracted = factory.root().storeBaggage(baggage).extractBaggage()

        assertEquals("alice", extracted.getValue("user"))
        assertEquals("eu", extracted.getValue("region"))
    }

    @Test
    fun `clearBaggage on populated context yields empty baggage`() {
        val baggage = BaggageAdapter(OtelJavaBaggage.builder().put("user", "alice").build())
        val cleared = factory.root().storeBaggage(baggage).clearBaggage()

        assertTrue(cleared.extractBaggage().asMap().isEmpty())
        assertNull(cleared.extractBaggage().getValue("user"))
    }

    @Test
    fun `kotlin storeBaggage interops with otel-java fromContext`() {
        val baggage = BaggageAdapter(OtelJavaBaggage.builder().put("user", "alice").build())
        val stored = factory.root().storeBaggage(baggage)

        val javaBaggage = OtelJavaBaggage.fromContext(stored.toOtelJavaContext())

        assertEquals("alice", javaBaggage.getEntryValue("user"))
    }

    @Test
    fun `otel-java storeInContext interops with kotlin extractBaggage`() {
        val javaBaggage = OtelJavaBaggage.builder().put("user", "alice").build()
        val javaCtx = javaBaggage.storeInContext(factory.root().toOtelJavaContext())

        val extracted = javaCtx.toOtelKotlinContext().extractBaggage()

        assertEquals("alice", extracted.getValue("user"))
    }

    @Test
    fun `storeBaggage accepts a foreign baggage implementation`() {
        val baggage = FakeBaggage(
            mapOf(
                "user" to FakeBaggageEntry("alice"),
                "region" to FakeBaggageEntry("eu", FakeBaggageEntryMetadata("meta")),
            )
        )
        val extracted = factory.root().storeBaggage(baggage).extractBaggage()
        assertEquals("alice", extracted.getValue("user"))
        assertEquals("eu", extracted.getValue("region"))
        assertEquals("", extracted.asMap().getValue("user").metadata.value)
        assertEquals("meta", extracted.asMap().getValue("region").metadata.value)
    }

    @Test
    fun `storeBaggage of a foreign baggage interops with otel-java fromContext`() {
        val baggage = FakeBaggage(mapOf("user" to FakeBaggageEntry("alice")))
        val stored = factory.root().storeBaggage(baggage)
        val javaBaggage = OtelJavaBaggage.fromContext(stored.toOtelJavaContext())
        assertEquals("alice", javaBaggage.getEntryValue("user"))
    }

    @Test
    fun `storeBaggage of an empty foreign baggage yields empty baggage`() {
        val stored = factory.root().storeBaggage(FakeBaggage())
        assertTrue(stored.extractBaggage().asMap().isEmpty())
    }
}
