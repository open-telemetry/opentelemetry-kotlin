package io.opentelemetry.kotlin.logging

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.attributes.MutableAttributeContainerImpl
import io.opentelemetry.kotlin.clock.FakeClock
import io.opentelemetry.kotlin.export.MutableShutdownState
import io.opentelemetry.kotlin.export.OperationResultCode
import io.opentelemetry.kotlin.factory.createSdkFactory
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

@OptIn(ExperimentalApi::class)
internal class LoggerProviderImplTest {

    private val clock = FakeClock()
    private val loggingConfig = LoggingConfig(
        emptyList(),
        LogLimitConfig(100, 100),
        ResourceImpl(MutableAttributeContainerImpl(), null)
    )
    private val factory = createSdkFactory()
    private lateinit var impl: LoggerProviderImpl

    @BeforeTest
    fun setup() {
        impl = LoggerProviderImpl(
            clock = clock,
            loggingConfig = loggingConfig,
            sdkFactory = factory,
            shutdownState = MutableShutdownState(),
        )
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
            listOf(processor),
            LogLimitConfig(100, 100),
            FakeResource(),
        )
        val impl = LoggerProviderImpl(
            clock = clock,
            loggingConfig = config,
            sdkFactory = factory,
            shutdownState = MutableShutdownState(),
        )
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
            listOf(processor),
            LogLimitConfig(100, 100),
            FakeResource(),
        )
        val impl = LoggerProviderImpl(
            clock = clock,
            loggingConfig = config,
            sdkFactory = factory,
            shutdownState = MutableShutdownState(),
        )
        impl.getLogger(name = "test")

        val result = impl.shutdown()
        assertEquals(OperationResultCode.Success, result)
        assertEquals(true, shutdownCalled)
    }

    @Test
    fun testGetLoggerAfterShutdownReturnsNoopLogger() = runTest {
        val shutdownState = MutableShutdownState()
        val provider = LoggerProviderImpl(
            clock = clock,
            loggingConfig = loggingConfig,
            sdkFactory = factory,
            shutdownState = shutdownState,
        )
        shutdownState.shutdown()
        val logger = provider.getLogger(name = "test")
        assertFalse(logger.enabled())
    }

    @Test
    fun testExistingLoggerDoesNotEmitAfterShutdown() = runTest {
        val shutdownState = MutableShutdownState()
        val processor = FakeLogRecordProcessor()
        val config = LoggingConfig(
            listOf(processor),
            LogLimitConfig(100, 100),
            FakeResource(),
        )
        val provider = LoggerProviderImpl(
            clock = clock,
            loggingConfig = config,
            sdkFactory = factory,
            shutdownState = shutdownState,
        )
        val logger = provider.getLogger(name = "test")
        shutdownState.shutdown()
        logger.emit(body = "should not emit")
        assertEquals(0, processor.logs.size)
    }
}
