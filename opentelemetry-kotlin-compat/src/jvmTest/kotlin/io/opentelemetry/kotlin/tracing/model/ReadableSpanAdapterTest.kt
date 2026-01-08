package io.opentelemetry.kotlin.tracing.model

import fakeInProgressOtelJavaSpanData
import fakeOtelJavaEventData
import fakeOtelJavaLinkData
import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.aliases.OtelJavaSpanData
import io.opentelemetry.kotlin.aliases.OtelJavaStatusData
import io.opentelemetry.kotlin.attributes.attrsFromMap
import io.opentelemetry.kotlin.attributes.convertToMap
import io.opentelemetry.kotlin.fakes.otel.java.FakeOtelJavaReadableSpan
import io.opentelemetry.kotlin.fakes.otel.java.FakeOtelJavaSpanData
import io.opentelemetry.kotlin.scope.toOtelJavaInstrumentationScopeInfo
import io.opentelemetry.kotlin.tracing.ext.toOtelJavaEventData
import io.opentelemetry.kotlin.tracing.ext.toOtelJavaLinkData
import io.opentelemetry.kotlin.tracing.ext.toOtelJavaSpanContext
import io.opentelemetry.kotlin.tracing.ext.toOtelJavaSpanKind
import io.opentelemetry.kotlin.tracing.ext.toOtelJavaStatusData
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalApi::class)
internal class ReadableSpanAdapterTest {

    private lateinit var adapter: ReadableSpanAdapter
    private lateinit var fakeImpl: FakeOtelJavaReadableSpan

    @Before
    fun setUp() {
        fakeImpl = FakeOtelJavaReadableSpan(
            otelJavaSpanData = fakeInProgressOtelJavaSpanData
        )
        adapter = ReadableSpanAdapter(fakeImpl)
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
        fakeImpl.otelJavaSpanData = FakeOtelJavaSpanData(
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

    private fun ReadableSpanAdapter.assertImmutableProperties(expected: OtelJavaSpanData) {
        assertEquals(expected.spanContext, spanContext.toOtelJavaSpanContext())
        assertEquals(expected.parentSpanContext, parent.toOtelJavaSpanContext())
        assertEquals(expected.kind, spanKind.toOtelJavaSpanKind())
        assertEquals(expected.startEpochNanos, startTimestamp)
        assertEquals(expected.resource.attributes.convertToMap(), resource.attributes)
        assertEquals(expected.resource.schemaUrl, resource.schemaUrl)
        assertEquals(expected.instrumentationScopeInfo, instrumentationScopeInfo.toOtelJavaInstrumentationScopeInfo())
    }

    private fun ReadableSpanAdapter.assertMutableProperties(expected: OtelJavaSpanData) {
        assertEquals(expected.name, name)
        assertEquals(expected.status, status.toOtelJavaStatusData())
        assertEquals(expected.hasEnded(), hasEnded)
        assertEquals(expected.endEpochNanos, endTimestamp)
        assertEquals(expected.attributes.convertToMap(), attributes)
        assertEquals(expected.events, events.map { it.toOtelJavaEventData() })
        assertEquals(expected.links, links.map { it.toOtelJavaLinkData() })
    }
}
