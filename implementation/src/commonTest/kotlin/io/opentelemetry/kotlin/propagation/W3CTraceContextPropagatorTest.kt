package io.opentelemetry.kotlin.propagation

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.factory.ContextFactoryImpl
import io.opentelemetry.kotlin.factory.IdGeneratorImpl
import io.opentelemetry.kotlin.factory.SpanContextFactoryImpl
import io.opentelemetry.kotlin.factory.SpanFactoryImpl
import io.opentelemetry.kotlin.factory.TraceFlagsFactoryImpl
import io.opentelemetry.kotlin.factory.TraceStateFactoryImpl
import io.opentelemetry.kotlin.tracing.SpanContext
import io.opentelemetry.kotlin.tracing.TraceFlagsImpl
import io.opentelemetry.kotlin.tracing.TraceState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * Asserts behaviour against the W3C Trace Context spec:
 * https://www.w3.org/TR/trace-context/
 */
@OptIn(ExperimentalApi::class)
internal class W3CTraceContextPropagatorTest {

    private val idGenerator = IdGeneratorImpl()
    private val traceFlagsFactory = TraceFlagsFactoryImpl()
    private val traceStateFactory = TraceStateFactoryImpl()
    private val spanContextFactory = SpanContextFactoryImpl(idGenerator, traceFlagsFactory, traceStateFactory)
    private val contextFactory = ContextFactoryImpl()
    private val spanFactory = SpanFactoryImpl(spanContextFactory, contextFactory.spanKey)

    private val propagator = W3CTraceContextPropagator(
        traceFlagsFactory = traceFlagsFactory,
        traceStateFactory = traceStateFactory,
        spanContextFactory = spanContextFactory,
        spanFactory = spanFactory,
        contextFactory = contextFactory,
    )

    private val traceId = "0af7651916cd43dd8448eb211c80319c"
    private val spanId = "b7ad6b7169203331"

    @Test
    fun `fields returns traceparent and tracestate in order`() {
        assertEquals(listOf("traceparent", "tracestate"), propagator.fields().toList())
    }

    @Test
    fun `inject does nothing on root context with no span`() {
        val carrier = mutableMapOf<String, String>()
        propagator.inject(contextFactory.root(), carrier, MapTextMapSetter)
        assertTrue(carrier.isEmpty())
    }

    @Test
    fun `inject does nothing when current span is invalid`() {
        val carrier = mutableMapOf<String, String>()
        val context = contextFactory.storeSpan(contextFactory.root(), spanFactory.invalid)
        propagator.inject(context, carrier, MapTextMapSetter)
        assertTrue(carrier.isEmpty())
    }

    @Test
    fun `inject writes canonical traceparent for a valid sampled span`() {
        val context = contextWithSpan(
            spanContext(
                traceFlags = TraceFlagsImpl(isSampled = true, isRandom = false),
            ),
        )
        val carrier = injectInto(context)
        assertEquals("00-$traceId-$spanId-01", carrier["traceparent"])
    }

    @Test
    fun `inject writes flags 00 when not sampled`() {
        val context = contextWithSpan(
            spanContext(
                traceFlags = TraceFlagsImpl(isSampled = false, isRandom = false),
            ),
        )
        val carrier = injectInto(context)
        assertEquals("00-$traceId-$spanId-00", carrier["traceparent"])
    }

    @Test
    fun `inject writes flags 03 when sampled and random bits set`() {
        val context = contextWithSpan(
            spanContext(
                traceFlags = TraceFlagsImpl(isSampled = true, isRandom = true),
            ),
        )
        val carrier = injectInto(context)
        assertEquals("00-$traceId-$spanId-03", carrier["traceparent"])
    }

    @Test
    fun `inject does not write tracestate when state is empty`() {
        val context = contextWithSpan(spanContext())
        val carrier = injectInto(context)
        assertNull(carrier["tracestate"])
    }

    @Test
    fun `inject writes tracestate when state has entries`() {
        val state = traceStateFactory.default.put("foo", "bar")
        val context = contextWithSpan(spanContext(traceState = state))
        val carrier = injectInto(context)
        assertEquals("foo=bar", carrier["tracestate"])
    }

    @Test
    fun `inject preserves multiple tracestate entries in insertion order`() {
        val state = traceStateFactory.default
            .put("foo", "1")
            .put("bar", "2")
            .put("baz", "3")
        val context = contextWithSpan(spanContext(traceState = state))
        val carrier = injectInto(context)
        assertEquals("foo=1,bar=2,baz=3", carrier["tracestate"])
    }

    @Test
    fun `extract returns original context when traceparent header is absent`() {
        val root = contextFactory.root()
        val result = propagator.extract(root, emptyMap(), MapTextMapGetter)
        assertSame(root, result)
    }

    @Test
    fun `extract returns original context when traceparent has wrong field count`() {
        val root = contextFactory.root()
        val carrier = mapOf("traceparent" to "00-$traceId-$spanId")
        val result = propagator.extract(root, carrier, MapTextMapGetter)
        assertSame(root, result)
    }

    @Test
    fun `extract returns original context when traceparent contains uppercase hex`() {
        val root = contextFactory.root()
        val carrier = mapOf("traceparent" to "00-${traceId.uppercase()}-$spanId-01")
        val result = propagator.extract(root, carrier, MapTextMapGetter)
        assertSame(root, result)
    }

    @Test
    fun `extract returns original context when traceparent uses forbidden version ff`() {
        val root = contextFactory.root()
        val carrier = mapOf("traceparent" to "ff-$traceId-$spanId-01")
        val result = propagator.extract(root, carrier, MapTextMapGetter)
        assertSame(root, result)
    }

    @Test
    fun `extract returns original context when traceparent has all-zero traceId`() {
        val root = contextFactory.root()
        val carrier = mapOf("traceparent" to "00-00000000000000000000000000000000-$spanId-01")
        val result = propagator.extract(root, carrier, MapTextMapGetter)
        assertSame(root, result)
    }

    @Test
    fun `extract returns original context when traceparent has all-zero spanId`() {
        val root = contextFactory.root()
        val carrier = mapOf("traceparent" to "00-$traceId-0000000000000000-01")
        val result = propagator.extract(root, carrier, MapTextMapGetter)
        assertSame(root, result)
    }

    @Test
    fun `extract ignores tracestate when traceparent is invalid`() {
        val root = contextFactory.root()
        val carrier = mapOf(
            "traceparent" to "ff-$traceId-$spanId-01",
            "tracestate" to "foo=bar",
        )
        val result = propagator.extract(root, carrier, MapTextMapGetter)
        assertSame(root, result)
    }

    @Test
    fun `extract produces a SpanContext with isRemote=true`() {
        val carrier = mapOf("traceparent" to "00-$traceId-$spanId-01")
        val result = propagator.extract(contextFactory.root(), carrier, MapTextMapGetter)
        val sc = spanFactory.fromContext(result).spanContext
        assertTrue(sc.isRemote)
    }

    @Test
    fun `extract reads sampled flag from traceparent flags byte`() {
        val sampledCarrier = mapOf("traceparent" to "00-$traceId-$spanId-01")
        val unsampledCarrier = mapOf("traceparent" to "00-$traceId-$spanId-00")

        val sampled = spanFactory
            .fromContext(propagator.extract(contextFactory.root(), sampledCarrier, MapTextMapGetter))
            .spanContext
        val unsampled = spanFactory
            .fromContext(propagator.extract(contextFactory.root(), unsampledCarrier, MapTextMapGetter))
            .spanContext

        assertTrue(sampled.traceFlags.isSampled)
        assertFalse(unsampled.traceFlags.isSampled)
    }

    @Test
    fun `extract attaches an empty TraceState when tracestate header is absent`() {
        val carrier = mapOf("traceparent" to "00-$traceId-$spanId-01")
        val result = propagator.extract(contextFactory.root(), carrier, MapTextMapGetter)
        val sc = spanFactory.fromContext(result).spanContext
        assertTrue(sc.traceState.asMap().isEmpty())
    }

    @Test
    fun `extract attaches parsed TraceState when tracestate header is present`() {
        val carrier = mapOf(
            "traceparent" to "00-$traceId-$spanId-01",
            "tracestate" to "foo=bar,baz=qux",
        )
        val result = propagator.extract(contextFactory.root(), carrier, MapTextMapGetter)
        val sc = spanFactory.fromContext(result).spanContext
        assertEquals(mapOf("foo" to "bar", "baz" to "qux"), sc.traceState.asMap())
    }

    @Test
    fun `extract drops malformed tracestate members but keeps valid ones`() {
        val carrier = mapOf(
            "traceparent" to "00-$traceId-$spanId-01",
            "tracestate" to "foo=bar,bogus,baz=qux",
        )
        val result = propagator.extract(contextFactory.root(), carrier, MapTextMapGetter)
        val sc = spanFactory.fromContext(result).spanContext
        assertEquals(mapOf("foo" to "bar", "baz" to "qux"), sc.traceState.asMap())
    }

    @Test
    fun `extract attaches a non-recording span on the returned context`() {
        val carrier = mapOf("traceparent" to "00-$traceId-$spanId-01")
        val result = propagator.extract(contextFactory.root(), carrier, MapTextMapGetter)
        val span = spanFactory.fromContext(result)
        assertFalse(span.isRecording())
        assertEquals(traceId, span.spanContext.traceId)
        assertEquals(spanId, span.spanContext.spanId)
    }

    @Test
    fun `inject and extract round-trip preserves traceId spanId flags and tracestate`() {
        val state = traceStateFactory.default.put("vendor", "value")
        val original = spanContext(
            traceFlags = TraceFlagsImpl(isSampled = true, isRandom = false),
            traceState = state,
        )
        val carrier = injectInto(contextWithSpan(original))
        val extracted = propagator.extract(contextFactory.root(), carrier, MapTextMapGetter)
        val sc = spanFactory.fromContext(extracted).spanContext

        assertEquals(original.traceId, sc.traceId)
        assertEquals(original.spanId, sc.spanId)
        assertEquals(original.traceFlags.isSampled, sc.traceFlags.isSampled)
        assertEquals(original.traceState.asMap(), sc.traceState.asMap())
        assertTrue(sc.isRemote)
    }

    private fun spanContext(
        traceFlags: io.opentelemetry.kotlin.tracing.TraceFlags = TraceFlagsImpl(isSampled = true, isRandom = false),
        traceState: TraceState = traceStateFactory.default,
    ): SpanContext = spanContextFactory.create(
        traceId = traceId,
        spanId = spanId,
        traceFlags = traceFlags,
        traceState = traceState,
        isRemote = false,
    )

    private fun contextWithSpan(spanContext: SpanContext): Context =
        contextFactory.storeSpan(contextFactory.root(), spanFactory.fromSpanContext(spanContext))

    private fun injectInto(context: Context): MutableMap<String, String> {
        val carrier = mutableMapOf<String, String>()
        propagator.inject(context, carrier, MapTextMapSetter)
        return carrier
    }
}
