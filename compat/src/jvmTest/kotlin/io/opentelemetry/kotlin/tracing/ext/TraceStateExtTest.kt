package io.opentelemetry.kotlin.tracing.ext

import io.opentelemetry.kotlin.aliases.OtelJavaTraceState
import io.opentelemetry.kotlin.aliases.OtelJavaTraceStateBuilder
import io.opentelemetry.kotlin.factory.DefaultTraceStateFactory
import io.opentelemetry.kotlin.tracing.FakeTraceState
import org.junit.Test
import java.util.function.BiConsumer
import kotlin.test.assertEquals
import kotlin.test.assertSame

internal class TraceStateExtTest {

    @Test
    fun toOtelJavaTraceState() {
        val expected = FakeTraceState()
        val observed = expected.toOtelJavaTraceState()
        assertEquals(expected.asMap(), observed.asMap())
    }

    @Test
    fun `java trace state is copied into kotlin trace state in order`() {
        val javaState = OtelJavaTraceState.builder()
            .put("first", "1")
            .put("second", "2")
            .put("third", "3")
            .build()
        val expectedKeys = mutableListOf<String>()
        javaState.forEach { key, _ -> expectedKeys.add(key) }

        val observed = javaState.toOtelKotlinTraceState()
        assertEquals(expectedKeys, observed.asMap().keys.toList())
        assertEquals(javaState.asMap(), observed.asMap())
    }

    @Test
    fun `entries that fail kotlin validation are dropped`() {
        val javaState = MapTraceState(linkedMapOf("valid" to "1", "Invalid" to "2", "other" to "3"))
        val observed = javaState.toOtelKotlinTraceState()
        assertEquals(listOf("valid", "other"), observed.asMap().keys.toList())
    }

    @Test
    fun `empty java trace state converts to the shared empty trace state`() {
        assertSame(DefaultTraceStateFactory.default, OtelJavaTraceState.getDefault().toOtelKotlinTraceState())
    }

    @Test
    fun `round trip preserves entries`() {
        val kotlinState = DefaultTraceStateFactory.default
            .put("vendor", "value")
            .put("tenant@system", "other")
        val observed = kotlinState.toOtelJavaTraceState().toOtelKotlinTraceState()
        assertEquals(kotlinState.asMap().toList(), observed.asMap().toList())
    }

    private class MapTraceState(private val entries: Map<String, String>) : OtelJavaTraceState {
        override fun get(key: String): String? = entries[key]
        override fun size(): Int = entries.size
        override fun isEmpty(): Boolean = entries.isEmpty()
        override fun forEach(consumer: BiConsumer<String, String>) = entries.forEach(consumer)
        override fun asMap(): Map<String, String> = entries
        override fun toBuilder(): OtelJavaTraceStateBuilder = throw UnsupportedOperationException()
    }
}
