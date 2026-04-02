package io.opentelemetry.kotlin.logging

import io.opentelemetry.kotlin.attributes.AttributesModel
import io.opentelemetry.kotlin.clock.FakeClock
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.factory.FakeContextFactory
import io.opentelemetry.kotlin.factory.FakeSpanContextFactory
import io.opentelemetry.kotlin.factory.FakeSpanFactory
import io.opentelemetry.kotlin.init.config.LogLimitConfig
import io.opentelemetry.kotlin.init.config.LoggingConfig
import io.opentelemetry.kotlin.logging.export.FakeLogRecordProcessor
import io.opentelemetry.kotlin.resource.FakeResource
import io.opentelemetry.kotlin.resource.ResourceImpl
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertSame

internal class LoggerProviderImplTest {

    private val clock = FakeClock()
    private val loggingConfig = LoggingConfig(
        null,
        LogLimitConfig(100, 100),
        ResourceImpl(AttributesModel(), null)
    )
    private val contextFactory = FakeContextFactory()
    private val spanContextFactory = FakeSpanContextFactory()
    private val spanFactory = FakeSpanFactory()
    private lateinit var impl: LoggerProviderImpl

    @BeforeTest
    fun setup() {
        impl = LoggerProviderImpl(clock, loggingConfig, contextFactory, spanContextFactory, spanFactory)
    }

    @Test
    fun testMinimalLoggerProvider() {
        assertNotNull(impl.getLogger(name = ""))
    }

    @Test
    fun testFullLoggerProvider() {
        val first = impl.getLogger(
            name = "name",
            version = "0.1.0",
            schemaUrl = "https://example.com/foo"
        ) {
            setStringAttribute("key", "value")
        }
        assertNotNull(first)
    }

    @Test
    fun testDupeLoggerProviderName() {
        val first = impl.getLogger(name = "name")
        val second = impl.getLogger(name = "name")
        val third = impl.getLogger(name = "other")
        assertSame(first, second)
        assertNotEquals(first, third)
    }

    @Test
    fun testDupeLoggerProviderVersion() {
        val first = impl.getLogger(name = "name", version = "0.1.0")
        val second = impl.getLogger(name = "name", version = "0.1.0")
        val third = impl.getLogger(name = "name", version = "0.2.0")
        assertSame(first, second)
        assertNotEquals(first, third)
    }

    @Test
    fun testDupeLoggerProviderSchemaUrl() {
        val first = impl.getLogger(name = "name", schemaUrl = "https://example.com/foo")
        val second = impl.getLogger(name = "name", schemaUrl = "https://example.com/foo")
        val third = impl.getLogger(name = "name", schemaUrl = "https://example.com/bar")
        assertSame(first, second)
        assertNotEquals(first, third)
    }

    @Test
    fun testDupeLoggerProviderAttributes() {
        val first = impl.getLogger(name = "name") {
            setStringAttribute("key", "value")
        }
        val second = impl.getLogger(name = "name") {
            setStringAttribute("key", "value")
        }
        val third = impl.getLogger(name = "name") {
            setStringAttribute("foo", "bar")
        }
        assertSame(first, second)
        assertNotEquals(first, third)
    }

    @Test
    fun testForceFlushEmptyProcessors() = runTest {
        val result = impl.forceFlush()
        assertEquals(OperationResultCode.Success, result)
    }

    @Test
    fun testShutdownEmptyProcessors() = runTest {
        val result = impl.shutdown()
        assertEquals(OperationResultCode.Success, result)
    }

    @Test
    fun testForceFlushProcessorDelegation() = runTest {
        var flushCalled = false
        val processor = FakeLogRecordProcessor(
            flushCode = {
                flushCalled = true
                OperationResultCode.Success
            }
        )
        val config = LoggingConfig(
            processor,
            LogLimitConfig(100, 100),
            FakeResource(),
        )
        impl = LoggerProviderImpl(clock, config, contextFactory, spanContextFactory, spanFactory)
        impl.getLogger(name = "test")

        val result = impl.forceFlush()
        assertEquals(OperationResultCode.Success, result)
        assertEquals(true, flushCalled)
    }

    @Test
    fun testShutdownProcessorDelegation() = runTest {
        var shutdownCalled = false
        val processor = FakeLogRecordProcessor(
            shutdownCode = {
                shutdownCalled = true
                OperationResultCode.Success
            }
        )
        val config = LoggingConfig(
            processor,
            LogLimitConfig(100, 100),
            FakeResource(),
        )
        impl = LoggerProviderImpl(clock, config, contextFactory, spanContextFactory, spanFactory)
        impl.getLogger(name = "test")

        val result = impl.shutdown()
        assertEquals(OperationResultCode.Success, result)
        assertEquals(true, shutdownCalled)
    }

    @Test
    fun testGetLoggerAfterShutdownReturnsNoopLogger() = runTest {
        impl.shutdown()
        val logger = impl.getLogger(name = "test")
        assertFalse(logger.enabled())
    }

    @Test
    fun testExistingLoggerDoesNotEmitAfterShutdown() = runTest {
        val processor = FakeLogRecordProcessor()
        val config = LoggingConfig(
            processor,
            LogLimitConfig(100, 100),
            FakeResource(),
        )
        impl = LoggerProviderImpl(clock, config, contextFactory, spanContextFactory, spanFactory)
        val logger = impl.getLogger(name = "test")
        impl.shutdown()
        logger.emit(body = "should not emit")
        assertEquals(0, processor.logs.size)
    }
}
