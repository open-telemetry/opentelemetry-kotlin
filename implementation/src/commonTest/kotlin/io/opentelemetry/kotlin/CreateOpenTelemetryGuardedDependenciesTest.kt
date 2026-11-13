package io.opentelemetry.kotlin

import io.opentelemetry.kotlin.attributes.AttributeContainer
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import io.opentelemetry.kotlin.factory.IdGenerator
import io.opentelemetry.kotlin.framework.SdkTracker
import io.opentelemetry.kotlin.logging.export.FakeLogRecordProcessor
import io.opentelemetry.kotlin.tracing.SpanKind
import io.opentelemetry.kotlin.tracing.export.FakeSpanProcessor
import io.opentelemetry.kotlin.tracing.model.SpanLink
import io.opentelemetry.kotlin.tracing.sampling.Sampler
import io.opentelemetry.kotlin.tracing.sampling.SamplingResult
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
internal class CreateOpenTelemetryGuardedDependenciesTest {

    private val sdks = SdkTracker()

    @AfterTest
    fun tearDown() = sdks.shutdownAll()

    @Test
    fun `throwing clock id generator and sampler fall back to the defaults`() {
        val handler = FakeSdkErrorHandler()
        val spanProcessor = FakeSpanProcessor()
        val logProcessor = FakeLogRecordProcessor()
        val api = createOpenTelemetry(clock = { boom() }) {
            errorHandler(handler)
            idGenerator { HostileIdGenerator }
            tracerProvider {
                sampler { HostileSampler }
                export { spanProcessor }
            }
            loggerProvider { export { logProcessor } }
        }.also(sdks::track)

        val span = api.tracerProvider.getTracer("test").startSpan("span")
        api.loggerProvider.getLogger("test").emit("log")

        assertFalse(span.isRecording())
        assertTrue(span.spanContext.isValid)
        assertTrue(spanProcessor.startCalls.isEmpty())
        assertTrue(checkNotNull(logProcessor.logs.single().observedTimestamp) > 0)
        assertEquals(
            setOf(
                "Clock.now failed, using fallback clock",
                "IdGenerator.generateTraceIdBytes failed, using fallback",
                "IdGenerator.generateSpanIdBytes failed, using fallback",
                "Sampler failed, using fallback sampler",
            ),
            handler.userCodeErrors.map { it.message }.toSet(),
        )
    }

    private object HostileIdGenerator : IdGenerator {
        override fun generateSpanIdBytes(): ByteArray = boom()
        override fun generateTraceIdBytes(): ByteArray = boom()
        override val invalidTraceId: ByteArray = ByteArray(16)
        override val invalidSpanId: ByteArray = ByteArray(8)
    }

    private object HostileSampler : Sampler {
        override val description: String = "HostileSampler"

        override fun shouldSample(
            context: Context,
            traceIdBytes: ByteArray,
            name: String,
            spanKind: SpanKind,
            attributes: AttributeContainer,
            links: List<SpanLink>,
        ): SamplingResult = boom()
    }
}

private fun boom(): Nothing = error("boom")
