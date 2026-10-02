package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.OpenTelemetry
import io.opentelemetry.kotlin.attributes.AttributeContainer
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.createCompatOpenTelemetry
import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import io.opentelemetry.kotlin.error.SdkErrorSeverity
import io.opentelemetry.kotlin.tracing.export.FakeSpanProcessor
import io.opentelemetry.kotlin.tracing.model.SpanLink
import io.opentelemetry.kotlin.tracing.sampling.Sampler
import io.opentelemetry.kotlin.tracing.sampling.SamplingResult
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
internal class CompatTracerErrorHandlingTest {

    private val errorHandler = FakeSdkErrorHandler()

    @Test
    fun `throwing span creation action degrades to a non-recording span`() {
        val processor = FakeSpanProcessor()
        val tracer = createOtel(processor).tracerProvider.getTracer("test")
        val span = tracer.startSpan("span") { boom() }

        assertIs<NonRecordingSpan>(span)
        assertFalse(span.spanContext.isValid)
        assertTrue(processor.startCalls.isEmpty())
        assertSingleError("Tracer.startSpan failed")
    }

    @Test
    fun `failed span creation preserves the parent span context`() {
        val otel = createOtel(FakeSpanProcessor())
        val tracer = otel.tracerProvider.getTracer("test")
        val parent = tracer.startSpan("parent")
        val parentCtx = otel.context.root().storeSpan(parent)
        val span = tracer.startSpan("child", parentContext = parentCtx) { boom() }

        assertFalse(span.isRecording())
        assertEquals(parent.spanContext.traceId, span.spanContext.traceId)
        assertEquals(parent.spanContext.spanId, span.spanContext.spanId)
        assertSingleError("Tracer.startSpan failed")
    }

    @Test
    fun `throwing sampler degrades to a non-recording span`() {
        val otel = createCompatOpenTelemetry {
            errorHandler(errorHandler)
            tracerProvider { sampler { HostileSampler() } }
        }
        val span = otel.tracerProvider.getTracer("test").startSpan("span")

        assertFalse(span.isRecording())
        assertSingleError("Tracer.startSpan failed")
    }

    @Test
    fun `throwing onStart does not escape and other processors still run`() {
        val hostile = FakeSpanProcessor(startAction = { _, _ -> boom() })
        val healthy = FakeSpanProcessor()
        val tracer = createOtel(hostile, healthy).tracerProvider.getTracer("test")
        val span = tracer.startSpan("span")

        assertTrue(span.isRecording())
        assertEquals(1, healthy.startCalls.size)
        assertSingleError("SpanProcessor.onStart failed")
    }

    @Test
    fun `throwing onEnding and onEnd do not escape and other processors still run`() {
        val hostile = FakeSpanProcessor(endingAction = { boom() }, endAction = { boom() })
        val healthy = FakeSpanProcessor()
        val tracer = createOtel(hostile, healthy).tracerProvider.getTracer("test")
        tracer.startSpan("span").end()

        assertEquals(1, healthy.endingCalls.size)
        assertEquals(1, healthy.endCalls.size)
        assertEquals(
            listOf("SpanProcessor.onEnding failed", "SpanProcessor.onEnd failed"),
            errorHandler.userCodeErrors.map { it.message }
        )
    }

    @Test
    fun `throwing event and link attribute lambdas do not escape`() {
        val tracer = createOtel(FakeSpanProcessor()).tracerProvider.getTracer("test")
        val span = tracer.startSpan("span")
        span.addEvent("event") { boom() }
        span.addLink(span.spanContext) { boom() }

        assertEquals(
            listOf("Span.addEvent failed", "Span.addLink failed"),
            errorHandler.userCodeErrors.map { it.message }
        )
        assertTrue(span.isRecording())
    }

    @Test
    fun `throwing scope attributes lambda degrades to a noop tracer`() {
        val tracerProvider = createOtel(FakeSpanProcessor()).tracerProvider
        val tracer = tracerProvider.getTracer("test") { boom() }

        assertFalse(tracer.startSpan("span").isRecording())
        assertSingleError("TracerProvider.getTracer failed")
    }

    private fun createOtel(vararg processors: FakeSpanProcessor): OpenTelemetry =
        createCompatOpenTelemetry {
            errorHandler(errorHandler)
            tracerProvider {
                processors.forEach { processor -> export { processor } }
            }
        }

    private fun assertSingleError(message: String) {
        assertEquals(1, errorHandler.errors.size)
        val error = errorHandler.userCodeErrors.single()
        assertEquals(message, error.message)
        assertEquals(SdkErrorSeverity.WARNING, error.severity)
        assertEquals("boom", error.cause.message)
    }

    private fun boom(): Nothing = error("boom")

    private inner class HostileSampler : Sampler {
        override fun shouldSample(
            context: Context,
            traceIdBytes: ByteArray,
            name: String,
            spanKind: SpanKind,
            attributes: AttributeContainer,
            links: List<SpanLink>,
        ): SamplingResult = boom()

        override val description: String = "HostileSampler"
    }
}
