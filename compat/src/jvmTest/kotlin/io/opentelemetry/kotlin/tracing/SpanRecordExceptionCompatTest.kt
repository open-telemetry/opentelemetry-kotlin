package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.framework.OtelKotlinHarness
import io.opentelemetry.kotlin.semconv.ExceptionAttributes
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalApi::class)
internal class SpanRecordExceptionCompatTest {

    private lateinit var harness: OtelKotlinHarness

    @BeforeTest
    fun setUp() = runTest {
        harness = OtelKotlinHarness(testScheduler)
    }

    @Test
    fun testRecordException() = runTest {
        harness.tracer.startSpan("test").apply {
            recordException(IllegalStateException("something went wrong"))
            end()
        }

        harness.assertSpans(1) { spans ->
            val attrs = spans.single().events.single().also {
                assertEquals("exception", it.name)
            }.attributes
            assertNotNull(attrs[ExceptionAttributes.EXCEPTION_STACKTRACE])
            assertEquals("something went wrong", attrs[ExceptionAttributes.EXCEPTION_MESSAGE])
            assertTrue((attrs[ExceptionAttributes.EXCEPTION_TYPE] as String).contains("IllegalStateException"))
        }
    }

    @Test
    fun testRecordExceptionExtraAttrs() = runTest {
        harness.tracer.startSpan("test").apply {
            recordException(RuntimeException("oops")) {
                setStringAttribute("custom.key", "custom.value")
            }
            end()
        }

        harness.assertSpans(1) { spans ->
            val attrs = spans.single().events.single().attributes
            assertNotNull(attrs[ExceptionAttributes.EXCEPTION_STACKTRACE])
            assertEquals("custom.value", attrs["custom.key"])
        }
    }
}
