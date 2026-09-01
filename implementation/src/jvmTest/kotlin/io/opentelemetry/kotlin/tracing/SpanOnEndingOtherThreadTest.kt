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
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

internal class SpanOnEndingOtherThreadTest {

    @Test
    fun onEndingCanMutateFromAnotherThreadWithoutDeadlock() {
        val processor = FakeSpanProcessor()
        processor.endingAction = { span ->
            val worker = Thread {
                span.setName("from-other-thread")
                span.setStringAttribute("key", "value")
            }
            worker.start()
            worker.join(JOIN_TIMEOUT_MS)
            assertFalse(worker.isAlive)
        }

        val tracer = TracerImpl(
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

        tracer.startSpan("test").end()

        with(processor.endCalls.single()) {
            assertEquals("from-other-thread", name)
            assertEquals(mapOf("key" to "value"), attributes)
        }
    }

    private companion object {
        const val JOIN_TIMEOUT_MS = 5_000L
    }
}
