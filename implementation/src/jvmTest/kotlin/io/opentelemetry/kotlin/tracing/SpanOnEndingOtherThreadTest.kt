package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.InstrumentationScopeInfoImpl
import io.opentelemetry.kotlin.clock.FakeClock
import io.opentelemetry.kotlin.error.NoopSdkErrorHandler
import io.opentelemetry.kotlin.export.MutableShutdownState
import io.opentelemetry.kotlin.factory.FakeContextFactory
import io.opentelemetry.kotlin.factory.FakeIdGenerator
import io.opentelemetry.kotlin.factory.FakeSpanContextFactory
import io.opentelemetry.kotlin.factory.FakeTraceFlagsFactory
import io.opentelemetry.kotlin.resource.FakeResource
import io.opentelemetry.kotlin.tracing.export.FakeSpanProcessor
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class SpanOnEndingOtherThreadTest {

    @Test
    fun spanEndsWhenOnEndingIsNotRequired() {
        val processor = FakeSpanProcessor(onEndingRequired = false)
        val span = tracer(processor).startSpan("test")
        span.setStringAttribute("key", "value")

        span.end()
        span.setName("after-ending")

        assertTrue(processor.endingCalls.isEmpty())
        with(processor.endCalls.single()) {
            assertEquals("test", name)
            assertEquals(mapOf("key" to "value"), attributes)
            assertTrue(hasEnded)
        }
    }

    @Test
    fun activeSpanMutationFromOtherThreadIsRetained() {
        val processor = FakeSpanProcessor()
        val workerFinished = CountDownLatch(1)
        val span = tracer(processor).startSpan("test")
        val worker = Thread {
            span.setStringAttribute("key", "value")
            workerFinished.countDown()
        }
        worker.start()

        assertTrue(workerFinished.await(JOIN_TIMEOUT_MS, TimeUnit.MILLISECONDS))
        span.end()

        assertEquals(mapOf("key" to "value"), processor.endCalls.single().attributes)
    }

    @Test
    fun onEndingRejectsOtherThreadMutationWithoutDeadlock() {
        val processor = FakeSpanProcessor()
        val workerFinished = CountDownLatch(1)
        var workerCompleted = false
        processor.endingAction = { span ->
            span.setName("from-callback")
            span.setStringAttribute("callback", "value")
            val worker = Thread {
                span.setName("from-other-thread")
                span.setStringAttribute("key", "value")
                workerFinished.countDown()
            }
            worker.start()
            workerCompleted = workerFinished.await(JOIN_TIMEOUT_MS, TimeUnit.MILLISECONDS)
        }

        val tracer = tracer(processor)

        tracer.startSpan("test").apply {
            setName("before-ending")
            end()
        }

        assertTrue(workerCompleted)
        with(processor.endCalls.single()) {
            assertEquals("from-callback", name)
            assertEquals(mapOf("callback" to "value"), attributes)
        }
    }

    @Test
    fun endingIsIdempotentAndSnapshotIsImmutable() {
        val processor = FakeSpanProcessor()
        processor.endingAction = { span ->
            span.setName("from-callback")
            span.end(123L)
        }

        val span = tracer(processor).startSpan("test")
        span.end(42L)
        span.end(99L)
        span.setName("after-ending")
        span.setStringAttribute("key", "value")

        with(processor.endCalls.single()) {
            assertEquals("from-callback", name)
            assertEquals(42L, endTimestamp)
            assertEquals(emptyMap(), attributes)
        }
    }

    @Test
    fun endingCallbackExceptionStillEndsSpan() {
        val processor = FakeSpanProcessor()
        processor.endingAction = { throw IllegalStateException("callback failure") }

        val span = tracer(processor).startSpan("test")
        span.end()

        with(processor.endCalls.single()) {
            assertEquals("test", name)
            assertTrue(hasEnded)
        }
    }

    private fun tracer(processor: FakeSpanProcessor): TracerImpl = TracerImpl(
        clock = FakeClock(),
        processor = processor,
        contextFactory = FakeContextFactory(),
        spanContextFactory = FakeSpanContextFactory(),
        traceFlagsFactory = FakeTraceFlagsFactory(),
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
