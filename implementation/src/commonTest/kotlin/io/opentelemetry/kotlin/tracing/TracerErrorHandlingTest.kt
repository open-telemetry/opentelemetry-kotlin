package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.Clock
import io.opentelemetry.kotlin.InstrumentationScopeInfoImpl
import io.opentelemetry.kotlin.attributes.AttributeContainer
import io.opentelemetry.kotlin.clock.FakeClock
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import io.opentelemetry.kotlin.error.SdkError
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.error.SdkErrorSeverity
import io.opentelemetry.kotlin.export.MutableShutdownState
import io.opentelemetry.kotlin.factory.FakeContextFactory
import io.opentelemetry.kotlin.factory.FakeIdGenerator
import io.opentelemetry.kotlin.factory.FakeSpanContextFactory
import io.opentelemetry.kotlin.factory.FakeTraceFlagsFactory
import io.opentelemetry.kotlin.factory.IdGenerator
import io.opentelemetry.kotlin.resource.FakeResource
import io.opentelemetry.kotlin.tracing.export.FakeSpanProcessor
import io.opentelemetry.kotlin.tracing.model.SpanLink
import io.opentelemetry.kotlin.tracing.sampling.AlwaysOnSampler
import io.opentelemetry.kotlin.tracing.sampling.Sampler
import io.opentelemetry.kotlin.tracing.sampling.SamplingResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * A hostile dependency supplied by the host application must never escape [TracerImpl].
 */
internal class TracerErrorHandlingTest {

    private val key = InstrumentationScopeInfoImpl("test-tracer", null, null, emptyMap())

    private val hostileCases = listOf(
        Case("clock throws", clock = Clock { boom() }),
        Case("trace ID generation throws", idGenerator = HostileIdGenerator(traceId = true)),
        Case("span ID generation throws", idGenerator = HostileIdGenerator(spanId = true)),
        Case("trace ID randomness flag throws", idGenerator = HostileIdGenerator(randomness = true)),
        Case("sampler throws", sampler = HostileSampler()),
        Case("sampling decision throws", sampler = HostileSampler(decisionOnly = true)),
        Case("span creation action throws", action = { boom() }),
    )

    @Test
    fun testHostileDependencyDoesNotEscapeStartSpan() {
        hostileCases.forEach { case ->
            val errorHandler = FakeSdkErrorHandler()
            val processor = FakeSpanProcessor()
            val tracer = createTracer(case, processor, errorHandler)

            val span = tracer.startSpan("test-span", action = case.action)

            assertIs<NonRecordingSpan>(span, case.name)
            assertFalse(span.isRecording(), case.name)
            assertFalse(span.spanContext.isValid, case.name)
            assertTrue(processor.startCalls.isEmpty(), case.name)

            assertEquals(1, errorHandler.errors.size, case.name)
            val error = errorHandler.userCodeErrors.first()
            assertEquals("Tracer.startSpan failed", error.message, case.name)
            assertEquals(SdkErrorSeverity.WARNING, error.severity, case.name)
            assertEquals("boom", error.cause.message, case.name)
        }
    }

    @Test
    fun testTracerRemainsUsableAfterHostileAction() {
        val errorHandler = FakeSdkErrorHandler()
        val processor = FakeSpanProcessor()
        val tracer = createTracer(Case("healthy"), processor, errorHandler)

        assertFalse(tracer.startSpan("first") { boom() }.isRecording())
        assertEquals(1, errorHandler.userCodeErrors.size)

        val span = tracer.startSpan("second")
        assertTrue(span.isRecording())
        assertEquals(1, errorHandler.userCodeErrors.size)
        assertEquals(1, processor.startCalls.size)
    }

    @Test
    fun testThrowingErrorHandlerDoesNotEscapeStartSpan() {
        val tracer = createTracer(
            Case("clock throws", clock = Clock { boom() }),
            FakeSpanProcessor(),
            ThrowingSdkErrorHandler(),
        )

        val span = tracer.startSpan("test-span")

        assertFalse(span.isRecording())
        assertFalse(span.spanContext.isValid)
    }

    private fun createTracer(
        case: Case,
        processor: FakeSpanProcessor,
        errorHandler: SdkErrorHandler,
    ) = TracerImpl(
        clock = case.clock,
        processor = processor,
        contextFactory = FakeContextFactory(),
        spanContextFactory = FakeSpanContextFactory(),
        traceFlagsFactory = FakeTraceFlagsFactory(),
        scope = key,
        resource = FakeResource(),
        spanLimitConfig = fakeSpanLimitsConfig,
        idGenerator = case.idGenerator,
        shutdownState = MutableShutdownState(),
        sampler = case.sampler,
        sdkErrorHandler = errorHandler,
    )

    private class Case(
        val name: String,
        val clock: Clock = FakeClock(),
        val idGenerator: IdGenerator = FakeIdGenerator(),
        val sampler: Sampler = AlwaysOnSampler,
        val action: (SpanCreationAction.() -> Unit)? = null,
    )

    private class HostileIdGenerator(
        private val traceId: Boolean = false,
        private val spanId: Boolean = false,
        private val randomness: Boolean = false,
    ) : IdGenerator {
        private val delegate = FakeIdGenerator()

        override fun generateTraceIdBytes(): ByteArray = when {
            traceId -> boom()
            else -> delegate.generateTraceIdBytes()
        }

        override fun generateSpanIdBytes(): ByteArray = when {
            spanId -> boom()
            else -> delegate.generateSpanIdBytes()
        }

        override val generatesRandomTraceIds: Boolean
            get() = when {
                randomness -> boom()
                else -> false
            }

        override val invalidTraceId: ByteArray = delegate.invalidTraceId
        override val invalidSpanId: ByteArray = delegate.invalidSpanId
    }

    /**
     * Throws from [shouldSample], or - if [decisionOnly] - from the returned result instead.
     */
    private class HostileSampler(
        private val decisionOnly: Boolean = false,
    ) : Sampler {
        override fun shouldSample(
            context: Context,
            traceIdBytes: ByteArray,
            name: String,
            spanKind: SpanKind,
            attributes: AttributeContainer,
            links: List<SpanLink>,
        ): SamplingResult = when {
            decisionOnly -> HostileSamplingResult()
            else -> boom()
        }

        override val description: String = "HostileSampler"
    }

    private class HostileSamplingResult : SamplingResult {
        override val decision: SamplingResult.Decision
            get() = boom()
        override val attributes: AttributeContainer
            get() = boom()
        override val traceState: TraceState
            get() = boom()
    }

    private class ThrowingSdkErrorHandler : SdkErrorHandler {
        override fun onError(error: SdkError): Unit = boom()
    }
}

private fun boom(): Nothing = error("boom")
