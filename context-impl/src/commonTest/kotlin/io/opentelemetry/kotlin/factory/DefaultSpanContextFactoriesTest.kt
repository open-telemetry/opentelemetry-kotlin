package io.opentelemetry.kotlin.factory

import io.opentelemetry.kotlin.ExperimentalApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
internal class DefaultSpanContextFactoriesTest {

    @Test
    fun `defaults are shared across calls`() {
        assertSame(DefaultTraceFlagsFactory.default, DefaultTraceFlagsFactory.default)
        assertSame(DefaultTraceStateFactory.default, DefaultTraceStateFactory.default)
        assertSame(DefaultSpanContextFactory.invalid, DefaultSpanContextFactory.invalid)
    }

    @Test
    fun `invalid span context uses the default flags and state`() {
        val invalid = DefaultSpanContextFactory.invalid
        assertFalse(invalid.isValid)
        assertSame(DefaultTraceFlagsFactory.default, invalid.traceFlags)
        assertSame(DefaultTraceStateFactory.default, invalid.traceState)
    }

    @Test
    fun `span context factory creates valid span contexts`() {
        val traceId = "4bf92f3577b34da6a3ce929d0e0e4736"
        val spanId = "00f067aa0ba902b7"
        val flags = DefaultTraceFlagsFactory.fromHex("01")
        val spanContext = DefaultSpanContextFactory.create(
            traceId,
            spanId,
            flags,
            DefaultTraceStateFactory.default,
            true,
        )
        assertEquals(traceId, spanContext.traceId)
        assertEquals(spanId, spanContext.spanId)
        assertTrue(spanContext.traceFlags.isSampled)
        assertTrue(spanContext.isValid)
        assertTrue(spanContext.isRemote)
    }

    @Test
    fun `trace flags factory creates shared instances from booleans`() {
        val unsampled = DefaultTraceFlagsFactory.create(isSampled = false, isRandom = false)
        val sampledRandom = DefaultTraceFlagsFactory.create(isSampled = true, isRandom = true)
        assertSame(DefaultTraceFlagsFactory.default, unsampled)
        assertSame(DefaultTraceFlagsFactory.fromHex("03"), sampledRandom)
    }
}
