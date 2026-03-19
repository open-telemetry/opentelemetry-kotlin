package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.clock.FakeClock
import io.opentelemetry.kotlin.createCompatOpenTelemetry
import io.opentelemetry.kotlin.factory.CompatIdGenerator
import io.opentelemetry.kotlin.tracing.export.FakeSpanProcessor
import io.opentelemetry.kotlin.tracing.export.compositeSpanProcessor
import io.opentelemetry.kotlin.tracing.sampling.BuiltInSampler
import io.opentelemetry.kotlin.tracing.sampling.FakeSampler
import io.opentelemetry.kotlin.tracing.sampling.SamplingResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalApi::class)
internal class CompatTracerProviderSamplerTest {

    @Test
    fun `default sampler records and samples spans`() {
        val clock = FakeClock()
        val config = CompatTracerProviderConfig(clock, CompatIdGenerator())
        val provider = config.build(clock)
        val span = provider.getTracer("test").startSpan("span")
        assertTrue(span.isRecording())
        assertTrue(span.spanContext.traceFlags.isSampled)
    }

    @Test
    fun `builtin ALWAYS_ON sampler records and samples spans`() {
        val clock = FakeClock()
        val config = CompatTracerProviderConfig(clock, CompatIdGenerator()).apply {
            sampler(BuiltInSampler.ALWAYS_ON)
        }
        val span = config.build(clock).getTracer("test").startSpan("span")
        assertTrue(span.isRecording())
        assertTrue(span.spanContext.traceFlags.isSampled)
    }

    @Test
    fun `custom sampler DROP produces non-recording span`() {
        val clock = FakeClock()
        val config = CompatTracerProviderConfig(clock, CompatIdGenerator()).apply {
            sampler { FakeSampler(SamplingResult.Decision.DROP) }
        }
        val span = config.build(clock).getTracer("test").startSpan("span")
        assertFalse(span.isRecording())
    }

    @Test
    fun `custom sampler RECORD_AND_SAMPLE produces recording and sampled span`() {
        val clock = FakeClock()
        val config = CompatTracerProviderConfig(clock, CompatIdGenerator()).apply {
            sampler { FakeSampler(SamplingResult.Decision.RECORD_AND_SAMPLE) }
        }
        val span = config.build(clock).getTracer("test").startSpan("span")
        assertTrue(span.isRecording())
        assertTrue(span.spanContext.traceFlags.isSampled)
    }

    @Test
    fun `builtin ALWAYS_OFF sampler drops spans`() {
        val clock = FakeClock()
        val config = CompatTracerProviderConfig(clock, CompatIdGenerator()).apply {
            sampler(BuiltInSampler.ALWAYS_OFF)
        }
        val span = config.build(clock).getTracer("test").startSpan("span")
        assertFalse(span.isRecording())
        assertFalse(span.spanContext.traceFlags.isSampled)
    }

    @Test
    fun `builtin ALWAYS_ON sampler exports span`() {
        val processor = FakeSpanProcessor()
        val sdk = createCompatOpenTelemetry {
            tracerProvider {
                sampler(BuiltInSampler.ALWAYS_ON)
                export { compositeSpanProcessor(processor) }
            }
        }
        sdk.tracerProvider.getTracer("test").startSpan("span").end()
        assertEquals(1, processor.endCalls.size)
    }

    @Test
    fun `custom sampler DROP produces non-recording span and nothing is exported`() {
        val processor = FakeSpanProcessor()
        val sdk = createCompatOpenTelemetry {
            tracerProvider {
                sampler { FakeSampler(SamplingResult.Decision.DROP) }
                export { compositeSpanProcessor(processor) }
            }
        }
        val span = sdk.tracerProvider.getTracer("test").startSpan("span")
        assertFalse(span.isRecording())
        span.end()
        assertEquals(0, processor.endCalls.size)
    }
}
