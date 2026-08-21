package io.opentelemetry.kotlin.logging

import io.opentelemetry.kotlin.Clock
import io.opentelemetry.kotlin.InstrumentationScopeInfoImpl
import io.opentelemetry.kotlin.attributes.AttributesMutator
import io.opentelemetry.kotlin.clock.FakeClock
import io.opentelemetry.kotlin.context.Context
import io.opentelemetry.kotlin.context.ContextKey
import io.opentelemetry.kotlin.context.FakeContextKey
import io.opentelemetry.kotlin.error.FakeSdkErrorHandler
import io.opentelemetry.kotlin.error.SdkError
import io.opentelemetry.kotlin.error.SdkErrorHandler
import io.opentelemetry.kotlin.error.SdkErrorSeverity
import io.opentelemetry.kotlin.export.MutableShutdownState
import io.opentelemetry.kotlin.factory.ContextFactory
import io.opentelemetry.kotlin.factory.FakeContextFactory
import io.opentelemetry.kotlin.factory.FakeSpanContextFactory
import io.opentelemetry.kotlin.logging.export.FakeLogRecordProcessor
import io.opentelemetry.kotlin.resource.FakeResource
import io.opentelemetry.kotlin.tracing.fakeLogLimitsConfig
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class LoggerErrorHandlingTest {

    private val key = InstrumentationScopeInfoImpl("test-logger", null, null, emptyMap())

    private val hostileCases = listOf(
        Case("clock throws", clock = Clock { boom() }),
        Case("context factory throws", contextFactory = HostileContextFactory()),
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

            assertTrue(processor.logs.isEmpty(), case.name)
            assertEquals(1, errorHandler.errors.size, case.name)
            val error = errorHandler.userCodeErrors.first()
            assertEquals("Logger.emit failed", error.message, case.name)
            assertEquals(SdkErrorSeverity.WARNING, error.severity, case.name)
            assertEquals("boom", error.cause.message, case.name)
        }
    }

    @Test
    fun testHostileContextFactoryDoesNotEscapeEnabled() {
        val errorHandler = FakeSdkErrorHandler()
        val logger = createLogger(
            Case("context factory throws", contextFactory = HostileContextFactory()),
            FakeLogRecordProcessor(),
            errorHandler,
        )

        assertFalse(logger.enabled())

        val error = errorHandler.userCodeErrors.single()
        assertEquals("Logger.enabled failed", error.message)
        assertEquals("boom", error.cause.message)
    }

    @Test
    fun testLoggerRemainsUsableAfterHostileAttributes() {
        val errorHandler = FakeSdkErrorHandler()
        val processor = FakeLogRecordProcessor()
        val logger = createLogger(Case("healthy"), processor, errorHandler)

        logger.emit("first") { boom() }
        assertEquals(1, errorHandler.userCodeErrors.size)
        assertTrue(processor.logs.isEmpty())

        logger.emit("second")
        assertEquals(1, errorHandler.userCodeErrors.size)
        assertEquals(1, processor.logs.size)
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

    private fun createLogger(
        case: Case,
        processor: FakeLogRecordProcessor,
        errorHandler: SdkErrorHandler,
    ) = LoggerImpl(
        clock = case.clock,
        processor = processor,
        contextFactory = case.contextFactory,
        spanContextFactory = FakeSpanContextFactory(),
        key = key,
        resource = FakeResource(),
        logLimitConfig = fakeLogLimitsConfig,
        shutdownState = MutableShutdownState(),
        sdkErrorHandler = errorHandler,
    )

    private class Case(
        val name: String,
        val clock: Clock = FakeClock(),
        val contextFactory: ContextFactory = FakeContextFactory(),
        val attributes: (AttributesMutator.() -> Unit)? = null,
        val exception: Throwable? = null,
    )

    private class HostileContextFactory : ContextFactory {
        private val delegate = FakeContextFactory()

        override fun root(): Context = delegate.root()

        override fun implicit(): Context = boom()

        override fun <T> createKey(name: String): ContextKey<T> = FakeContextKey(name)
    }

    private class HostileThrowable : Throwable() {
        override val message: String
            get() = boom()
    }

    private class ThrowingSdkErrorHandler : SdkErrorHandler {
        override fun onError(error: SdkError): Unit = boom()
    }
}

private fun boom(): Nothing = error("boom")
