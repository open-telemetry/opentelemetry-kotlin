package io.opentelemetry.kotlin.tracing

import io.opentelemetry.kotlin.Clock
import io.opentelemetry.kotlin.FakeInstrumentationScopeInfo
import io.opentelemetry.kotlin.attributes.AnyValue
import io.opentelemetry.kotlin.clock.FakeClock
import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import io.opentelemetry.kotlin.error.SdkError
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.error.SdkErrorSeverity
import io.opentelemetry.kotlin.init.config.SpanLimitConfig
import io.opentelemetry.kotlin.resource.FakeResource
import io.opentelemetry.kotlin.tracing.export.FakeSpanProcessor
import io.opentelemetry.kotlin.tracing.model.ReadWriteSpan
import io.opentelemetry.kotlin.tracing.model.SpanModel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class SpanMutatorErrorHandlingTest {

    private val truncatingLimits = SpanLimitConfig(
        attributeCountLimit = 100,
        attributeValueLengthLimit = 100,
        linkCountLimit = 100,
        eventCountLimit = 100,
        attributeCountPerEventLimit = 100,
        attributeCountPerLinkLimit = 100,
    )

    private val hostileCases = listOf(
        Case("string list attribute") { setStringListAttribute("key", HostileList()) },
        Case("any value attribute") {
            setAnyValueAttribute("key", AnyValue.ListValue(HostileList()))
        },
        Case("event attributes") { addEvent("event") { boom() } },
        Case("link attributes") { addLink(FakeSpanContext.VALID) { boom() } },
    )

    @Test
    fun testHostileInputDoesNotEscapeMutator() {
        hostileCases.forEach { case ->
            val errorHandler = FakeSdkErrorHandler()
            val span = createSpan(errorHandler = errorHandler)
            case.mutate(span)

            val error = errorHandler.userCodeErrors.single()
            assertEquals(SdkErrorSeverity.WARNING, error.severity, case.name)
            assertEquals("boom", error.cause.message, case.name)
            assertTrue(span.isRecording(), case.name)
        }
    }

    @Test
    fun testSpanRemainsUsableAfterHostileInput() {
        val errorHandler = FakeSdkErrorHandler()
        val processor = FakeSpanProcessor()
        val span = createSpan(processor = processor, errorHandler = errorHandler)

        span.setStringListAttribute("hostile", HostileList())
        span.setStringAttribute("key", "value")
        span.end()

        assertEquals(1, errorHandler.userCodeErrors.size)
        assertEquals("value", span.attributes["key"])
        assertFalse(span.attributes.containsKey("hostile"))
        assertTrue(span.hasEnded)
        assertEquals(1, processor.endCalls.size)
    }

    @Test
    fun testHostileClockStillEndsSpan() {
        val errorHandler = FakeSdkErrorHandler()
        val processor = FakeSpanProcessor()
        val span = createSpan(
            clock = { boom() },
            processor = processor,
            errorHandler = errorHandler,
        )
        span.end()

        assertTrue(span.hasEnded)
        assertFalse(span.isRecording())
        assertEquals(0L, span.endTimestamp)
        assertEquals(1, processor.endCalls.size)
        assertEquals("boom", errorHandler.userCodeErrors.single().cause.message)
    }

    @Test
    fun testThrowingErrorHandlerDoesNotEscapeMutator() {
        val span = createSpan(errorHandler = ThrowingSdkErrorHandler())
        span.setStringListAttribute("key", HostileList())
        assertTrue(span.isRecording())
    }

    private fun createSpan(
        clock: Clock = FakeClock(),
        processor: FakeSpanProcessor? = null,
        errorHandler: SdkErrorHandler,
    ) = SpanModel(
        clock = clock,
        processor = processor,
        name = "span",
        spanKind = SpanKind.INTERNAL,
        startTimestamp = 0L,
        instrumentationScopeInfo = FakeInstrumentationScopeInfo(),
        resource = FakeResource(),
        parent = FakeSpanContext.INVALID,
        spanContext = FakeSpanContext.VALID,
        spanLimitConfig = truncatingLimits,
        initialLinks = emptyList(),
        sdkErrorHandler = errorHandler,
    )

    private class Case(
        val name: String,
        val mutate: ReadWriteSpan.() -> Unit,
    )

    /**
     * A list that throws when read. Both [size] and [iterator] are hostile so that every path
     * through the attribute code hits the failure.
     */
    private class HostileList<T>(
        private val delegate: List<T> = emptyList(),
    ) : List<T> by delegate {
        override val size: Int
            get() = boom()

        override fun iterator(): Iterator<T> = boom()
    }

    private class ThrowingSdkErrorHandler : SdkErrorHandler {
        override fun onError(error: SdkError): Unit = boom()
    }
}

private fun boom(): Nothing = error("boom")
