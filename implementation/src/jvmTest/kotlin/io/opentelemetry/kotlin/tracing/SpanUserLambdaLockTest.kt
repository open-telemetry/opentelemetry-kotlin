package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.InstrumentationScopeInfoImpl
import io.opentelemetry.kotlin.clock.FakeClock
import io.opentelemetry.kotlin.error.NoopSdkErrorHandler
import io.opentelemetry.kotlin.export.MutableShutdownState
import io.opentelemetry.kotlin.factory.FakeContextFactory
import io.opentelemetry.kotlin.factory.FakeIdGenerator
import io.opentelemetry.kotlin.factory.FakeSpanContextFactory
import io.opentelemetry.kotlin.resource.FakeResource
import io.opentelemetry.kotlin.tracing.export.FakeSpanProcessor
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class SpanUserLambdaLockTest {

    @Test
    fun eventAttributesLambdaDoesNotBlockOtherThreads() {
        val processor = FakeSpanProcessor()
        val span = tracer(processor).startSpan("test")
        var workerCompleted = false
        span.addEvent("event") {
            setStringAttribute("event_key", "value")
            workerCompleted = mutateFromOtherThread(span)
        }
        span.end()

        assertTrue(workerCompleted)
        with(processor.endCalls.single()) {
            assertEquals(mapOf("key" to "value"), attributes)
            assertEquals(mapOf("event_key" to "value"), events.single().attributes)
        }
    }

    @Test
    fun linkAttributesLambdaDoesNotBlockOtherThreads() {
        val processor = FakeSpanProcessor()
        val span = tracer(processor).startSpan("test")
        var workerCompleted = false
        span.addLink(FakeSpanContext.VALID) {
            setStringAttribute("link_key", "value")
            workerCompleted = mutateFromOtherThread(span)
        }
        span.end()

        assertTrue(workerCompleted)
        with(processor.endCalls.single()) {
            assertEquals(mapOf("key" to "value"), attributes)
            assertEquals(mapOf("link_key" to "value"), links.single().attributes)
        }
    }

    private fun mutateFromOtherThread(span: Span): Boolean {
        val workerFinished = CountDownLatch(1)
        Thread {
            span.setStringAttribute("key", "value")
            workerFinished.countDown()
        }.start()
        return workerFinished.await(JOIN_TIMEOUT_MS, TimeUnit.MILLISECONDS)
    }

    private fun tracer(processor: FakeSpanProcessor): TracerImpl = TracerImpl(
        clock = FakeClock(),
        processor = processor,
        contextFactory = FakeContextFactory(),
        spanContextFactory = FakeSpanContextFactory(),
        scope = InstrumentationScopeInfoImpl("key", null, null, emptyMap()),
        resource = FakeResource(),
        spanLimitConfig = fakeSpanLimitsConfig,
        idGenerator = FakeIdGenerator(),
        shutdownState = MutableShutdownState(),
        sdkErrorHandler = NoopSdkErrorHandler,
    )

    private companion object {
        const val JOIN_TIMEOUT_MS = 5_000L
    }
}
