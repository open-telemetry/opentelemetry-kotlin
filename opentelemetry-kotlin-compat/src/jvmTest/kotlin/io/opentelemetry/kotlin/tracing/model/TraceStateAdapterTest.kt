package io.opentelemetry.kotlin.tracing.model

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaTraceState
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalApi::class)
internal class TraceStateAdapterTest {

    private val initial = OtelJavaTraceState.builder().put("key", "value").build()

    @Test
    fun testTraceState() {
        val adapter = TraceStateAdapter(initial)
        val expected = mapOf("key" to "value")
        assertEquals(expected, adapter.asMap())
        assertEquals("value", adapter.get("key"))

        val mutate1 = adapter.put("key2", "value2")
        assertEquals(expected.plus(mapOf("key2" to "value2")), mutate1.asMap())

        val mutate2 = adapter.remove("key2")
        assertEquals(expected, mutate2.asMap())
    }
}
