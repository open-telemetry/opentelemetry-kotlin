package io.opentelemetry.kotlin.tracing.model

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaTraceFlags
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
internal class TraceFlagsAdapterTest {

    @Test
    fun `default flags`() {
        val adapter = TraceFlagsAdapter(OtelJavaTraceFlags.getDefault())

        assertFalse(adapter.isSampled)
        assertFalse(adapter.isRandom)
        assertEquals("00", adapter.hex)
    }

    @Test
    fun `sampled flags`() {
        val adapter = TraceFlagsAdapter(OtelJavaTraceFlags.getSampled())

        assertTrue(adapter.isSampled)
        assertFalse(adapter.isRandom)
        assertEquals("01", adapter.hex)
    }

    @Test
    fun `random flags`() {
        val adapter = TraceFlagsAdapter(OtelJavaTraceFlags.fromByte(0b00000010))

        assertFalse(adapter.isSampled)
        assertTrue(adapter.isRandom)
        assertEquals("02", adapter.hex)
    }

    @Test
    fun `sampled and random flags`() {
        val adapter = TraceFlagsAdapter(OtelJavaTraceFlags.fromByte(0b00000011))

        assertTrue(adapter.isSampled)
        assertTrue(adapter.isRandom)
        assertEquals("03", adapter.hex)
    }

    @Test
    fun `most significant bits different from 0`() {
        val sampledAndRandom = TraceFlagsAdapter(OtelJavaTraceFlags.fromByte(0b01100011))
        assertTrue(sampledAndRandom.isSampled)
        assertTrue(sampledAndRandom.isRandom)
        assertEquals("03", sampledAndRandom.hex)

        val random = TraceFlagsAdapter(OtelJavaTraceFlags.fromByte(0b01100010))
        assertFalse(random.isSampled)
        assertTrue(random.isRandom)
        assertEquals("02", random.hex)

        val sampled = TraceFlagsAdapter(OtelJavaTraceFlags.fromByte(0b01110001))
        assertTrue(sampled.isSampled)
        assertFalse(sampled.isRandom)
        assertEquals("01", sampled.hex)

        val default = TraceFlagsAdapter(OtelJavaTraceFlags.fromByte(0b01110000))
        assertFalse(default.isSampled)
        assertFalse(default.isRandom)
        assertEquals("00", default.hex)
    }
}
