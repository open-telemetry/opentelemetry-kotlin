package io.opentelemetry.kotlin.integration.test

import io.opentelemetry.kotlin.aliases.OtelJavaAttributeKey
import io.opentelemetry.kotlin.aliases.OtelJavaBaggage
import io.opentelemetry.kotlin.aliases.OtelJavaCompletableResultCode
import io.opentelemetry.kotlin.aliases.OtelJavaContext
import io.opentelemetry.kotlin.aliases.OtelJavaExtendedSpanProcessor
import io.opentelemetry.kotlin.aliases.OtelJavaReadWriteSpan
import io.opentelemetry.kotlin.aliases.OtelJavaReadableSpan
import io.opentelemetry.kotlin.aliases.OtelJavaSpan
import io.opentelemetry.kotlin.aliases.OtelJavaSpanData
import io.opentelemetry.kotlin.aliases.OtelJavaSpanExporter
import io.opentelemetry.kotlin.aliases.OtelJavaSpanKind
import io.opentelemetry.kotlin.aliases.OtelJavaStatusCode
import io.opentelemetry.kotlin.baggage.createBaggage
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.toOtelJavaApi
import io.opentelemetry.kotlin.tracing.SpanKind
import io.opentelemetry.kotlin.tracing.StatusCode
import io.opentelemetry.kotlin.tracing.StatusData
import io.opentelemetry.kotlin.tracing.export.SpanExporter
import io.opentelemetry.kotlin.tracing.export.SpanProcessor
import io.opentelemetry.kotlin.tracing.export.toOtelKotlinSpanExporter
import io.opentelemetry.kotlin.tracing.export.toOtelKotlinSpanProcessor
import io.opentelemetry.kotlin.tracing.model.ReadWriteSpan
import io.opentelemetry.kotlin.tracing.model.ReadableSpan
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
internal class OtelJavaSpanProcessorOnKotlinSdkTest {

    private val stringKey = OtelJavaAttributeKey.stringKey("key")

    private lateinit var harness: IntegrationTestHarness

    @BeforeTest
    fun setUp() = runTest {
        harness = IntegrationTestHarness(testScheduler)
    }

    @Test
    fun testJavaProcessorReceivesKotlinApiSpans() = runTest {
        val processor = RecordingOtelJavaSpanProcessor()
        harness.config.spanProcessors.add(processor.toOtelKotlinSpanProcessor())
        harness.tracer.startSpan("my_span", spanKind = SpanKind.CLIENT) {
            setStringAttribute("key", "value")
        }.end()

        val exported = harness.exportedSpan()
        val start = processor.startCalls.single()
        assertEquals("my_span", start.name)
        assertEquals("value", start.getAttribute(stringKey))
        assertEquals(OtelJavaSpanKind.CLIENT, start.kind)
        assertEquals("test_tracer", start.instrumentationScopeInfo.name)
        assertEquals(exported.spanContext.spanId, start.spanContext.spanId)
        assertEquals(exported.spanContext.traceId, start.spanContext.traceId)
        assertFalse(start.parentSpanContext.isValid)

        val ending = processor.endingCalls.single()
        assertEquals("my_span", ending.name)

        val end = processor.endCalls.single()
        assertEquals("my_span", end.name)
        assertTrue(end.hasEnded())
        assertEquals(exported.spanContext.spanId, end.toSpanData().spanId)
        assertEquals("value", end.toSpanData().attributes.get(stringKey))
    }

    @Test
    fun testJavaProcessorReceivesJavaApiSpans() = runTest {
        val processor = RecordingOtelJavaSpanProcessor()
        harness.config.spanProcessors.add(processor.toOtelKotlinSpanProcessor())

        val tracer = harness.kotlinApi.toOtelJavaApi().tracerProvider.get("tracer")
        val parent = tracer.spanBuilder("parent").startSpan()
        val child = tracer.spanBuilder("child")
            .setParent(OtelJavaContext.root().with(parent))
            .startSpan()
        child.end()
        parent.end()

        assertEquals(listOf("parent", "child"), processor.startCalls.map { it.name })
        assertEquals(listOf("child", "parent"), processor.endCalls.map { it.name })
        val childStart = processor.startCalls[1]
        assertEquals(parent.spanContext.spanId, childStart.parentSpanContext.spanId)
        assertEquals(parent.spanContext.traceId, childStart.spanContext.traceId)
    }

    @Test
    fun testJavaProcessorReadsKotlinParentAndBaggage() = runTest {
        val processor = RecordingOtelJavaSpanProcessor()
        harness.config.spanProcessors.add(processor.toOtelKotlinSpanProcessor())
        val parent = harness.tracer.startSpan("parent")
        val parentContext = harness.kotlinApi.context.root()
            .storeSpan(parent)
            .storeBaggage(createBaggage { put("key", "value") })
        harness.tracer.startSpan("child", parentContext = parentContext).end()
        parent.end()

        val childParentContext = processor.startContexts[1]
        val javaParent = OtelJavaSpan.fromContext(childParentContext)
        assertEquals(parent.spanContext.spanId, javaParent.spanContext.spanId)
        assertEquals(parent.spanContext.traceId, javaParent.spanContext.traceId)
        assertEquals("value", OtelJavaBaggage.fromContext(childParentContext).getEntryValue("key"))
        assertFalse(OtelJavaSpan.fromContext(processor.startContexts[0]).spanContext.isValid)
    }

    @Test
    fun testInFlightLatencyUsesSdkClock() = runTest {
        val processor = RecordingOtelJavaSpanProcessor()
        harness.config.spanProcessors.add(processor.toOtelKotlinSpanProcessor())
        harness.fakeClock.time = 1000
        val span = harness.tracer.startSpan("my_span")
        harness.fakeClock.time = 1600
        assertEquals(600L, processor.startCalls.single().latencyNanos)
        span.end()
    }

    @Test
    fun testJavaProcessorMutationsAreExported() = runTest {
        val processor = RecordingOtelJavaSpanProcessor(
            startAction = { span ->
                span.updateName("renamed")
                span.setAttribute("start", "yes")
            },
            endingAction = { span ->
                span.setAttribute("ending", "yes")
                span.setStatus(OtelJavaStatusCode.ERROR, "whoops")
            },
        )
        harness.config.spanProcessors.add(processor.toOtelKotlinSpanProcessor())
        harness.tracer.startSpan("my_span").end()

        val exported = harness.exportedSpan()
        assertEquals("renamed", exported.name)
        assertEquals(mapOf("start" to "yes", "ending" to "yes"), exported.attributes)
        assertEquals(StatusCode.ERROR, exported.status.statusCode)
        assertEquals("whoops", exported.status.description)
    }

    @Test
    fun testJavaExporterReceivesSpansFromKotlinProcessor() = runTest {
        val exporter = RecordingOtelJavaSpanExporter()
        val scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler))
        harness.config.spanProcessors.add(ExportingSpanProcessor(exporter.toOtelKotlinSpanExporter(), scope))
        harness.tracer.startSpan("my_span") {
            setStringAttribute("key", "value")
        }.apply {
            addEvent("my_event")
            setStatus(StatusData.Error("whoops"))
            end()
        }
        assertJavaSpanDataMatches(harness.exportedSpan(), exporter.exports.single())
    }

    private fun assertJavaSpanDataMatches(
        expected: io.opentelemetry.kotlin.tracing.data.SpanData,
        observed: OtelJavaSpanData,
    ) {
        assertEquals(expected.name, observed.name)
        assertEquals(expected.spanContext.traceId, observed.traceId)
        assertEquals(expected.spanContext.spanId, observed.spanId)
        assertEquals("value", observed.attributes.get(stringKey))
        assertEquals("my_event", observed.events.single().name)
        assertEquals(OtelJavaStatusCode.ERROR, observed.status.statusCode)
        assertEquals("whoops", observed.status.description)
        assertEquals(expected.startTimestamp, observed.startEpochNanos)
        assertEquals(expected.endTimestamp, observed.endEpochNanos)
        assertEquals("test_tracer", observed.instrumentationScopeInfo.name)
        assertTrue(observed.hasEnded())
    }

    private fun IntegrationTestHarness.exportedSpan(): io.opentelemetry.kotlin.tracing.data.SpanData {
        var span: io.opentelemetry.kotlin.tracing.data.SpanData? = null
        assertSpans(1) { span = it.single() }
        return checkNotNull(span)
    }

    private class RecordingOtelJavaSpanProcessor(
        private val startAction: (OtelJavaReadWriteSpan) -> Unit = {},
        private val endingAction: (OtelJavaReadWriteSpan) -> Unit = {},
    ) : OtelJavaExtendedSpanProcessor {

        val startCalls = mutableListOf<OtelJavaReadWriteSpan>()
        val startContexts = mutableListOf<OtelJavaContext>()
        val endingCalls = mutableListOf<OtelJavaReadWriteSpan>()
        val endCalls = mutableListOf<OtelJavaReadableSpan>()

        override fun onStart(parentContext: OtelJavaContext, span: OtelJavaReadWriteSpan) {
            startCalls += span
            startContexts += parentContext
            startAction(span)
        }

        override fun onEnding(span: OtelJavaReadWriteSpan) {
            endingCalls += span
            endingAction(span)
        }

        override fun onEnd(span: OtelJavaReadableSpan) {
            endCalls += span
        }

        override fun isStartRequired(): Boolean = true
        override fun isEndRequired(): Boolean = true
        override fun isOnEndingRequired(): Boolean = true
    }

    private class RecordingOtelJavaSpanExporter : OtelJavaSpanExporter {
        val exports = mutableListOf<OtelJavaSpanData>()

        override fun export(spans: MutableCollection<OtelJavaSpanData>): OtelJavaCompletableResultCode {
            exports += spans
            return OtelJavaCompletableResultCode.ofSuccess()
        }

        override fun flush(): OtelJavaCompletableResultCode = OtelJavaCompletableResultCode.ofSuccess()
        override fun shutdown(): OtelJavaCompletableResultCode = OtelJavaCompletableResultCode.ofSuccess()
    }

    private class ExportingSpanProcessor(
        private val exporter: SpanExporter,
        private val scope: CoroutineScope,
    ) : SpanProcessor {
        override fun onStart(span: ReadWriteSpan, parentContext: Context) {}
        override fun onEnding(span: ReadWriteSpan) {}
        override fun onEnd(span: ReadableSpan) {
            scope.launch { exporter.export(listOf(span.toSpanData())) }
        }

        override fun isStartRequired(): Boolean = false
        override fun isEndRequired(): Boolean = true
        override fun isOnEndingRequired(): Boolean = false
        override suspend fun forceFlush(): OperationResultCode = exporter.forceFlush()
        override suspend fun shutdown(): OperationResultCode = exporter.shutdown()
    }
}
