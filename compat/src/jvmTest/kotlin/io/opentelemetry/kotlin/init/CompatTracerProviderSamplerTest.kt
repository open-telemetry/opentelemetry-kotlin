package io.opentelemetry.kotlin.init

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.clock.FakeClock
import io.opentelemetry.kotlin.createCompatOpenTelemetry
import io.opentelemetry.kotlin.error.NoopSdkErrorHandler
import io.opentelemetry.kotlin.factory.CompatIdGenerator
import io.opentelemetry.kotlin.tracing.export.FakeSpanProcessor
import io.opentelemetry.kotlin.tracing.export.compositeSpanProcessor
import io.opentelemetry.kotlin.tracing.sampling.FakeSampler
import io.opentelemetry.kotlin.tracing.sampling.SamplingResult
import io.opentelemetry.kotlin.tracing.sampling.alwaysOff
import io.opentelemetry.kotlin.tracing.sampling.alwaysOn
import io.opentelemetry.kotlin.tracing.sampling.composableAlwaysOff
import io.opentelemetry.kotlin.tracing.sampling.composableAlwaysOn
import io.opentelemetry.kotlin.tracing.sampling.composableParentThreshold
import io.opentelemetry.kotlin.tracing.sampling.composableProbability
import io.opentelemetry.kotlin.tracing.sampling.composite
import io.opentelemetry.kotlin.tracing.sampling.parentBased
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalApi::class)
internal class CompatTracerProviderSamplerTest {

    private val idGenerator = CompatIdGenerator()

    @Test
    fun `default sampler records and samples spans`() {
        val clock = FakeClock()
        val config = CompatTracerProviderConfig(clock, NoopSdkErrorHandler)
        val provider = config.build(clock, idGenerator)
        val span = provider.getTracer("test").startSpan("span")
        assertTrue(span.isRecording())
        assertTrue(span.spanContext.traceFlags.isSampled)
    }

    @Test
    fun `builtin ALWAYS_ON sampler records and samples spans`() {
        val clock = FakeClock()
        val config = CompatTracerProviderConfig(clock, NoopSdkErrorHandler).apply {
            sampler { alwaysOn() }
        }
        val span = config.build(clock, idGenerator).getTracer("test").startSpan("span")
        assertTrue(span.isRecording())
        assertTrue(span.spanContext.traceFlags.isSampled)
    }

    @Test
    fun `custom sampler DROP produces non-recording span`() {
        val clock = FakeClock()
        val config = CompatTracerProviderConfig(clock, NoopSdkErrorHandler).apply {
            sampler { FakeSampler(SamplingResult.Decision.DROP) }
        }
        val span = config.build(clock, idGenerator).getTracer("test").startSpan("span")
        assertFalse(span.isRecording())
    }

    @Test
    fun `custom sampler RECORD_AND_SAMPLE produces recording and sampled span`() {
        val clock = FakeClock()
        val config = CompatTracerProviderConfig(clock, NoopSdkErrorHandler).apply {
            sampler { FakeSampler(SamplingResult.Decision.RECORD_AND_SAMPLE) }
        }
        val span = config.build(clock, idGenerator).getTracer("test").startSpan("span")
        assertTrue(span.isRecording())
        assertTrue(span.spanContext.traceFlags.isSampled)
    }

    @Test
    fun `builtin ALWAYS_OFF sampler drops spans`() {
        val clock = FakeClock()
        val config = CompatTracerProviderConfig(clock, NoopSdkErrorHandler).apply {
            sampler { alwaysOff() }
        }
        val span = config.build(clock, idGenerator).getTracer("test").startSpan("span")
        assertFalse(span.isRecording())
        assertFalse(span.spanContext.traceFlags.isSampled)
    }

    @Test
    fun `builtin ALWAYS_ON sampler exports span`() {
        val processor = FakeSpanProcessor()
        val sdk = createCompatOpenTelemetry {
            tracerProvider {
                sampler { alwaysOn() }
                export { compositeSpanProcessor(processor) }
            }
        }
        sdk.tracerProvider.getTracer("test").startSpan("span").end()
        assertEquals(1, processor.endCalls.size)
    }

    @Test
    fun `parentBased samples root spans with alwaysOn`() {
        val clock = FakeClock()
        val config = CompatTracerProviderConfig(clock, NoopSdkErrorHandler).apply {
            sampler { parentBased(root = alwaysOn()) }
        }
        val span = config.build(clock, idGenerator).getTracer("test").startSpan("span")
        assertTrue(span.isRecording())
        assertTrue(span.spanContext.traceFlags.isSampled)
    }

    @Test
    fun `parentBased drops root spans with alwaysOff`() {
        val clock = FakeClock()
        val config = CompatTracerProviderConfig(clock, NoopSdkErrorHandler).apply {
            sampler { parentBased(root = alwaysOff()) }
        }
        val span = config.build(clock, idGenerator).getTracer("test").startSpan("span")
        assertFalse(span.isRecording())
        assertFalse(span.spanContext.traceFlags.isSampled)
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

    @Test
    fun `composite with composableAlwaysOn samples spans`() {
        val clock = FakeClock()
        val config = CompatTracerProviderConfig(clock, NoopSdkErrorHandler).apply {
            sampler { composite(composableAlwaysOn()) }
        }
        val span = config.build(clock, idGenerator).getTracer("test").startSpan("span")
        assertTrue(span.isRecording())
        assertTrue(span.spanContext.traceFlags.isSampled)
    }

    @Test
    fun `composite with composableAlwaysOff drops spans`() {
        val clock = FakeClock()
        val config = CompatTracerProviderConfig(clock, NoopSdkErrorHandler).apply {
            sampler { composite(composableAlwaysOff()) }
        }
        val span = config.build(clock, idGenerator).getTracer("test").startSpan("span")
        assertFalse(span.isRecording())
        assertFalse(span.spanContext.traceFlags.isSampled)
    }

    @Test
    fun `composite with composableProbability 1_0 samples spans`() {
        val clock = FakeClock()
        val config = CompatTracerProviderConfig(clock, NoopSdkErrorHandler).apply {
            sampler { composite(composableProbability(1.0)) }
        }
        val span = config.build(clock, idGenerator).getTracer("test").startSpan("span")
        assertTrue(span.isRecording())
        assertTrue(span.spanContext.traceFlags.isSampled)
    }

    @Test
    fun `composite with composableProbability 0_0 drops spans`() {
        val clock = FakeClock()
        val config = CompatTracerProviderConfig(clock, NoopSdkErrorHandler).apply {
            sampler { composite(composableProbability(0.0)) }
        }
        val span = config.build(clock, idGenerator).getTracer("test").startSpan("span")
        assertFalse(span.isRecording())
        assertFalse(span.spanContext.traceFlags.isSampled)
    }

    @Test
    fun `composite with composableParentThreshold falls back to root on root spans`() {
        val clock = FakeClock()
        val config = CompatTracerProviderConfig(clock, NoopSdkErrorHandler).apply {
            sampler { composite(composableParentThreshold(root = composableAlwaysOn())) }
        }
        val span = config.build(clock, idGenerator).getTracer("test").startSpan("span")
        assertTrue(span.isRecording())
        assertTrue(span.spanContext.traceFlags.isSampled)
    }

    @Test
    fun `composite with composableParentThreshold drops root spans when root is composableAlwaysOff`() {
        val clock = FakeClock()
        val config = CompatTracerProviderConfig(clock, NoopSdkErrorHandler).apply {
            sampler { composite(composableParentThreshold(root = composableAlwaysOff())) }
        }
        val span = config.build(clock, idGenerator).getTracer("test").startSpan("span")
        assertFalse(span.isRecording())
        assertFalse(span.spanContext.traceFlags.isSampled)
    }
}
