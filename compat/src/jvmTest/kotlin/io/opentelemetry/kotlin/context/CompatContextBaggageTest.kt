package io.opentelemetry.kotlin.context

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaBaggage
import io.opentelemetry.kotlin.baggage.FakeBaggage
import io.opentelemetry.kotlin.baggage.FakeBaggageEntry
import io.opentelemetry.kotlin.baggage.FakeBaggageEntryMetadata
import io.opentelemetry.kotlin.baggage.createBaggage
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
internal class CompatContextBaggageTest {

    @Test
    fun `extractBaggage on root returns empty baggage`() {
        assertTrue(rootContext().extractBaggage().asMap().isEmpty())
    }

    @Test
    fun `storeBaggage then extractBaggage round-trips entries`() {
        val baggage = createBaggage {
            put("user", "alice")
            put("region", "eu")
        }

        val extracted = rootContext().storeBaggage(baggage).extractBaggage()

        assertEquals("alice", extracted.getValue("user"))
        assertEquals("eu", extracted.getValue("region"))
    }

    @Test
    fun `clearBaggage on populated context yields empty baggage`() {
        val baggage = createBaggage { put("user", "alice") }
        val cleared = rootContext().storeBaggage(baggage).clearBaggage()

        assertTrue(cleared.extractBaggage().asMap().isEmpty())
        assertNull(cleared.extractBaggage().getValue("user"))
    }

    @Test
    fun `kotlin storeBaggage interops with otel-java fromContext`() {
        val baggage = createBaggage { put("user", "alice") }
        val stored = rootContext().storeBaggage(baggage)

        val javaBaggage = OtelJavaBaggage.fromContext(stored.toOtelJavaContext())

        assertEquals("alice", javaBaggage.getEntryValue("user"))
    }

    @Test
    fun `otel-java storeInContext interops with kotlin extractBaggage`() {
        val javaBaggage = OtelJavaBaggage.builder().put("user", "alice").build()
        val javaCtx = javaBaggage.storeInContext(rootContext().toOtelJavaContext())

        val extracted = javaCtx.toOtelKotlinContext().extractBaggage()

        assertEquals("alice", extracted.getValue("user"))
    }

    @Test
    fun `extractBaggage preserves otel-java entries with non-token keys`() {
        val javaBaggage = OtelJavaBaggage.builder().put("bad key", "b").put("good", "g").build()
        val javaCtx = javaBaggage.storeInContext(rootContext().toOtelJavaContext())

        val extracted = javaCtx.toOtelKotlinContext().extractBaggage()

        assertEquals("b", extracted.getValue("bad key"))
        assertEquals("g", extracted.getValue("good"))
    }

    @Test
    fun `modifying extracted baggage writes back all otel-java entries`() {
        val javaBaggage = OtelJavaBaggage.builder().put("bad key", "b").put("good", "g").build()
        val ctx = javaBaggage.storeInContext(rootContext().toOtelJavaContext()).toOtelKotlinContext()

        val updated = ctx.storeBaggage(ctx.extractBaggage().set("user", "alice"))
        val written = OtelJavaBaggage.fromContext(updated.toOtelJavaContext())

        assertEquals(mapOf("bad key" to "b", "good" to "g", "user" to "alice"), written.asMap().mapValues { it.value.value })
    }

    @Test
    fun `storeBaggage accepts a foreign baggage implementation`() {
        val baggage = FakeBaggage(
            mapOf(
                "user" to FakeBaggageEntry("alice"),
                "region" to FakeBaggageEntry("eu", FakeBaggageEntryMetadata("meta")),
            )
        )
        val extracted = rootContext().storeBaggage(baggage).extractBaggage()
        assertEquals("alice", extracted.getValue("user"))
        assertEquals("eu", extracted.getValue("region"))
        assertEquals("", extracted.asMap().getValue("user").metadata.value)
        assertEquals("meta", extracted.asMap().getValue("region").metadata.value)
    }

    @Test
    fun `storeBaggage of a foreign baggage interops with otel-java fromContext`() {
        val baggage = FakeBaggage(mapOf("user" to FakeBaggageEntry("alice")))
        val stored = rootContext().storeBaggage(baggage)
        val javaBaggage = OtelJavaBaggage.fromContext(stored.toOtelJavaContext())
        assertEquals("alice", javaBaggage.getEntryValue("user"))
    }

    @Test
    fun `storeBaggage of an empty foreign baggage yields empty baggage`() {
        val stored = rootContext().storeBaggage(FakeBaggage())
        assertTrue(stored.extractBaggage().asMap().isEmpty())
    }
}
