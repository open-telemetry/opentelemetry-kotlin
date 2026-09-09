package io.opentelemetry.kotlin.propagation

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.error.NoopSdkErrorHandler
import io.opentelemetry.kotlin.factory.ContextFactoryImpl
import io.opentelemetry.kotlin.factory.IdGeneratorImpl
import io.opentelemetry.kotlin.factory.SpanContextFactoryImpl
import io.opentelemetry.kotlin.factory.SpanFactoryImpl
import io.opentelemetry.kotlin.factory.TraceFlagsFactoryImpl
import io.opentelemetry.kotlin.factory.TraceStateFactoryImpl
import io.opentelemetry.kotlin.init.B3Format
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
internal class B3PropagatorTest {

    private val traceFlagsFactory = TraceFlagsFactoryImpl()
    private val traceStateFactory = TraceStateFactoryImpl()
    private val spanContextFactory = SpanContextFactoryImpl(IdGeneratorImpl(), traceFlagsFactory, traceStateFactory)
    private val spanFactory = SpanFactoryImpl(spanContextFactory)
    private val contextFactory = ContextFactoryImpl(spanFactory)

    private val singlePropagator =
        B3Propagator(B3Format.SINGLE, traceFlagsFactory, traceStateFactory, spanContextFactory, spanFactory, NoopSdkErrorHandler)
    private val multiPropagator =
        B3Propagator(B3Format.MULTI, traceFlagsFactory, traceStateFactory, spanContextFactory, spanFactory, NoopSdkErrorHandler)

    private val traceId = "0af7651916cd43dd8448eb211c80319c"
    private val spanId = "b7ad6b7169203331"

    @Test
    fun `single format fields returns only b3`() {
        assertEquals(listOf("b3"), singlePropagator.fields().toList())
    }

    @Test
    fun `multi format fields returns X-B3 headers`() {
        assertEquals(listOf("X-B3-TraceId", "X-B3-SpanId", "X-B3-Sampled"), multiPropagator.fields().toList())
    }

    @Test
    fun `inject single does nothing when span is invalid`() {
        val carrier = mutableMapOf<String, String>()
        singlePropagator.inject(contextFactory.root(), carrier, FakeTextMapSetter)
        assertTrue(carrier.isEmpty())
    }

    @Test
    fun `inject single writes correct sampled flag`() {
        assertEquals("$traceId-$spanId-1", injectSingle(sampled = true)["b3"])
        assertEquals("$traceId-$spanId-0", injectSingle(sampled = false)["b3"])
    }

    @Test
    fun `inject single writes d flag when debug context is set`() {
        val spanContext = spanContextFactory.create(traceId, spanId, traceFlagsFactory.fromHex("01"), traceStateFactory.default, false)
        val ctx = contextFactory.root()
            .storeSpan(spanFactory.fromSpanContext(spanContext))
            .set(B3Propagator.DEBUG_CONTEXT_KEY, true)
        val carrier = mutableMapOf<String, String>()
        singlePropagator.inject(ctx, carrier, FakeTextMapSetter)
        assertEquals("$traceId-$spanId-d", carrier["b3"])
    }

    @Test
    fun `inject multi does nothing when span is invalid`() {
        val carrier = mutableMapOf<String, String>()
        multiPropagator.inject(contextFactory.root(), carrier, FakeTextMapSetter)
        assertTrue(carrier.isEmpty())
    }

    @Test
    fun `inject multi writes correct sampled header`() {
        val sampledCarrier = injectMulti(sampled = true)
        assertEquals(traceId, sampledCarrier["X-B3-TraceId"])
        assertEquals(spanId, sampledCarrier["X-B3-SpanId"])
        assertEquals("1", sampledCarrier["X-B3-Sampled"])
        assertNull(sampledCarrier["X-B3-Flags"])

        val unsampledCarrier = injectMulti(sampled = false)
        assertEquals("0", unsampledCarrier["X-B3-Sampled"])
        assertNull(unsampledCarrier["X-B3-Flags"])
    }

    @Test
    fun `inject multi writes X-B3-Flags 1 and sampled 1 when debug`() {
        val spanContext = spanContextFactory.create(traceId, spanId, traceFlagsFactory.fromHex("01"), traceStateFactory.default, false)
        val ctx = contextFactory.root()
            .storeSpan(spanFactory.fromSpanContext(spanContext))
            .set(B3Propagator.DEBUG_CONTEXT_KEY, true)
        val carrier = mutableMapOf<String, String>()
        multiPropagator.inject(ctx, carrier, FakeTextMapSetter)
        assertEquals("1", carrier["X-B3-Flags"])
        assertEquals("1", carrier["X-B3-Sampled"])
    }

    @Test
    fun `extract single returns original context when b3 header absent`() {
        val ctx = contextFactory.root()
        assertSame(ctx, singlePropagator.extract(ctx, emptyMap(), FakeTextMapGetter))
    }

    @Test
    fun `extract single parses sampled flag`() {
        val sampled = singlePropagator.extract(contextFactory.root(), mapOf("b3" to "$traceId-$spanId-1"), FakeTextMapGetter)
        assertEquals(traceId, sampled.extractSpan().spanContext.traceId)
        assertEquals(spanId, sampled.extractSpan().spanContext.spanId)
        assertTrue(sampled.extractSpan().spanContext.traceFlags.isSampled)

        val unsampled = singlePropagator.extract(contextFactory.root(), mapOf("b3" to "$traceId-$spanId-0"), FakeTextMapGetter)
        assertFalse(unsampled.extractSpan().spanContext.traceFlags.isSampled)
    }

    @Test
    fun `extract single treats absent sampled part as not sampled`() {
        val ctx = singlePropagator.extract(contextFactory.root(), mapOf("b3" to "$traceId-$spanId"), FakeTextMapGetter)
        assertFalse(ctx.extractSpan().spanContext.traceFlags.isSampled)
    }

    @Test
    fun `extract single sets debug context key and sampled when flag is d`() {
        val ctx = singlePropagator.extract(contextFactory.root(), mapOf("b3" to "$traceId-$spanId-d"), FakeTextMapGetter)
        assertTrue(ctx.extractSpan().spanContext.traceFlags.isSampled)
        assertEquals(true, ctx.get(B3Propagator.DEBUG_CONTEXT_KEY))
    }

    @Test
    fun `extract single pads 64-bit traceId to 128-bit`() {
        val shortTraceId = "a" + "0".repeat(15)
        val ctx = singlePropagator.extract(contextFactory.root(), mapOf("b3" to "$shortTraceId-$spanId-1"), FakeTextMapGetter)
        assertEquals("0000000000000000$shortTraceId", ctx.extractSpan().spanContext.traceId)
    }

    @Test
    fun `extract single returns original context for single-part header`() {
        val ctx = contextFactory.root()
        assertSame(ctx, singlePropagator.extract(ctx, mapOf("b3" to traceId), FakeTextMapGetter))
    }

    @Test
    fun `extract single returns original context for 5-part header`() {
        val ctx = contextFactory.root()
        assertSame(ctx, singlePropagator.extract(ctx, mapOf("b3" to "$traceId-$spanId-1-$spanId-extra"), FakeTextMapGetter))
    }

    @Test
    fun `extract single returns original context for all-zero traceId`() {
        val ctx = contextFactory.root()
        assertSame(ctx, singlePropagator.extract(ctx, mapOf("b3" to "${"0".repeat(32)}-$spanId-1"), FakeTextMapGetter))
    }

    @Test
    fun `extract single returns original context for all-zero spanId`() {
        val ctx = contextFactory.root()
        assertSame(ctx, singlePropagator.extract(ctx, mapOf("b3" to "$traceId-${"0".repeat(16)}-1"), FakeTextMapGetter))
    }

    @Test
    fun `extract single ignores parent span id when 4 parts present`() {
        val ctx = singlePropagator.extract(contextFactory.root(), mapOf("b3" to "$traceId-$spanId-1-$spanId"), FakeTextMapGetter)
        assertTrue(ctx.extractSpan().spanContext.isValid)
        assertEquals(spanId, ctx.extractSpan().spanContext.spanId)
    }

    @Test
    fun `extract multi returns original context when traceId absent`() {
        val ctx = contextFactory.root()
        assertSame(ctx, multiPropagator.extract(ctx, mapOf("X-B3-SpanId" to spanId, "X-B3-Sampled" to "1"), FakeTextMapGetter))
    }

    @Test
    fun `extract multi returns original context when spanId absent`() {
        val ctx = contextFactory.root()
        assertSame(ctx, multiPropagator.extract(ctx, mapOf("X-B3-TraceId" to traceId, "X-B3-Sampled" to "1"), FakeTextMapGetter))
    }

    @Test
    fun `extract multi parses sampled flag`() {
        val sampled = multiPropagator.extract(
            contextFactory.root(),
            mapOf("X-B3-TraceId" to traceId, "X-B3-SpanId" to spanId, "X-B3-Sampled" to "1"),
            FakeTextMapGetter,
        )
        assertEquals(traceId, sampled.extractSpan().spanContext.traceId)
        assertEquals(spanId, sampled.extractSpan().spanContext.spanId)
        assertTrue(sampled.extractSpan().spanContext.traceFlags.isSampled)

        val unsampled = multiPropagator.extract(
            contextFactory.root(),
            mapOf("X-B3-TraceId" to traceId, "X-B3-SpanId" to spanId, "X-B3-Sampled" to "0"),
            FakeTextMapGetter,
        )
        assertFalse(unsampled.extractSpan().spanContext.traceFlags.isSampled)
    }

    @Test
    fun `extract multi treats absent sampled as not sampled`() {
        val carrier = mapOf("X-B3-TraceId" to traceId, "X-B3-SpanId" to spanId)
        val ctx = multiPropagator.extract(contextFactory.root(), carrier, FakeTextMapGetter)
        assertFalse(ctx.extractSpan().spanContext.traceFlags.isSampled)
    }

    @Test
    fun `extract multi sets debug key and sampled when X-B3-Flags is 1`() {
        val carrier = mapOf("X-B3-TraceId" to traceId, "X-B3-SpanId" to spanId, "X-B3-Flags" to "1")
        val ctx = multiPropagator.extract(contextFactory.root(), carrier, FakeTextMapGetter)
        assertTrue(ctx.extractSpan().spanContext.traceFlags.isSampled)
        assertEquals(true, ctx.get(B3Propagator.DEBUG_CONTEXT_KEY))
    }

    @Test
    fun `extract multi pads 64-bit traceId`() {
        val shortTraceId = "b" + "0".repeat(15)
        val carrier = mapOf("X-B3-TraceId" to shortTraceId, "X-B3-SpanId" to spanId, "X-B3-Sampled" to "1")
        val ctx = multiPropagator.extract(contextFactory.root(), carrier, FakeTextMapGetter)
        assertEquals("0000000000000000$shortTraceId", ctx.extractSpan().spanContext.traceId)
    }

    @Test
    fun `extract multi returns original context for all-zero spanId`() {
        val ctx = contextFactory.root()
        assertSame(
            ctx,
            multiPropagator.extract(
                ctx,
                mapOf("X-B3-TraceId" to traceId, "X-B3-SpanId" to "0".repeat(16), "X-B3-Sampled" to "1"),
                FakeTextMapGetter,
            )
        )
    }

    @Test
    fun `single header takes precedence over multi when both present`() {
        val spanId2 = "a1b2c3d4e5f60001"
        val carrier = mapOf(
            "b3" to "$traceId-$spanId-1",
            "X-B3-TraceId" to traceId,
            "X-B3-SpanId" to spanId2,
            "X-B3-Sampled" to "1",
        )
        val ctx = singlePropagator.extract(contextFactory.root(), carrier, FakeTextMapGetter)
        assertEquals(spanId, ctx.extractSpan().spanContext.spanId)
    }

    @Test
    fun `falls back to multi when single header absent`() {
        val carrier = mapOf("X-B3-TraceId" to traceId, "X-B3-SpanId" to spanId, "X-B3-Sampled" to "1")
        val ctx = singlePropagator.extract(contextFactory.root(), carrier, FakeTextMapGetter)
        assertEquals(spanId, ctx.extractSpan().spanContext.spanId)
    }

    @Test
    fun `falls back to multi when single header is invalid`() {
        val spanId2 = "a1b2c3d4e5f60001"
        val carrier = mapOf(
            "b3" to "not-valid",
            "X-B3-TraceId" to traceId,
            "X-B3-SpanId" to spanId2,
            "X-B3-Sampled" to "1",
        )
        val ctx = singlePropagator.extract(contextFactory.root(), carrier, FakeTextMapGetter)
        assertEquals(spanId2, ctx.extractSpan().spanContext.spanId)
    }

    @Test
    fun `single inject then extract round-trips sampled flag`() {
        val sampledCtx = singlePropagator.extract(contextFactory.root(), injectSingle(sampled = true), FakeTextMapGetter)
        assertEquals(traceId, sampledCtx.extractSpan().spanContext.traceId)
        assertEquals(spanId, sampledCtx.extractSpan().spanContext.spanId)
        assertTrue(sampledCtx.extractSpan().spanContext.traceFlags.isSampled)

        val unsampledCtx = singlePropagator.extract(contextFactory.root(), injectSingle(sampled = false), FakeTextMapGetter)
        assertFalse(unsampledCtx.extractSpan().spanContext.traceFlags.isSampled)
    }

    @Test
    fun `multi inject then extract round-trips sampled span`() {
        val carrier = injectMulti(sampled = true)
        val ctx = multiPropagator.extract(contextFactory.root(), carrier, FakeTextMapGetter)
        val sc = ctx.extractSpan().spanContext
        assertEquals(traceId, sc.traceId)
        assertEquals(spanId, sc.spanId)
        assertTrue(sc.traceFlags.isSampled)
    }

    @Test
    fun `debug flag survives single inject then extract`() {
        val spanContext = spanContextFactory.create(traceId, spanId, traceFlagsFactory.fromHex("01"), traceStateFactory.default, false)
        val inCtx = contextFactory.root()
            .storeSpan(spanFactory.fromSpanContext(spanContext))
            .set(B3Propagator.DEBUG_CONTEXT_KEY, true)
        val carrier = mutableMapOf<String, String>()
        singlePropagator.inject(inCtx, carrier, FakeTextMapSetter)
        val outCtx = singlePropagator.extract(contextFactory.root(), carrier, FakeTextMapGetter)
        assertEquals(true, outCtx.get(B3Propagator.DEBUG_CONTEXT_KEY))
        assertTrue(outCtx.extractSpan().spanContext.traceFlags.isSampled)
    }

    @Test
    fun `debug flag survives multi inject then extract`() {
        val spanContext = spanContextFactory.create(traceId, spanId, traceFlagsFactory.fromHex("01"), traceStateFactory.default, false)
        val inCtx = contextFactory.root()
            .storeSpan(spanFactory.fromSpanContext(spanContext))
            .set(B3Propagator.DEBUG_CONTEXT_KEY, true)
        val carrier = mutableMapOf<String, String>()
        multiPropagator.inject(inCtx, carrier, FakeTextMapSetter)
        val outCtx = multiPropagator.extract(contextFactory.root(), carrier, FakeTextMapGetter)
        assertEquals(true, outCtx.get(B3Propagator.DEBUG_CONTEXT_KEY))
        assertTrue(outCtx.extractSpan().spanContext.traceFlags.isSampled)
    }

    private fun injectSingle(sampled: Boolean): MutableMap<String, String> {
        val flags = if (sampled) { traceFlagsFactory.fromHex("01") } else { traceFlagsFactory.fromHex("00") }
        val spanContext = spanContextFactory.create(traceId, spanId, flags, traceStateFactory.default, false)
        val ctx = contextFactory.root().storeSpan(spanFactory.fromSpanContext(spanContext))
        return mutableMapOf<String, String>().also { singlePropagator.inject(ctx, it, FakeTextMapSetter) }
    }

    private fun injectMulti(sampled: Boolean): MutableMap<String, String> {
        val flags = if (sampled) { traceFlagsFactory.fromHex("01") } else { traceFlagsFactory.fromHex("00") }
        val spanContext = spanContextFactory.create(traceId, spanId, flags, traceStateFactory.default, false)
        val ctx = contextFactory.root().storeSpan(spanFactory.fromSpanContext(spanContext))
        return mutableMapOf<String, String>().also { multiPropagator.inject(ctx, it, FakeTextMapSetter) }
    }
}
