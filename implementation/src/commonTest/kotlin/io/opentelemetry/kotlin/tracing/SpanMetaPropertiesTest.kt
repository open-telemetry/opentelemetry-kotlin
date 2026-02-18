package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.InstrumentationScopeInfoImpl
import io.opentelemetry.kotlin.clock.FakeClock
import io.opentelemetry.kotlin.factory.FakeSdkFactory
import io.opentelemetry.kotlin.resource.FakeResource
import io.opentelemetry.kotlin.tracing.export.FakeSpanProcessor
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertSame

@OptIn(ExperimentalApi::class)
internal class SpanMetaPropertiesTest {

    private val key = InstrumentationScopeInfoImpl("key", null, null, emptyMap())
    private val fakeResource = FakeResource()
    private lateinit var tracer: TracerImpl
    private lateinit var clock: FakeClock
    private lateinit var processor: FakeSpanProcessor

    @BeforeTest
    fun setUp() {
        clock = FakeClock()
        processor = FakeSpanProcessor()
        tracer = TracerImpl(
            clock,
            processor,
            FakeSdkFactory(),
            key,
            fakeResource,
            fakeSpanLimitsConfig,
        )
    }

    @Test
    fun testSpanInstrumentationScope() {
        tracer.startSpan("test").end()
        val scope = processor.endCalls.single().instrumentationScopeInfo
        assertSame(key, scope)
    }

    @Test
    fun testSpanResource() {
        tracer.startSpan("test").end()
        val resource = processor.endCalls.single().resource
        assertSame(fakeResource, resource)
    }
}
