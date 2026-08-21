package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.InstrumentationScopeInfoImpl
import io.opentelemetry.kotlin.clock.FakeClock
import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import io.opentelemetry.kotlin.export.MutableShutdownState
import io.opentelemetry.kotlin.factory.ContextFactoryImpl
import io.opentelemetry.kotlin.factory.IdGenerator
import io.opentelemetry.kotlin.factory.IdGeneratorImpl
import io.opentelemetry.kotlin.factory.SpanContextFactoryImpl
import io.opentelemetry.kotlin.factory.SpanFactoryImpl
import io.opentelemetry.kotlin.factory.TraceFlagsFactoryImpl
import io.opentelemetry.kotlin.factory.TraceStateFactoryImpl
import io.opentelemetry.kotlin.factory.hexToByteArray
import io.opentelemetry.kotlin.resource.FakeResource
import io.opentelemetry.kotlin.tracing.export.FakeSpanProcessor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * An [IdGenerator] is supplied by the end-user and can return IDs that are not valid, and a third-party
 * [SpanContext] can report itself as valid while carrying malformed IDs. This contains assertions
 * that those scenarios are handled.
 */
internal class TracerInvalidIdTest {

    private val key = InstrumentationScopeInfoImpl("key", null, null, emptyMap())
    private val traceFlagsFactory = TraceFlagsFactoryImpl()
    private val traceStateFactory = TraceStateFactoryImpl()
    private val zeroTraceId = "0".repeat(32)
    private val zeroSpanId = "0".repeat(16)

    @Test
    fun testAllZeroTraceIdIsInvalid() {
        val span = buildTracer(BrokenIdGenerator(traceIdBytes = ByteArray(16))).startSpan("test")
        val spanContext = span.spanContext

        assertFalse(spanContext.isValid)
        assertEquals(zeroTraceId, spanContext.traceId)
        assertNotEquals(zeroSpanId, spanContext.spanId)
    }

    @Test
    fun testWrongLengthTraceIdIsInvalidAndZeroed() {
        val span =
            buildTracer(BrokenIdGenerator(traceIdBytes = ByteArray(3) { 1 })).startSpan("test")
        val spanContext = span.spanContext

        assertFalse(spanContext.isValid)
        assertEquals(zeroTraceId, spanContext.traceId)
        assertEquals(16, spanContext.traceIdBytes.size)
    }

    @Test
    fun testEmptyTraceIdIsInvalidAndZeroed() {
        val span =
            buildTracer(BrokenIdGenerator(traceIdBytes = "oops".hexToByteArray())).startSpan("test")
        val spanContext = span.spanContext

        assertFalse(spanContext.isValid)
        assertEquals(zeroTraceId, spanContext.traceId)
        assertEquals(16, spanContext.traceIdBytes.size)
    }

    @Test
    fun testAllZeroSpanIdIsInvalid() {
        val span = buildTracer(BrokenIdGenerator(spanIdBytes = ByteArray(8))).startSpan("test")
        val spanContext = span.spanContext

        assertFalse(spanContext.isValid)
        assertEquals(zeroSpanId, spanContext.spanId)
        assertNotEquals(zeroTraceId, spanContext.traceId)
    }

    @Test
    fun testWrongLengthSpanIdIsInvalidAndZeroed() {
        val span =
            buildTracer(BrokenIdGenerator(spanIdBytes = ByteArray(9) { 1 })).startSpan("test")
        val spanContext = span.spanContext

        assertFalse(spanContext.isValid)
        assertEquals(zeroSpanId, spanContext.spanId)
        assertEquals(8, spanContext.spanIdBytes.size)
    }

    @Test
    fun testParentClaimingValidityWithMalformedTraceIdIsNotInherited() {
        val idGenerator = IdGeneratorImpl()
        val hostileParent = FakeSpanContext(
            traceIdBytes = ByteArray(4) { 1 },
            spanIdBytes = "1234567890123456".hexToByteArray(),
        )
        assertTrue(hostileParent.isValid)

        val span = startChildOf(hostileParent, idGenerator)
        val spanContext = span.spanContext

        assertTrue(spanContext.isValid)
        assertEquals(16, spanContext.traceIdBytes.size)
        assertNotEquals(zeroTraceId, spanContext.traceId)
        assertNotEquals(hostileParent.traceId, spanContext.traceId)
        assertFalse(spanContext.isRemote)
    }

    @Test
    fun testChildOfRemoteParentIsRemote() {
        val idGenerator = IdGeneratorImpl()
        val span = startChildOf(remoteParent(idGenerator), idGenerator)
        assertTrue(span.spanContext.isRemote)
        assertTrue(span.spanContext.isValid)
    }

    @Test
    fun testDescendantOfRemoteParentIsRemote() {
        val idGenerator = IdGeneratorImpl()
        val tracer = buildTracer(idGenerator)
        val spanFactory = SpanFactoryImpl(
            SpanContextFactoryImpl(
                idGenerator,
                traceFlagsFactory,
                traceStateFactory
            )
        )
        val contextFactory = ContextFactoryImpl(spanFactory)

        val child = tracer.startSpan(
            "child",
            parentContext = contextFactory.root()
                .storeSpan(spanFactory.fromSpanContext(remoteParent(idGenerator))),
        )
        val grandchild = tracer.startSpan(
            "grandchild",
            parentContext = contextFactory.root().storeSpan(child),
        )

        assertTrue(grandchild.spanContext.isRemote)
    }

    @Test
    fun testRootSpanIsNotRemote() {
        val span = buildTracer(IdGeneratorImpl()).startSpan("test")
        assertFalse(span.spanContext.isRemote)
    }

    private fun remoteParent(idGenerator: IdGenerator): SpanContext =
        SpanContextFactoryImpl(idGenerator, traceFlagsFactory, traceStateFactory).create(
            traceId = "12345678901234567890123456789012",
            spanId = "1234567890123456",
            traceFlags = traceFlagsFactory.default,
            traceState = traceStateFactory.default,
            isRemote = true,
        )

    private fun startChildOf(parent: SpanContext, idGenerator: IdGenerator): Span {
        val spanFactory = SpanFactoryImpl(
            SpanContextFactoryImpl(
                idGenerator,
                traceFlagsFactory,
                traceStateFactory
            )
        )
        val parentContext =
            ContextFactoryImpl(spanFactory).root().storeSpan(spanFactory.fromSpanContext(parent))
        return buildTracer(idGenerator).startSpan("test", parentContext = parentContext)
    }

    private fun buildTracer(idGenerator: IdGenerator): TracerImpl {
        val spanContextFactory =
            SpanContextFactoryImpl(idGenerator, traceFlagsFactory, traceStateFactory)
        return TracerImpl(
            clock = FakeClock(),
            processor = FakeSpanProcessor(),
            contextFactory = ContextFactoryImpl(SpanFactoryImpl(spanContextFactory)),
            spanContextFactory = spanContextFactory,
            traceFlagsFactory = traceFlagsFactory,
            scope = key,
            resource = FakeResource(),
            spanLimitConfig = fakeSpanLimitsConfig,
            idGenerator = idGenerator,
            shutdownState = MutableShutdownState(),
            sdkErrorHandler = FakeSdkErrorHandler(),
        )
    }

    /**
     * Returns an invalid trace/span ID where one is supplied, otherwise delegates to [IdGeneratorImpl].
     */
    private class BrokenIdGenerator(
        private val traceIdBytes: ByteArray? = null,
        private val spanIdBytes: ByteArray? = null,
        private val impl: IdGenerator = IdGeneratorImpl(),
    ) : IdGenerator {
        override fun generateTraceIdBytes(): ByteArray = traceIdBytes ?: impl.generateTraceIdBytes()
        override fun generateSpanIdBytes(): ByteArray = spanIdBytes ?: impl.generateSpanIdBytes()
        override val invalidTraceId: ByteArray = impl.invalidTraceId
        override val invalidSpanId: ByteArray = impl.invalidSpanId
        override val generatesRandomTraceIds: Boolean = impl.generatesRandomTraceIds
    }
}
