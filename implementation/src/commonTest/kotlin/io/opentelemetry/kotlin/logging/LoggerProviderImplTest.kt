package io.opentelemetry.kotlin.logging

import io.opentelemetry.kotlin.ExperimentalApi
import io.opentelemetry.kotlin.attributes.MutableAttributeContainerImpl
import io.opentelemetry.kotlin.clock.FakeClock
import io.opentelemetry.kotlin.factory.createSdkFactory
import io.opentelemetry.kotlin.init.config.LogLimitConfig
import io.opentelemetry.kotlin.init.config.LoggingConfig
import io.opentelemetry.kotlin.resource.ResourceImpl
import kotlin.test.Test
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

    @Test
    fun testMinimalLoggerProvider() {
        val impl = LoggerProviderImpl(clock, loggingConfig, factory)
        assertNotNull(impl.getLogger(name = ""))
    }

    @Test
    fun testFullLoggerProvider() {
        val impl = LoggerProviderImpl(clock, loggingConfig, factory)
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
        val impl = LoggerProviderImpl(clock, loggingConfig, factory)
        val first = impl.getLogger(name = "name")
        val second = impl.getLogger(name = "name")
        val third = impl.getLogger(name = "other")
        assertSame(first, second)
        assertNotEquals(first, third)
    }

    @Test
    fun testDupeLoggerProviderVersion() {
        val impl = LoggerProviderImpl(clock, loggingConfig, factory)
        val first = impl.getLogger(name = "name", version = "0.1.0")
        val second = impl.getLogger(name = "name", version = "0.1.0")
        val third = impl.getLogger(name = "name", version = "0.2.0")
        assertSame(first, second)
        assertNotEquals(first, third)
    }

    @Test
    fun testDupeLoggerProviderSchemaUrl() {
        val impl = LoggerProviderImpl(clock, loggingConfig, factory)
        val first = impl.getLogger(name = "name", schemaUrl = "https://example.com/foo")
        val second = impl.getLogger(name = "name", schemaUrl = "https://example.com/foo")
        val third = impl.getLogger(name = "name", schemaUrl = "https://example.com/bar")
        assertSame(first, second)
        assertNotEquals(first, third)
    }

    @Test
    fun testDupeLoggerProviderAttributes() {
        val impl = LoggerProviderImpl(clock, loggingConfig, factory)
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
}
