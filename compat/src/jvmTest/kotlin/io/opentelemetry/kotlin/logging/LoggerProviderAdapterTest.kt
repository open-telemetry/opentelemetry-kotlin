package io.opentelemetry.kotlin.logging

import io.opentelemetry.kotlin.aliases.OtelJavaSdkLoggerProvider
import kotlin.test.Test
import kotlin.test.assertNotEquals
import kotlin.test.assertNotSame
import kotlin.test.assertSame

internal class LoggerProviderAdapterTest {

    private val adapter = LoggerProviderAdapter(OtelJavaSdkLoggerProvider.builder().build())

    @Test
    fun testDupeLoggerProviderAttributes() {
        val first = adapter.getLogger(name = "name") {
            setStringAttribute("key", "value")
        }
        val second = adapter.getLogger(name = "name") {
            setStringAttribute("key", "value")
        }
        val third = adapter.getLogger(name = "name") {
            setStringAttribute("foo", "bar")
        }
        assertSame(first, second)
        assertNotEquals(first, third)
    }

    @Test
    fun testScopePropertyBoundaryCollision() {
        val first = adapter.getLogger(name = "ab", version = "c")
        val second = adapter.getLogger(name = "a", version = "bc")
        assertNotSame(first, second)
    }

    @Test
    fun testNullScopePropertyCollision() {
        val first = adapter.getLogger(name = "name")
        val second = adapter.getLogger(name = "name", version = "null")
        assertNotSame(first, second)
    }
}
