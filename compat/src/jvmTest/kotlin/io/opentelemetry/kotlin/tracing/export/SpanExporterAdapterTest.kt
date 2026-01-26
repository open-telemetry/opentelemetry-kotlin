package io.opentelemetry.kotlin.tracing.export

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.attributes.convertToMap
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.fakes.otel.java.FakeOtelJavaSpanExporter
import io.opentelemetry.kotlin.tracing.data.FakeSpanData
import io.opentelemetry.kotlin.tracing.ext.toOtelJavaSpanKind
import io.opentelemetry.kotlin.tracing.ext.toOtelJavaStatusCode
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalApi::class)
internal class SpanExporterAdapterTest {

    private lateinit var impl: FakeOtelJavaSpanExporter
    private lateinit var wrapper: SpanExporterAdapter

    @Before
    fun setUp() {
        impl = FakeOtelJavaSpanExporter()
        wrapper = SpanExporterAdapter(impl)
    }

    @Test
    fun `test flush`() = runTest {
        assertEquals(OperationResultCode.Success, wrapper.forceFlush())
        assertEquals(1, impl.flushCount)
    }

    @Test
    fun `test shutdown`() = runTest {
        assertEquals(OperationResultCode.Success, wrapper.shutdown())
        assertEquals(1, impl.shutdownCount)
    }

    @Test
    fun `test export`() = runTest {
        val original = FakeSpanData()
        assertEquals(OperationResultCode.Success, wrapper.export(listOf(original)))

        val observed = impl.exports.single()
        assertEquals(original.name, observed.name)
        assertEquals(original.status.statusCode.toOtelJavaStatusCode(), observed.status.statusCode)
        assertEquals(original.parent.spanId, observed.parentSpanContext.spanId)
        assertEquals(original.spanContext.spanId, observed.spanContext.spanId)
        assertEquals(original.spanKind.toOtelJavaSpanKind(), observed.kind)
        assertEquals(original.startTimestamp, observed.startEpochNanos)
        assertEquals(original.attributes, observed.attributes.convertToMap())
        assertEquals(original.resource.attributes, observed.resource.attributes.convertToMap())

        val originalScope = original.instrumentationScopeInfo
        val observedScope = observed.instrumentationScopeInfo
        assertEquals(originalScope.name, observedScope.name)
        assertEquals(originalScope.version, observedScope.version)
        assertEquals(originalScope.schemaUrl, observedScope.schemaUrl)
        assertEquals(
            emptyMap(),
            observedScope.attributes.convertToMap()
        ) // otel-java don't support this

        val originalEvent = original.events.single()
        val observedEvent = observed.events.single()
        assertEquals(originalEvent.name, observedEvent.name)
        assertEquals(originalEvent.attributes, observedEvent.attributes.convertToMap())

        val originalLink = original.links.single()
        val observedLink = observed.links.single()
        assertEquals(originalLink.spanContext.spanId, observedLink.spanContext.spanId)
        assertEquals(originalLink.attributes, observedLink.attributes.convertToMap())
    }
}
