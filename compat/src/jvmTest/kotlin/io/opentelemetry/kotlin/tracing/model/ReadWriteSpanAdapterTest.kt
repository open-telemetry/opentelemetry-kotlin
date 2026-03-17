package io.opentelemetry.kotlin.tracing.model

import fakeInProgressOtelJavaSpanData
import fakeOtelJavaEventData
import fakeOtelJavaLinkData
import io.opentelemetry.kotlin.aliases.OtelJavaAttributeKey
import io.opentelemetry.kotlin.aliases.OtelJavaAttributes
import io.opentelemetry.kotlin.aliases.OtelJavaSpan
import io.opentelemetry.kotlin.aliases.OtelJavaSpanData
import io.opentelemetry.kotlin.aliases.OtelJavaStatusData
import io.opentelemetry.kotlin.attributes.attrsFromMap
import io.opentelemetry.kotlin.attributes.convertToMap
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.context.toOtelJavaContext
import io.opentelemetry.kotlin.fakes.otel.java.FakeOtelJavaReadWriteSpan
import io.opentelemetry.kotlin.fakes.otel.java.FakeOtelJavaReadableSpan
import io.opentelemetry.kotlin.fakes.otel.java.FakeOtelJavaSpanData
import io.opentelemetry.kotlin.framework.OtelKotlinHarness
import io.opentelemetry.kotlin.scope.toOtelJavaInstrumentationScopeInfo
import io.opentelemetry.kotlin.tracing.data.SpanEventData
import io.opentelemetry.kotlin.tracing.data.SpanLinkData
import io.opentelemetry.kotlin.tracing.data.StatusData
import io.opentelemetry.kotlin.tracing.export.FakeSpanProcessor
import io.opentelemetry.kotlin.tracing.ext.toOtelJavaEventData
import io.opentelemetry.kotlin.tracing.ext.toOtelJavaLinkData
import io.opentelemetry.kotlin.tracing.ext.toOtelJavaSpanContext
import io.opentelemetry.kotlin.tracing.ext.toOtelJavaSpanKind
import io.opentelemetry.kotlin.tracing.ext.toOtelJavaStatusData
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.BeforeTest
import kotlin.test.assertEquals

internal class ReadWriteSpanAdapterTest {

    private lateinit var harness: OtelKotlinHarness
    private lateinit var adapter: ReadWriteSpanAdapter
    private lateinit var fakeReadableSpan: FakeOtelJavaReadableSpan
    private lateinit var fakeImpl: FakeOtelJavaReadWriteSpan

    @BeforeTest
    fun setUp() = runTest {
        fakeReadableSpan = FakeOtelJavaReadableSpan(fakeInProgressOtelJavaSpanData)
        fakeImpl = FakeOtelJavaReadWriteSpan(fakeReadableSpan)
        adapter = ReadWriteSpanAdapter(fakeImpl)
        harness = OtelKotlinHarness(testScheduler)
    }

    @Test
    fun `pass through of initial state`() {
        with(fakeImpl.toSpanData()) {
            adapter.assertImmutableProperties(this)
            adapter.assertMutableProperties(this)
        }
    }

    @Test
    fun `mutable properties change as implementation changes`() {
        val initialState = fakeImpl.toSpanData()
        fakeReadableSpan.otelJavaSpanData = FakeOtelJavaSpanData(
            implName = "new${initialState.name}",
            implSpanContext = initialState.spanContext,
            implParentSpanContext = initialState.parentSpanContext,
            implSpanKind = initialState.kind,
            implAttributes = attrsFromMap(initialState.attributes.convertToMap() + mapOf("newattr" to "value")),
            implEventData = initialState.events + fakeOtelJavaEventData,
            implLinkData = initialState.links + fakeOtelJavaLinkData,
            implStartNs = initialState.startEpochNanos,
            implEndNs = initialState.startEpochNanos + 5_000_000,
            implEnded = true,
            implStatusData = OtelJavaStatusData.error(),
            implResource = initialState.resource
        )

        val modifiedState = fakeImpl.toSpanData()

        adapter.assertImmutableProperties(initialState)
        adapter.assertMutableProperties(modifiedState)
    }

    @Test
    fun `span updatable`() = runTest {
        val newStatus = StatusData.Error("err")
        val processor = FakeSpanProcessor(
            startAction = assertReadWriteSpan(
                updateCode = { span ->
                    span.apply {
                        setName("new-name")
                        setStatus(newStatus)
                        setStringAttribute("key", "value")
                    }
                },
                expectedName = "new-name",
                expectedStatus = StatusData.Error("err"),
                expectedAttributes = mapOf("key" to "value")
            ),
            endAction = assertReadableSpan(
                expectedName = "name",
                expectedStatus = StatusData.Error("err"),
                expectedAttributes = mapOf("key" to "value")
            ),
        )
        harness.config.spanProcessors.add(processor)
        harness.tracer.startSpan("name").end()
        harness.assertSpans(
            expectedCount = 1,
            assertions = { spans ->
                with(spans.single()) {
                    assertEquals("name", name)
                    assertEquals(newStatus.statusCode.name, status.statusCode.name)
                    assertEquals(newStatus.description, status.description)
                    assertEquals(mapOf("key" to "value"), attributes)
                }
            }
        )
    }

    @Test
    fun `recordException empty attrs`() {
        val exception = IllegalStateException("something went wrong")
        adapter.recordException(exception)

        val (recordedException, recordedAttrs) = fakeImpl.recordedExceptions.single()
        assertEquals(exception, recordedException)
        assertEquals(OtelJavaAttributes.empty(), recordedAttrs)
    }

    @Test
    fun `recordException extra attrs`() {
        val exception = RuntimeException("oops")
        adapter.recordException(exception) {
            setStringAttribute("custom.key", "custom.value")
        }

        val (recordedException, recordedAttrs) = fakeImpl.recordedExceptions.single()
        assertEquals(exception, recordedException)
        assertEquals("custom.value", recordedAttrs.get(OtelJavaAttributeKey.stringKey("custom.key")))
    }

    @Test
    fun `recordException null attrs`() {
        val exception = RuntimeException("error")
        adapter.recordException(exception, null)

        val (recordedException, recordedAttrs) = fakeImpl.recordedExceptions.single()
        assertEquals(exception, recordedException)
        assertEquals(OtelJavaAttributes.empty(), recordedAttrs)
    }

    private fun ReadWriteSpanAdapter.assertImmutableProperties(expected: OtelJavaSpanData) {
        assertEquals(expected.spanContext, spanContext.toOtelJavaSpanContext())
        assertEquals(expected.parentSpanContext, parent.toOtelJavaSpanContext())
        assertEquals(expected.kind, spanKind.toOtelJavaSpanKind())
        assertEquals(expected.startEpochNanos, startTimestamp)
        assertEquals(expected.resource.attributes.convertToMap(), resource.attributes)
        assertEquals(expected.resource.schemaUrl, resource.schemaUrl)
        assertEquals(expected.instrumentationScopeInfo, instrumentationScopeInfo.toOtelJavaInstrumentationScopeInfo())
    }

    private fun ReadWriteSpanAdapter.assertMutableProperties(expected: OtelJavaSpanData) {
        assertEquals(expected.name, name)
        assertEquals(expected.status, status.toOtelJavaStatusData())
        assertEquals(expected.hasEnded(), hasEnded)
        assertEquals(expected.endEpochNanos, endTimestamp)
        assertEquals(expected.attributes.convertToMap(), attributes)
        assertEquals(expected.events, events.map { it.toOtelJavaEventData() })
        assertEquals(expected.links, links.map { it.toOtelJavaLinkData() })
    }

    private fun assertReadWriteSpan(
        updateCode: (span: ReadWriteSpan) -> Unit,
        expectedName: String? = null,
        expectedStatus: StatusData? = null,
        expectedAttributes: Map<String, Any>? = null,
        expectedEvents: List<SpanEventData>? = null,
        expectedLinks: List<SpanLinkData>? = null,
    ): (span: ReadWriteSpan, _: Context) -> Unit {
        return fun(span: ReadWriteSpan, context: Context) {
            updateCode(span)
            assertEquals(
                OtelJavaSpan.getInvalid().spanContext.spanId,
                OtelJavaSpan.fromContext(context.toOtelJavaContext()).spanContext.spanId,
            )
            assertReadableSpan(
                expectedName = expectedName,
                expectedStatus = expectedStatus,
                expectedAttributes = expectedAttributes,
                expectedEvents = expectedEvents,
                expectedLinks = expectedLinks
            ).invoke(span)
        }
    }

    private fun assertReadableSpan(
        expectedName: String? = null,
        expectedStatus: StatusData? = null,
        expectedAttributes: Map<String, Any>? = null,
        expectedEvents: List<SpanEventData>? = null,
        expectedLinks: List<SpanLinkData>? = null,
    ): (span: ReadableSpan) -> Unit {
        return fun(readableSpan: ReadableSpan) {
            with(readableSpan) {
                if (expectedName != null) {
                    assertEquals(expectedName, name)
                }

                if (expectedStatus != null) {
                    assertEquals(expectedStatus.statusCode, status.statusCode)
                    assertEquals(expectedStatus.description, status.description)
                }

                if (expectedAttributes != null) {
                    assertEquals(expectedAttributes, attributes)
                }

                if (expectedEvents != null) {
                    assertEquals(expectedEvents, events)
                }

                if (expectedLinks != null) {
                    assertEquals(expectedLinks, links)
                }
            }
        }
    }
}
