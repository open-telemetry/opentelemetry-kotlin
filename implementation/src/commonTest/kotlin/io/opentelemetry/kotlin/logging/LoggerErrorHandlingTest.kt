package io.opentelemetry.kotlin.logging

import io.opentelemetry.kotlin.Clock
import io.opentelemetry.kotlin.InstrumentationScopeInfoImpl
import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.behavior.AttributeLimitsBehavior
import io.opentelemetry.kotlin.clock.FakeClock
import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import io.opentelemetry.kotlin.error.SdkError
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.error.SdkErrorSeverity
import io.opentelemetry.kotlin.export.MutableShutdownState
import io.opentelemetry.kotlin.logging.export.FakeLogRecordProcessor
import io.opentelemetry.kotlin.resource.FakeResource
import io.opentelemetry.kotlin.semconv.ExceptionAttributes
import io.opentelemetry.kotlin.tracing.fakeLogLimitsConfig
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class LoggerErrorHandlingTest {

    private val key = InstrumentationScopeInfoImpl("test-logger", null, null, emptyMap())

    private val hostileCases = listOf(
        Case("clock throws", clock = Clock { boom() }),
        Case("attributes lambda throws", attributes = { boom() }),
        Case("exception message throws", exception = HostileThrowable()),
    )

    @Test
    fun testHostileDependencyDoesNotEscapeEmit() {
        hostileCases.forEach { case ->
            val errorHandler = FakeSdkErrorHandler()
            val processor = FakeLogRecordProcessor()
            val logger = createLogger(case, processor, errorHandler)

            logger.emit("message", exception = case.exception, attributes = case.attributes)

            when (case.name) {
                "attributes lambda throws" -> {
                    assertEquals(1, processor.logs.size, case.name)
                    val error = errorHandler.userCodeErrors.single()
                    assertEquals("Logger.emit attributes failed", error.message, case.name)
                    assertEquals("boom", error.cause.message, case.name)
                }
                "exception message throws" -> {
                    assertEquals(1, processor.logs.size, case.name)
                    val error = errorHandler.userCodeErrors.single()
                    assertEquals("LogRecord.exception attributes failed", error.message, case.name)
                    assertEquals("boom", error.cause.message, case.name)
                }
                else -> {
                    assertTrue(processor.logs.isEmpty(), case.name)
                    val error = errorHandler.errors.single()
                    val cause = assertIs<SdkError.UserCodeError>(error, case.name).cause
                    assertEquals("Logger.emit failed", error.message, case.name)
                    assertEquals(SdkErrorSeverity.WARNING, error.severity, case.name)
                    assertEquals("boom", cause.message, case.name)
                }
            }
        }
    }

    @Test
    fun testExceptionAttributeFailureDoesNotPreventUserAttributesOrEmit() {
        val errorHandler = FakeSdkErrorHandler()
        val processor = FakeLogRecordProcessor()
        val logger = createLogger(Case("healthy"), processor, errorHandler)
        var attributesApplied = false

        logger.emit(
            "message",
            exception = HostileThrowable(),
            attributes = { attributesApplied = true },
        )

        assertTrue(attributesApplied)
        assertEquals(1, processor.logs.size)
        assertEquals("LogRecord.exception attributes failed", errorHandler.userCodeErrors.single().message)
    }

    @Test
    fun testLoggerRemainsUsableAfterHostileAttributes() {
        val errorHandler = FakeSdkErrorHandler()
        val processor = FakeLogRecordProcessor()
        val logger = createLogger(Case("healthy"), processor, errorHandler)

        logger.emit("first") { boom() }
        assertEquals(1, errorHandler.userCodeErrors.size)
        assertTrue(processor.logs.isNotEmpty())

        logger.emit("second")
        assertEquals(1, errorHandler.userCodeErrors.size)
        assertEquals(2, processor.logs.size)
    }

    @Test
    fun testHostileBodyStillEmitsLog() {
        val errorHandler = FakeSdkErrorHandler()
        val processor = FakeLogRecordProcessor()
        val logger = createLogger(Case("healthy"), processor, errorHandler)
        logger.emit(body = HostileBody(), eventName = "event")

        val error = errorHandler.userCodeErrors.single()
        assertEquals("LogRecord.body failed", error.message)
        assertEquals("boom", error.cause.message)
        val log = processor.logs.single()
        assertNull(log.body)
        assertEquals("event", log.eventName)
    }

    @Test
    fun testThrowingErrorHandlerDoesNotEscapeEmit() {
        val processor = FakeLogRecordProcessor()
        val logger = createLogger(
            Case("clock throws", clock = Clock { boom() }),
            processor,
            ThrowingSdkErrorHandler(),
        )

        logger.emit("message")

        assertTrue(processor.logs.isEmpty())
    }

    @Test
    fun testUserAttributesSurviveAttributeCountLimit() {
        val errorHandler = FakeSdkErrorHandler()
        val processor = FakeLogRecordProcessor()
        val logLimits = AttributeLimitsBehavior(attributeCountLimit = 2, attributeValueLengthLimit = 128)
        val logger = LoggerImpl(
            clock = FakeClock(),
            processor = processor,
            key = key,
            resource = FakeResource(),
            logLimits = logLimits,
            shutdownState = MutableShutdownState(),
            sdkErrorHandler = errorHandler,
        )

        logger.emit(
            "message",
            exception = RuntimeException("test exception"),
            attributes = {
                setStringAttribute("user.key1", "value1")
                setStringAttribute("user.key2", "value2")
            }
        )

        assertEquals(1, processor.logs.size)
        val log = processor.logs.single()
        // User attributes should be retained (filling the limit)
        assertEquals("value1", log.attributes["user.key1"])
        assertEquals("value2", log.attributes["user.key2"])
        // Exception attributes should be dropped due to limit
        assertNull(log.attributes[ExceptionAttributes.EXCEPTION_MESSAGE])
    }

    @Test
    fun testExceptionAttributesAddedWhenCapacityRemains() {
        val errorHandler = FakeSdkErrorHandler()
        val processor = FakeLogRecordProcessor()
        val logLimits = AttributeLimitsBehavior(attributeCountLimit = 5, attributeValueLengthLimit = 128)
        val logger = LoggerImpl(
            clock = FakeClock(),
            processor = processor,
            key = key,
            resource = FakeResource(),
            logLimits = logLimits,
            shutdownState = MutableShutdownState(),
            sdkErrorHandler = errorHandler,
        )

        logger.emit(
            "message",
            exception = RuntimeException("test exception"),
            attributes = {
                setStringAttribute("user.key1", "value1")
                setStringAttribute("user.key2", "value2")
            }
        )

        assertEquals(1, processor.logs.size)
        val log = processor.logs.single()
        // User attributes should be retained
        assertEquals("value1", log.attributes["user.key1"])
        assertEquals("value2", log.attributes["user.key2"])
        // Exception attributes should be added where capacity permits
        assertEquals("test exception", log.attributes[ExceptionAttributes.EXCEPTION_MESSAGE])
        assertNotNull(log.attributes[ExceptionAttributes.EXCEPTION_TYPE])
        assertNotNull(log.attributes[ExceptionAttributes.EXCEPTION_STACKTRACE])
    }

    @Test
    fun testUserAttributesWinOnKeyCollisions() {
        val errorHandler = FakeSdkErrorHandler()
        val processor = FakeLogRecordProcessor()
        val logger = createLogger(Case("healthy"), processor, errorHandler)

        logger.emit(
            "message",
            exception = RuntimeException("exception message"),
            attributes = {
                setStringAttribute(ExceptionAttributes.EXCEPTION_MESSAGE, "user message")
            }
        )

        assertEquals(1, processor.logs.size)
        val log = processor.logs.single()
        // User-provided exception.message should win
        assertEquals("user message", log.attributes[ExceptionAttributes.EXCEPTION_MESSAGE])
    }

    @Test
    fun testAttributeCountRemainsBounded() {
        val errorHandler = FakeSdkErrorHandler()
        val processor = FakeLogRecordProcessor()
        val logLimits = AttributeLimitsBehavior(attributeCountLimit = 3, attributeValueLengthLimit = 128)
        val logger = LoggerImpl(
            clock = FakeClock(),
            processor = processor,
            key = key,
            resource = FakeResource(),
            logLimits = logLimits,
            shutdownState = MutableShutdownState(),
            sdkErrorHandler = errorHandler,
        )

        logger.emit(
            "message",
            exception = RuntimeException("test exception"),
            attributes = {
                setStringAttribute("user.key1", "value1")
                setStringAttribute("user.key2", "value2")
                setStringAttribute("user.key3", "value3")
            }
        )

        assertEquals(1, processor.logs.size)
        val log = processor.logs.single()
        // Total attributes should not exceed the limit
        assertTrue(log.attributes.size <= 3)
        assertEquals(3, log.attributes.size)
        // User attributes should all be present
        assertEquals("value1", log.attributes["user.key1"])
        assertEquals("value2", log.attributes["user.key2"])
        assertEquals("value3", log.attributes["user.key3"])
        // Exception attributes should be dropped
        assertNull(log.attributes[ExceptionAttributes.EXCEPTION_MESSAGE])
    }

    @Test
    fun testNoExceptionNormalBehavior() {
        val errorHandler = FakeSdkErrorHandler()
        val processor = FakeLogRecordProcessor()
        val logger = createLogger(Case("healthy"), processor, errorHandler)

        logger.emit(
            "message",
            attributes = {
                setStringAttribute("user.key1", "value1")
                setStringAttribute("user.key2", "value2")
            }
        )

        assertEquals(1, processor.logs.size)
        val log = processor.logs.single()
        assertEquals("value1", log.attributes["user.key1"])
        assertEquals("value2", log.attributes["user.key2"])
    }

    private fun createLogger(
        case: Case,
        processor: FakeLogRecordProcessor,
        errorHandler: SdkErrorHandler,
    ) = LoggerImpl(
        clock = case.clock,
        processor = processor,
        key = key,
        resource = FakeResource(),
        logLimits = fakeLogLimitsConfig,
        shutdownState = MutableShutdownState(),
        sdkErrorHandler = errorHandler,
    )

    private class Case(
        val name: String,
        val clock: Clock = FakeClock(),
        val attributes: (AttributesMutator.() -> Unit)? = null,
        val exception: Throwable? = null,
    )

    private class HostileThrowable : Throwable() {
        override val message: String
            get() = boom()
    }

    private class HostileBody {
        override fun toString(): String = boom()
    }

    private class ThrowingSdkErrorHandler : SdkErrorHandler {
        override fun onError(error: SdkError): Unit = boom()
    }
}

private fun boom(): Nothing = error("boom")
