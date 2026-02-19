package io.opentelemetry.kotlin.tracing.model

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaTraceFlags
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
internal class TraceFlagsAdapterTest {

    @Test
    fun `default flags`() {
        val adapter = TraceFlagsAdapter(OtelJavaTraceFlags.getDefault())
        assertFalse(adapter.isSampled)
        assertFalse(adapter.isRandom)
    }

    @Test
    fun `sampled flags`() {
        val adapter = TraceFlagsAdapter(OtelJavaTraceFlags.getSampled())
        assertTrue(adapter.isSampled)
        assertFalse(adapter.isRandom)
    }

    @Test
    fun `random flags`() {
        val adapter = TraceFlagsAdapter(OtelJavaTraceFlags.fromByte(0b00000010))
        assertFalse(adapter.isSampled)
        assertTrue(adapter.isRandom)
    }

    @Test
    fun `sampled and random flags`() {
        val adapter = TraceFlagsAdapter(OtelJavaTraceFlags.fromByte(0b00000011))
        assertTrue(adapter.isSampled)
        assertTrue(adapter.isRandom)
    }

    @Test
    fun `most significant bits different from 0`() {
        val sampledAndRandom = TraceFlagsAdapter(OtelJavaTraceFlags.fromByte(0b01100011))
        assertTrue(sampledAndRandom.isSampled)
        assertTrue(sampledAndRandom.isRandom)

        val random = TraceFlagsAdapter(OtelJavaTraceFlags.fromByte(0b01100010))
        assertFalse(random.isSampled)
        assertTrue(random.isRandom)

        val sampled = TraceFlagsAdapter(OtelJavaTraceFlags.fromByte(0b01110001))
        assertTrue(sampled.isSampled)
        assertFalse(sampled.isRandom)

        val default = TraceFlagsAdapter(OtelJavaTraceFlags.fromByte(0b01110000))
        assertFalse(default.isSampled)
        assertFalse(default.isRandom)
    }
}
