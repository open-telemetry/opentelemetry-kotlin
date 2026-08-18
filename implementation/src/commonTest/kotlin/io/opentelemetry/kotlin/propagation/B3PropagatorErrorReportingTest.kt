package io.opentelemetry.kotlin.propagation

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import io.opentelemetry.kotlin.error.SdkErrorSeverity
import io.opentelemetry.kotlin.factory.ContextFactoryImpl
import io.opentelemetry.kotlin.factory.IdGeneratorImpl
import io.opentelemetry.kotlin.factory.SpanContextFactoryImpl
import io.opentelemetry.kotlin.factory.SpanFactoryImpl
import io.opentelemetry.kotlin.factory.TraceFlagsFactoryImpl
import io.opentelemetry.kotlin.factory.TraceStateFactoryImpl
import io.opentelemetry.kotlin.init.B3Format
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

@OptIn(ExperimentalApi::class)
internal class B3PropagatorErrorReportingTest {

    private val traceFlagsFactory = TraceFlagsFactoryImpl()
    private val traceStateFactory = TraceStateFactoryImpl()
    private val spanContextFactory = SpanContextFactoryImpl(IdGeneratorImpl(), traceFlagsFactory, traceStateFactory)
    private val spanFactory = SpanFactoryImpl(spanContextFactory)
    private val contextFactory = ContextFactoryImpl(spanFactory)

    private lateinit var handler: FakeSdkErrorHandler
    private lateinit var singlePropagator: B3Propagator
    private lateinit var multiPropagator: B3Propagator

    private val traceId = "0af7651916cd43dd8448eb211c80319c"
    private val spanId = "b7ad6b7169203331"

    @BeforeTest
    fun setUp() {
        handler = FakeSdkErrorHandler()
        singlePropagator = B3Propagator(
            B3Format.SINGLE,
            traceFlagsFactory,
            traceStateFactory,
            spanContextFactory,
            spanFactory,
            handler,
        )
        multiPropagator = B3Propagator(
            B3Format.MULTI,
            traceFlagsFactory,
            traceStateFactory,
            spanContextFactory,
            spanFactory,
            handler,
        )
    }

    @Test
    fun `extract single reports ApiMisuse for wrong number of parts`() {
        val ctx = contextFactory.root()
        assertSame(ctx, singlePropagator.extract(ctx, mapOf("b3" to traceId), MapTextMapGetter))

        assertEquals(1, handler.apiMisuses.size)
        val error = handler.apiMisuses.single()
        assertEquals("B3Propagator.extractSingle", error.api)
        assertEquals("B3 single header has wrong number of parts: $traceId", error.message)
        assertEquals(SdkErrorSeverity.WARNING, error.severity)
    }

    @Test
    fun `extract single reports ApiMisuse for invalid traceId`() {
        val ctx = contextFactory.root()
        assertSame(
            ctx,
            singlePropagator.extract(ctx, mapOf("b3" to "${"0".repeat(32)}-$spanId-1"), MapTextMapGetter),
        )

        assertEquals(1, handler.apiMisuses.size)
        val error = handler.apiMisuses.single()
        assertEquals("B3Propagator.extractSingle", error.api)
        assertEquals("B3 invalid traceId in single header: ${"0".repeat(32)}", error.message)
    }

    @Test
    fun `extract single reports ApiMisuse for invalid spanId`() {
        val ctx = contextFactory.root()
        assertSame(
            ctx,
            singlePropagator.extract(ctx, mapOf("b3" to "$traceId-${"0".repeat(16)}-1"), MapTextMapGetter),
        )

        assertEquals(1, handler.apiMisuses.size)
        val error = handler.apiMisuses.single()
        assertEquals("B3Propagator.extractSingle", error.api)
        assertEquals("B3 invalid spanId in single header: ${"0".repeat(16)}", error.message)
    }

    @Test
    fun `extract multi reports ApiMisuse for invalid traceId`() {
        val ctx = contextFactory.root()
        assertSame(
            ctx,
            multiPropagator.extract(
                ctx,
                mapOf("X-B3-TraceId" to "not-a-valid-trace-id", "X-B3-SpanId" to spanId),
                MapTextMapGetter,
            ),
        )

        assertEquals(1, handler.apiMisuses.size)
        val error = handler.apiMisuses.single()
        assertEquals("B3Propagator.extractMulti", error.api)
        assertEquals("B3 invalid traceId in multi header: not-a-valid-trace-id", error.message)
    }

    @Test
    fun `extract multi reports ApiMisuse for invalid spanId`() {
        val ctx = contextFactory.root()
        assertSame(
            ctx,
            multiPropagator.extract(
                ctx,
                mapOf("X-B3-TraceId" to traceId, "X-B3-SpanId" to "short"),
                MapTextMapGetter,
            ),
        )

        assertEquals(1, handler.apiMisuses.size)
        val error = handler.apiMisuses.single()
        assertEquals("B3Propagator.extractMulti", error.api)
        assertEquals("B3 invalid spanId in multi header: short", error.message)
    }
}
