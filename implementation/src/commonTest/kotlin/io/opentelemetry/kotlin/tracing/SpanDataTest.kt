package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.InstrumentationScopeInfoImpl
import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.clock.FakeClock
import io.opentelemetry.kotlin.error.NoopSdkErrorHandler
import io.opentelemetry.kotlin.export.MutableShutdownState
import io.opentelemetry.kotlin.factory.FakeContextFactory
import io.opentelemetry.kotlin.factory.FakeIdGenerator
import io.opentelemetry.kotlin.factory.FakeSpanContextFactory
import io.opentelemetry.kotlin.factory.FakeTraceFlagsFactory
import io.opentelemetry.kotlin.resource.FakeResource
import io.opentelemetry.kotlin.tracing.data.SpanData
import io.opentelemetry.kotlin.tracing.export.FakeSpanProcessor
import io.opentelemetry.kotlin.tracing.model.ReadableSpan
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

internal class SpanDataTest {

    private val key = InstrumentationScopeInfoImpl("key", null, null, emptyMap())
    private lateinit var tracer: TracerImpl
    private lateinit var clock: FakeClock
    private lateinit var processor: FakeSpanProcessor
    private lateinit var fakeResource: FakeResource
    private lateinit var fakeSpanContext: FakeSpanContext
    private lateinit var shutdownState: MutableShutdownState

    @BeforeTest
    fun setUp() {
        clock = FakeClock()
        processor = FakeSpanProcessor()
        fakeResource = FakeResource()
        fakeSpanContext = FakeSpanContext.INVALID
        shutdownState = MutableShutdownState()
        tracer = TracerImpl(
            clock = clock,
            processor = processor,
            contextFactory = FakeContextFactory(),
            spanContextFactory = FakeSpanContextFactory(),
            traceFlagsFactory = FakeTraceFlagsFactory(),
            scope = key,
            resource = fakeResource,
            spanLimitConfig = fakeSpanLimitsConfig,
            idGenerator = FakeIdGenerator(),
            shutdownState = shutdownState,
            sdkErrorHandler = NoopSdkErrorHandler,
        )
    }

    @Test
    fun testSpanDataCreationOnStart() {
        val span = simulateSpan()
        val data = processor.startCalls.single().toSpanData()
        assertSpanData(span, data)
    }

    @Test
    fun testSpanDataCreationOnEnd() {
        val span = simulateSpan()
        val data = processor.startCalls.single().toSpanData()
        assertSpanData(span, data)
    }

    @Test
    fun testRetrieveSpanData() {
        val span = tracer.startSpan("test")
        val readableSpan = span.toReadableSpan()
        val data: SpanData = readableSpan.toSpanData()
        assertEquals("test", data.name)
    }

    @Test
    fun testSpanDataAfterShutdown() {
        assertTrue(tracer.startSpan("test").isRecording())
        shutdownState.shutdownNow()
        assertFalse(tracer.startSpan("test").isRecording())
    }

    @Test
    fun testToSpanDataCopiesByteArrayAttributes() {
        val bytes = byteArrayOf(1, 2, 3)
        val span = tracer.startSpan("test")
        span.setByteArrayAttribute("bytes", bytes)
        val data = span.toReadableSpan().toSpanData()
        bytes[0] = 9
        assertEquals(1.toByte(), (data.attributes["bytes"] as ByteArray)[0])
    }

    @Test
    fun testToSpanDataCopiesListAttributes() {
        val flags = mutableListOf(true)
        val span = tracer.startSpan("test")
        span.setBooleanListAttribute("flags", flags)
        val data = span.toReadableSpan().toSpanData()
        flags.add(false)
        assertEquals(listOf(true), data.attributes["flags"])
    }

    @Test
    fun testToSpanDataCopiesEventAndLinkAttributeValues() {
        val eventList = mutableListOf(true)
        val linkBytes = byteArrayOf(1)
        val span = tracer.startSpan("test")
        span.addEvent("event") {
            setBooleanListAttribute("flags", eventList)
        }
        span.addLink(fakeSpanContext) {
            setByteArrayAttribute("bytes", linkBytes)
        }
        val data = span.toReadableSpan().toSpanData()
        eventList.add(false)
        linkBytes[0] = 9
        assertEquals(listOf(true), data.events.single().attributes["flags"])
        assertEquals(1.toByte(), (data.links.single().attributes["bytes"] as ByteArray)[0])
    }

    @Test
    fun testToSpanDataEventsAndLinksAreNotMutators() {
        val span = tracer.startSpan("test").apply {
            addEvent("event") { setStringAttribute("k", "v") }
            addLink(fakeSpanContext) { setStringAttribute("k", "v") }
            end()
        }
        val data = processor.endCalls.single()
        assertFalse(data.events.single() is AttributesMutator)
        assertFalse(data.links.single() is AttributesMutator)
    }

    private fun simulateSpan(): ReadableSpan {
        return tracer.startSpan(
            name = "test",
            spanKind = SpanKind.CLIENT,
            startTimestamp = 5,
        ).apply {
            setStatus(StatusData.Error("Whoops"))
            setStringAttribute("string", "value")
            addEvent("event", 10) {
                setStringAttribute("string", "value")
            }
            addLink(fakeSpanContext) {
                setStringAttribute("string", "value")
            }
            end()
        }.toReadableSpan()
    }

    private fun assertSpanData(
        span: ReadableSpan,
        data: SpanData
    ) {
        assertEquals(span.name, data.name)
        assertEquals(span.spanKind, data.spanKind)
        assertEquals(span.startTimestamp, data.startTimestamp)
        assertEquals(span.status, data.status)
        assertEquals(span.attributes, data.attributes)
        assertEquals(span.events.size, data.events.size)
        span.events.zip(data.events).forEach { (expected, actual) ->
            assertEquals(expected.name, actual.name)
            assertEquals(expected.timestamp, actual.timestamp)
            assertEquals(expected.attributes, actual.attributes)
        }
        assertEquals(span.droppedEventsCount, data.droppedEventsCount)
        assertEquals(span.links.size, data.links.size)
        span.links.zip(data.links).forEach { (expected, actual) ->
            assertEquals(expected.spanContext, actual.spanContext)
            assertEquals(expected.attributes, actual.attributes)
        }
        assertEquals(span.droppedLinksCount, data.droppedLinksCount)
        assertSame(fakeResource, data.resource)
        assertSame(key, data.instrumentationScopeInfo)
    }
}
